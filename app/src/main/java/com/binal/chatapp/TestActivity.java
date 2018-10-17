package com.binal.chatapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import junit.framework.Test;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class TestActivity extends AppCompatActivity implements CommunicationManager.TestCallback{

    Button get;
    TextView res;


    public static CommunicationManager myService;
    public static boolean bound = false;
    private Intent serviceIntent;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommunicationManager.MyBinder binder = (CommunicationManager.MyBinder)service;
            myService = binder.getService();
            myService.setTestCallback(TestActivity.this);
            bound = true;
            // send user_id_B to the server

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        get = (Button) findViewById(R.id.get);
        res = (TextView) findViewById(R.id.texxt);

        serviceIntent = new Intent(this, CommunicationManager.class);
        bindService(serviceIntent,serviceConnection,BIND_AUTO_CREATE);

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.getTestMessage();
            }
        });

    }

    public void putUp(JSONObject message){
        try {
            String msg = "" + message.getString("userA") + " : " + message.getString("userB") + " : " + message.getString("message") + " : " + message.getString("created");
            res.setText(msg);
        }catch (JSONException e){
            Log.d("As","asas " + e);
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        unbindService(serviceConnection);
    }

}
