package com.binal.chatapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements CommunicationManager.UserIdCallback{
    private static final String logH = "MainActivity";

    public static String ad = "http://192.168.122.1:6001";
    private SharedPreferences prefs;
    public static String user_id;
    public static String username;
    public static CommunicationManager myService;
    public static boolean bound = false;
    private Intent HomeActivityIntent;
    private Intent serviceIntent;
/*
    private Socket socket;
    {
        try {
            socket = IO.socket("http://192.168.1.10:6001");
        }catch (URISyntaxException e){
            Log.d("SocketIO","URISyntax Exception : e : "+ e);
        }
    }
*/

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommunicationManager.MyBinder binder = (CommunicationManager.MyBinder)service;
            myService = binder.getService();
            bound = true;
            myService.setUserIdCallback(MainActivity.this);
            Log.d(logH, "Service bound, Callback set");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(logH,"Activity Created");

        HomeActivityIntent = new Intent(this, HomeActivity.class);

        // check if client is registered ie user id and username is present
        // if true, the user id and username is stored in the static variables user_id and username
        // for use in other activities and redirect to HomeActivity
        prefs = getSharedPreferences("config", Activity.MODE_PRIVATE); //TODO change config to profile?

        if(prefs.getString("address",null) !=null){
            ad = prefs.getString("address",null);
        }

        if(prefs.getString("user_id_A", null) != null){
            user_id = prefs.getString("user_id_A","def");
            username = prefs.getString("username", null);
            CommunicationManager.registered = true;
            Log.d(logH , "Activity User id [ " + user_id + " ]  and Username [ " + username + " ] present; skipping registration");
            startActivity(HomeActivityIntent);
            finish();
        }
        else{
            // user_id isn't present but username is present => user_id wasn't returned or
            // network transaction wasn't done
            /*TODO
            if(prefs.getString("username", null)!=null){

            }*/
        }


        setContentView(R.layout.activity_main);
/*
  these have been moved to the service definition
        socket.on("userid", listener);
        socket.connect();
*/
        // if not registered
        // then username is obtained, sent to server using the service and user_id returned is stored
        serviceIntent = new Intent(getApplicationContext(), CommunicationManager.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        startService(serviceIntent);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText editText = (EditText) findViewById(R.id.username);
        final Button button = (Button) findViewById(R.id.signup);
        assert button != null;

        // when button is clicked the username is obtained
        // and sent to server using bound object for the service
        //TODO check for SQL injection
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = editText.getText().toString();
                /*
                TextView notif = (TextView) findViewById(R.id.notif);
                notif.setText("Processing..." + username);
                */
                Log.d(logH , "Activity Registration Username : " + username);

                // check for connection
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    //Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();
                    Snackbar.make(button, "Registering... ", Snackbar.LENGTH_SHORT).show();
                    //socket.emit("registration", username);

                    // the username is passed to the service which will send to server
                    myService.register(username);
                    prefs.edit().putString("username", username).commit();
                } else
                    Toast.makeText(MainActivity.this, "No Network Available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // this is the call back function that is called by the implemented CommunicationManager.OnUserIdCallback interface
    // the interface calls this when a user_id is returned from the server
    public void setUserId(String userid){
        // prefs.edit().putString("user_id_A",userid).commit();
        user_id = userid;
        // TODO remove in real application
        Toast.makeText(this, "user id : "+userid, Toast.LENGTH_SHORT).show();

        startActivity(HomeActivityIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //socket.disconnect();
        if(bound && myService!=null) {
            unbindService(serviceConnection);
        }
    }

}
