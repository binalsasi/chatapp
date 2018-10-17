package com.binal.chatapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;

public class IpSetter extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_setter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText ip = (EditText) findViewById(R.id.ip), port = (EditText) findViewById(R.id.port);
        Button confirm = (Button) findViewById(R.id.confirm);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String byte4 = ip.getText().toString();
                String portn = port.getText().toString();
                if(!byte4.equals("")){
                    if(portn.equals(""))
                        portn = "6001";
                    String ipA = "http://192.168.1." + byte4 + ":" + portn;
                    SharedPreferences prefs = getSharedPreferences("config", Activity.MODE_PRIVATE);
                    prefs.edit().putString("address", ipA).commit();
                    Snackbar.make(v,"Please Restart For Effect" , Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

}
