package com.binal.chatapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * This Service will handle all the network operations
 * and some database functions
 * Created by binal on 27/7/16.
 */
public class CommunicationManager extends Service {
    private static final String logH = "CommunicationManager";

    private NotificationManager mNotificationManager;
    private static int numMessages = 0;
    private static int notificationID = 100;

    private MyBinder myBinder = new MyBinder();
    public static boolean registered;
    private static String user_id;
    private DBHelper database;
    private String chatSelected = "0";
    private JSONObject message;
    private Handler statusPoll;
    private Thread statusP;
    public static boolean runStatusChecker = false;

    private ArrayList<Message> midCtPendingMessages;
    private int indexOffset = 0;

    private UserIdCallback userIdCallback;
    private UpdateChatHistoryCallback updateChatHistory;
    private MidCTCallback midCTCallback;
    private OnNewMessageReceivedCallback onNewMessageReceivedCallback;
    private StatusCheckerCallback statusCheckerCallback;

    // initializing the socket
    private Socket socket;
    {
        try{
            socket = IO.socket(MainActivity.ad);
            Log.d(logH, "SocketIO initialized");
        }catch (URISyntaxException e){
            Log.d(logH,"SocketIO URISyntaxException : " + e);
        }
    }

    // listener for receiving the user id of the client from the server
    // should be removed from the socket after registration
    private Emitter.Listener userIdListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
            //Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    user_id = String.valueOf(args[0]);
                    Log.d(logH, "UserID obtained : " + user_id);
                    // TODO handle the problems that could be caused when no userId is returned
                    if(user_id!=null && !user_id.equals("")) {
                        registered = true;
                        // already performed in MainActivity.setUserId()
                        SharedPreferences prefs = getSharedPreferences("config", Activity.MODE_PRIVATE);
                        prefs.edit().putString("user_id_A", user_id).commit();
                        userIdCallback.setUserId(user_id);
                        database = new DBHelper(getApplicationContext());
                    }
                }
            });
            //thread.start();
        }
    };

    // this listener listens for incoming new Messages
    // it then either populate the chat area (if the user is in the chat area) or not
    // it adds the message to database in either case
    private Emitter.Listener newMessageListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        Log.d(logH, "Selected Chat : " + chatSelected + "; userA of received Message : " + object.getString("userA"));
                        Log.d(logH, "Message received : " + object.getString("message") + " : " + object.getString("userA") + " : " + object.getString("userB"));
                        String name = database.getContactName(object.getString("userA"));
                        if(chatSelected.equals(object.getString("userA"))){
                            onNewMessageReceivedCallback.addToScreen(object.getString("message"), object.getString("created"));
                            database.addMsg(object.getString("mid"), object.getString("userA"), object.getString("userB"), name, object.getString("message"), object.getString("created"), true);
                        }
                        else{
                            database.addMsg(object.getString("mid"), object.getString("userA"), object.getString("userB"), name, object.getString("message"), object.getString("created"), false);
                            //TODO notify the user
                            //NotifyNewMessage(object.getString("userA"), name, object.getString("message"));
                            if(!chatSelected.equals("home"))
                                NotifyUser();
                            Log.d(logH, "notifying user");
                        }

                        // TODO its best to do this for case where chatSelected = 0
                        if(updateChatHistory != null) {
                            ChatHistoryItem item = new ChatHistoryItem();
                            item.setIdb(object.getString("userA"));
                            item.setName(name);
                            item.setMsg(object.getString("message"));
                            //item.setCreated();

                            updateChatHistory.updateHistory(item);
                        }
                    } catch (JSONException e) {
                        Log.d(logH, " JSON exception When getting : " + e);
                    }
                }
            });
        }
    };

    // this listener listens for msg id and created time of the message sent by the client
    // this is then added to database
    // TODO update the message in chat area if possible
    private Emitter.Listener midctListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try{
                        int index = object.getInt("index") - indexOffset;
                        Log.d(logH, "MIDCT obtained : " + object.getString("mid") +"  " +object.getString("created") + "   " + index);
                        Log.d(logH,"MidCt pendingmessages sie = " + midCtPendingMessages.size());
                        database.addMsg(object.getString("mid"), midCtPendingMessages.get(index).getIda(), midCtPendingMessages.get(index).getIdb(), midCtPendingMessages.get(object.getInt("index")).getName(), midCtPendingMessages.get(object.getInt("index")).getMsg(), object.getString("created"), true);
                        if(chatSelected.equals(midCtPendingMessages.get(index).getIdb())){
                            midCTCallback.updateMessage(midCtPendingMessages.get(index).getMid(), object.getString("mid"), object.getString("created"));
                        }
                        Log.d(logH,"midctcheckpoint 4?  " + midCtPendingMessages.size());

                        midCtPendingMessages.remove(index);
                        indexOffset++;
                        if(midCtPendingMessages.size()==0){
                            indexOffset = 0;
                        }
                    }catch (JSONException e){
                        Log.d(logH , "JSON Exception : when retreiving Mid and Ct : " + e);
                    }
                }
            });
            Log.d(logH, "midctcheckpoint  3 " + midCtPendingMessages.size());

        }
    };

    private Emitter.Listener unReadListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Log.d(logH,"Unreads listener fired");

                    JSONArray msgs = (JSONArray) args[0];
                    try {
                        Log.d(logH, "Number of unreads = " + msgs.length());

                        for (int i = 0; i < msgs.length(); i++) {
                            JSONObject object = msgs.getJSONObject(i);
                            Log.d(logH, " Unread Message " + i + "  " + object.getString("message"));
                            if(chatSelected.equals("home")){
                                database.addMsg(object.getString("msg_id"), object.getString("user_id_A"), object.getString("user_id_B"), database.getContactName(object.getString("user_id_A")), object.getString("message"), object.getString("created"), false);
                                if(updateChatHistory != null){
                                    ChatHistoryItem item = new ChatHistoryItem();
                                    item.setIdb(object.getString("user_id_A"));
                                    item.setName(database.getContactName(object.getString("user_id_A")));
                                    item.setMsg(object.getString("message"));
                                    updateChatHistory.updateHistory(item);
                                }
                            }
                            else{
                                if(chatSelected.equals(object.getString("user_id_A"))){
                                    Log.d(logH, " chat selected thus message rendered...");

                                    onNewMessageReceivedCallback.addToScreen(object.getString("message"), object.getString("created"));
                                    database.addMsg(object.getString("msg_id"), object.getString("user_id_A"), object.getString("user_id_B"), database.getContactName(object.getString("user_id_A")), object.getString("message"), object.getString("created"), true);
                                    NotifyUser();
                                }
                                else{
                                    Log.d(logH, "no chat selected thus message not rendered...");
                                    database.addMsg(object.getString("msg_id"), object.getString("user_id_A"), object.getString("user_id_B"), database.getContactName(object.getString("user_id_A")), object.getString("message"), object.getString("created"), false);
                                    NotifyUser();
                                }
                            }
                        }

                    }catch (JSONException e){
                        Log.d(logH, "JSON exception in UnreadListener : " + e);
                    }
                }
            });
        }
    };

    Emitter.Listener statusListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String lastseen = (String) args[0];
                    statusCheckerCallback.updateStatus(lastseen);
                }
            });
        }
    };

    public class MyBinder extends Binder{
        public CommunicationManager getService(){
            return CommunicationManager.this;
        }
    }

    public interface UserIdCallback{
        void setUserId(String user_id);
    }
    public interface UpdateChatHistoryCallback{
        void updateHistory(ChatHistoryItem item);
    }
    public interface MidCTCallback{
        void updateMessage(String index, String msg_id, String created);
    }
    public interface OnNewMessageReceivedCallback{
        void addToScreen(String message, String created);
    }
    public interface StatusCheckerCallback{
        void updateStatus(String lastseen);
    }

    public void setMidCTCallback(MidCTCallback callback){
        midCTCallback = callback;
    }
    public void setUpdateChatHistoryCallback(UpdateChatHistoryCallback callback){
        updateChatHistory = callback;
    }
    public void setOnNewMessageReceivedCallback(OnNewMessageReceivedCallback callback){
        onNewMessageReceivedCallback = callback;
    }
    public void setUserIdCallback(UserIdCallback callback){
        userIdCallback = callback;
    }
    public void setStatusCheckerCallback(StatusCheckerCallback callback){
        statusCheckerCallback = callback;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        if(midCtPendingMessages == null)
            midCtPendingMessages = new ArrayList<>();
        Log.d(logH,"midctcheckpoint  2 " + midCtPendingMessages.size());
        if(MainActivity.user_id != null)
            user_id = MainActivity.user_id;
        if(registered) {
            database = new DBHelper(getApplicationContext());
        }
        socket.on("userid", userIdListener);
        socket.on("midct", midctListener);
        socket.on("unread", unReadListener);
        socket.on("newMessage", newMessageListener);
        socket.on("testMessage", testListener);
        socket.on("status", statusListener);
        socket.connect();
        Log.d(logH, "Service Created : " + user_id + " check : " + MainActivity.user_id);
        socket.emit("user_id", user_id);
        if(registered) {
            Log.d(logH,"emitting unread event trigger");
            socket.emit("getMsgs", user_id);
        }
        statusP = new Thread(new Runnable() {
            @Override
            public void run() {
                while(runStatusChecker){
                    socket.emit("getStatus", chatSelected);
                        try {
                            Thread.currentThread().sleep(10000);
                        }catch (InterruptedException e){
                            Log.d(logH, " Status Checker  Interrupted exception : " + e);
                        }
                }
            }
        });

        Log.d(logH,"midctcheckpoint  3 " + midCtPendingMessages.size());
    }

    @Override
    public IBinder onBind(Intent arg0){
        return myBinder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(logH,"SocketIO Service Started");
        if(registered){
            //TODO in the future there might happen an error due to the user_id being lost, so it might be good to check if user id is present in the shared preferences
            socket.off("userid", userIdListener);
        }
        return START_STICKY;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(logH, "SocketIO Service Stopped : " + user_id + " check : " + MainActivity.user_id);
        socket.disconnect();
    }


    public void register(String username){
        socket.emit("registration", username);
    }

    public boolean isConnected(){
        if(socket.connected())
            return true;
        else
            return false;
    }

    public void setUserB(String user_id_B){
        //socket.emit("userB", user_id_B);
        chatSelected = user_id_B;
        Log.d(logH, "SetUserB called : " + user_id_B);

        if(!(user_id_B.equals("home")||user_id_B.equals("0")))
            checkStatus(true);

    }
    public void resetUserB(){
        //socket.emit("userB", "0");
        chatSelected = "0";
        Log.d(logH, "UserB reset");

        checkStatus(false);
    }

    public void sendMessage(Message msg){
        midCtPendingMessages.add(msg);
        Log.d(logH, "MidCtpending messages size after adding = " + midCtPendingMessages.size());

        JSONObject message = new JSONObject();
        try {
            message.put("userA", msg.getIda());
            message.put("userB", msg.getIdb());
            message.put("message", msg.getMsg());
            message.put("index", (midCtPendingMessages.indexOf(msg) + indexOffset));
        }catch (JSONException e){
            Log.d("JSON Exception","When sending : "+ e);
        }

        Log.d(logH,"midctcheckpoint  1 " + midCtPendingMessages.size());

        socket.emit("m2i", message);
        /*
        this.message = message;

        try {
            Log.d(logH, "Message To Be Sent : " + message.getString("userA") + " : " + message.getString("userB") + " : " + message.getString("message"));
            database.addMsg("1", message.getString("userA"), message.getString("userB"), database.getContactName(message.getString("userB")), message.getString("message"), "2", true);
        }catch (JSONException e){
            Log.d(logH, "JSON Exception While Adding Sent Message to DB +" + e);}
            */
    }

    public void NotifyNewMessage(String uid, String name, String message){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setContentTitle("New Message");
        mBuilder.setSmallIcon(R.drawable.ic_new_msg_notif);
        mBuilder.setContentText("You've received new message." + message);
        mBuilder.setTicker("New Message Alert!");
//        mBuilder.setSmallIcon(R.drawable.woman);
/* Increase notification number every time a new notification arrives */
        mBuilder.setNumber(++numMessages);

        Contact contact = new Contact();
        contact.setId(uid);
        contact.setName(name);
    /* Creates an explicit intent for an Activity in your app */
        Intent resultIntent = new Intent(this, ChatNow.class);
        resultIntent.putExtra("user_B", contact);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatNow.class);
