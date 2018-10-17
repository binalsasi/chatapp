package com.binal.chatapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements CommunicationManager.UpdateChatHistoryCallback {
    private static final String logH = "HomeActivity";

    public static DBHelper db;
    ArrayList<ChatHistoryItem> items;
    ArrayList<Contact> contacts;
    ListView history;


    public static CommunicationManager myService;
    public static boolean bound = false;
    private Intent serviceIntent;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommunicationManager.MyBinder binder = (CommunicationManager.MyBinder)service;
            myService = binder.getService();
            myService.setUpdateChatHistoryCallback(HomeActivity.this);
            myService.cancelNotification();
            bound = true;
            myService.setUserB("home");
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

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serviceIntent = new Intent(getApplicationContext(), CommunicationManager.class);
        startService(serviceIntent);
        bindService(serviceIntent,serviceConnection, BIND_AUTO_CREATE);

        // initiating the DB helper
        db = new DBHelper(this);

        // populating the list
        //items = db.getChatHistory();

        history = (ListView) findViewById(R.id.chat_history);
        //history.setAdapter(new HomeActivityChatListAdapter(this, items));
        history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(HomeActivity.this, items.get(position).getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, ChatNow.class);
                Contact contact = new Contact();
                Log.d(logH , "ChatHistory List item selected : " + items.get(position).getIdb() + " - " + items.get(position).getName());
                contact.setId(items.get(position).getIdb());
                contact.setName(items.get(position).getName());
                intent.putExtra("user_B", contact);
                startActivity(intent);
            }
        });

        //final Intent intent = new Intent(this, CommunicationManager.class);
        //bindService(intent,serviceConnection, BIND_AUTO_CREATE);
        //startService(intent);

    }

    @Override
    public void onResume(){
        super.onResume();

        // fetching all the contacts
        // this is currently done in onResume because when user has added a friend using the add friend
        // activity, this list should also be updated
        // in reality this will depend on the choice of design
        contacts = db.getAllContacts();
        items = db.getChatHistory();
        history.setAdapter(new HomeActivityChatListAdapter(this, items));
    }

    public void updateHistory(ChatHistoryItem item){
        boolean flag = false;
        for(int i = 0; i<items.size(); i++){
            if(items.get(i).getIdb().equals(item.getIdb())){
                item.setUnreads(items.get(i).getUnreads() + 1);
                items.remove(i);
                break;
            }
        }
        items.add(0, item);

        history.setAdapter(new HomeActivityChatListAdapter(this, items));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // to change address of communication
        if(id == R.id.setAdd){
            startActivity(new Intent(this, IpSetter.class));
        }

        // if users has clicked the Show Friends option from the action overflow
        if (id == R.id.show_friends) {
            if(contacts == null){
                Toast.makeText(HomeActivity.this,"Uh Oh! You Don't have any friends yet. Click 'Add Friends' to Add them",Toast.LENGTH_SHORT).show();
            }
            else{
                Intent intent = new Intent(HomeActivity.this, TestActivity.class);
                //intent.putExtra("contacts",contacts);
                startActivity(intent);
            }
            return true;
        }

        // if the user has clicked the Add Friends option from the action overflow
        if (id == R.id.add_friends) {
            Intent intent = new Intent(HomeActivity.this, AddFriends.class);
            intent.putExtra("contacts",contacts);
            startActivity(intent);
            return true;
        }

        // if the user has clicked the Chat Now option from the action overflow
        if (id == R.id.chatnow) {
            if(contacts == null){
                Toast.makeText(HomeActivity.this,"Uh Oh! You Don't have any friends yet. Click 'Add Friends' to Add them",Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(HomeActivity.this, FriendsList.class);
                intent.putExtra("mode", true);
                intent.putExtra("contacts", contacts);
                startActivity(intent);
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        myService.resetUserB();
        unbindService(serviceConnection);
    }
}
