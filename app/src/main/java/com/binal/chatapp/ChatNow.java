package com.binal.chatapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ChatNow extends AppCompatActivity
        implements  CommunicationManager.OnNewMessageReceivedCallback,
                    CommunicationManager.MidCTCallback,
                    CommunicationManager.StatusCheckerCallback {
    private static final String logH = "ChatNow";

    private String user_id_A = MainActivity.user_id;
    private String user_id_B;
    private String name;

    private LayoutInflater inflater;
    private LinearLayout chat_area;
    private LinearLayout messageView;
    private EditText input;
    private Button send;
    private TextView message;
    private TextView status;
    private String msg;
    private ArrayList<Message> messages;
    private int indexOffset = 0;

    public static CommunicationManager myService;
    public static boolean bound = false;
    private Intent serviceIntent;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommunicationManager.MyBinder binder = (CommunicationManager.MyBinder) service;
            myService = binder.getService();
            myService.setOnNewMessageReceivedCallback(ChatNow.this);
            myService.setMidCTCallback(ChatNow.this);
            myService.setStatusCheckerCallback(ChatNow.this);
            bound = true;
            // send user_id_B to the server
            myService.setUserB(user_id_B);

            if (messages != null && messages.size()!=0) {
                Log.d(logH, "messages arraylist wasn't null or 0 sized");
                JSONObject readPacket = new JSONObject();
                try {
                    readPacket.put("userA", user_id_B);
                    readPacket.put("mid", messages.get(messages.size() - 1).getMid());
                    myService.sendReadFlag(readPacket);
                } catch (JSONException e) {
                    Log.d(logH, "Json exception occurred while making json object for setting read flag : " + e);
                }
            }
            else{
                Log.d(logH,"messages arraylist was either null or 0 in size");
            }

            Log.d(logH, "Service Bound; Selected : " + user_id_B);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(logH, "Activity Created");

        setContentView(R.layout.activity_chat_now);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // retrieving the Contact object from the intent
        final Intent intent = getIntent();
        Contact contact = (Contact) intent.getSerializableExtra("user_B");
        user_id_B = contact.getId();
        Log.d(logH, "UserB = " + user_id_B + " - " + contact.getName());
        getSupportActionBar().setTitle("Chat : " + contact.getName());
        name = contact.getName();

        inflater = getLayoutInflater();
        status = (TextView) findViewById(R.id.status);
        chat_area = (LinearLayout) findViewById(R.id.chat_area);
        input = (EditText) findViewById(R.id.input);
        send = (Button) findViewById(R.id.send);


        // binding the service
        serviceIntent = new Intent(this, CommunicationManager.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        startService(serviceIntent);

        messages = HomeActivity.db.getMessagesFromUserId(user_id_B);
        Log.d(logH, user_id_B);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = input.getText().toString();
                if(!msg.equals("")){
                    //LinearLayout wrapper = new LinearLayout(getApplicationContext());
                    messageView = (LinearLayout) inflater.inflate(R.layout.message_layout, null);
                    messageView.setBackgroundColor(Color.parseColor("#FFD0D0D0"));
                    // #FFA4A4A4 for received messages

                    ((TextView)messageView.findViewById(R.id.name)).setText("You");
                    ((TextView)messageView.findViewById(R.id.message)).setText(msg);
                    ((TextView)messageView.findViewById(R.id.created)).setText("sending");

                    //wrapper.addView(messageView);
                    chat_area.addView(messageView);

                    /*
                    message = new TextView(getApplicationContext());
                    message.setTextColor(Color.BLACK);
                    message.setTextSize(20);

                    message.setText(msg);
                    chat_area.setGravity(Gravity.LEFT);
                    chat_area.addView(message);
                    */

                    input.setText("");

                    Message message = new Message();
                    message.setIda(user_id_A);
                    message.setIdb(user_id_B);
                    message.setMsg(msg);
                    message.setName(name);
                    message.setMid("" + messages.size());

                    messages.add(message);

                    Log.d(logH, "Message To Be Sent : " + user_id_A + " : " + user_id_B + " : " + msg);
                    Log.d("specialcheck", "Send; index in arr : " + messages.size());
                    myService.sendMessage(message);
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        renderMessages(messages);
        HomeActivity.db.markAsRead(user_id_B);
    }

    // populate the listView with old chats
    public void renderMessages(ArrayList<Message> messages){
        Log.d(logH, "Number of messages to Render : " + messages.size());
        for(int i = 0; i<messages.size(); i++){
            if(messages.get(i).getIda().equals(user_id_A)){
                newMessage(messages.get(i), 0); // previously messages.get(i).getMsg() was passed
            }
            else
                newMessage(messages.get(i), 1); // same here
        }
    }

    /*
    TODO this is  useless remove
    public void renderUnread(JSONArray jsa){
        if(jsa.length() != 0)
            newMessage("" +jsa.length() + " unread messages", 2);
        try {
            for (int i = 0; i < jsa.length(); i++) {
                newMessage(jsa.getJSONObject(i).getString("message").toString(), 1);
            }
        }catch (JSONException e){Log.d("JSONexception", "when parsing message array : " + e);}
    }
*/

    // add single TextView to chatArea
    public void newMessage(Message message, int mode){
        Log.d(logH, "Message To Render : " + message.getMsg());
        messageView = (LinearLayout)inflater.inflate(R.layout.message_layout, null);
        if(mode == 1)
            messageView.setBackgroundColor(Color.parseColor("#FFA4A4A4"));
        else
            messageView.setBackgroundColor(Color.parseColor("#FFD0D0D0"));

        ((TextView)messageView.findViewById(R.id.name)).setText(name);
        ((TextView)messageView.findViewById(R.id.message)).setText(message.getMsg());
        ((TextView)messageView.findViewById(R.id.created)).setText(message.getCreated());
        chat_area.addView(messageView);

        /*
        message = new TextView(getApplicationContext());
        message.setTextColor(Color.BLACK);
        message.setTextSize(20);
        if(mode == 1)
            message.setBackgroundColor(Color.GRAY);
        else if(mode == 2)
           message.setBackgroundColor(Color.BLUE);
        message.setText(Message);
        chat_area.setGravity(Gravity.RIGHT);
        chat_area.addView(message);
        */
    }

    // the callback method
    public void addToScreen(String message, String created){
        Message msg = new Message();
        msg.setMsg(message);
        msg.setCreated(created);
        newMessage(msg, 1);
        indexOffset++;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myService.resetUserB();
        unbindService(serviceConnection);
    }

    public void updateMessage(String index, String msg_id, String created){
        Log.d(logH, " msg = " + index + " : " + msg_id + " : " + created);
        int pos = Integer.parseInt(index);
        messages.get(pos).setMid(msg_id);
        messages.get(pos).setCreated(created);
        Log.d("specialcheck", "update; index specified : " + pos + " msg = " + messages.get(pos).getMsg());

        LinearLayout wrapper = (LinearLayout) chat_area.getChildAt(pos + indexOffset);
        ((TextView)wrapper.findViewById(R.id.created)).setText(created);

    /*
        TextView msgItem = (TextView) chat_area.getChildAt(pos + indexOffset);
        String msg = msgItem.getText().toString() + "   " + created;
        msgItem.setTextSize(20);
        msgItem.setText(msg);
    */
    }

    public void updateStatus(String lastseen){
        Log.d(logH,"Updating status to " + lastseen);
        if(lastseen.equals("0"))
            status.setText("Online");
        else
            status.setText("Last Seen : " + lastseen);
    }
}