/* Adds the Intent that starts the Activity to the top of the stack */
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
/* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(notificationID, mBuilder.build());
        Log.d(logH, "End of notif function");
    }
    public void NotifyUser(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setContentTitle("New Message");
        mBuilder.setSmallIcon(R.drawable.ic_new_msg_notif);
        mBuilder.setContentText("You've received new message.");
        mBuilder.setTicker("New Message Alert!");
//        mBuilder.setSmallIcon(R.drawable.woman);
/* Increase notification number every time a new notification arrives */
        mBuilder.setNumber(++numMessages);

    /* Creates an explicit intent for an Activity in your app */
        Intent resultIntent = new Intent(this, HomeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(HomeActivity.class);
/* Adds the Intent that starts the Activity to the top of the stack */
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
/* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(notificationID, mBuilder.build());
        Log.d(logH, "End of notif function");

    }
    public void cancelNotification() {
        Log.i("Cancel", "notification");
        if(mNotificationManager!=null)
            mNotificationManager.cancel(notificationID);
    }

    public void checkStatus(boolean run){
        Log.d(logH,"checkStatus() called; run = " + run);
        runStatusChecker = run;
        if(run){
            if(statusP.isInterrupted())
                statusP.run();
            else
                statusP.start();
          /*  statusPoll = new Handler();
            statusPoll.post(new Runnable() {
                @Override
                public void run() {
                    while(runStatusChecker){
                        socket.emit("getStatus", chatSelected);
                        try {
                            Thread.currentThread().sleep(10000);
                        }catch (InterruptedException e){
                            Log.d(logH, " Status Checker  Interrupted exception : " + e);
                        }
                    }
                }
            });*/
        }
        else{
            //statusPoll = null;
            //statusP.stop();
            //TODO find how to stop a thread!
            statusP.interrupt();
        }
    }

    public void sendReadFlag(JSONObject userB_mid){
        socket.emit("HaveRead", userB_mid);
    }







    public void getTestMessage(){
        socket.emit("getTestMessage");
    }
    public Emitter.Listener testListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                JSONObject object = (JSONObject) args[0];
                testCallback.putUp(object);
            }
        });
        }
    };
    public TestCallback testCallback;
    public interface TestCallback{
        void putUp(JSONObject a);
    }
    public void setTestCallback(TestCallback callback){
        testCallback = callback;
    }
}
