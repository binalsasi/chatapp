/*
package com.binal.chatapp;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by binal on 6/6/16.

public class registration_task extends AsyncTask<String,Void,String> {
    int user_id = 0;

    MainActivity activity;

    public registration_task(MainActivity activity){
        super();
        this.activity = activity;
    }
    @Override
    protected String doInBackground(String... params){
        OutputStream os = null;
        InputStream is = null;
        try{
            URL url = new URL(params[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            os = conn.getOutputStream();

            //   HashMap<String,String> params = new HashMap<String,String>(1);
            //   params.put("content",myurl[1]);

            //   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

            String username;
            StringBuilder builder = new StringBuilder();
            builder.append(URLEncoder.encode("name", "UTF-8"));
            builder.append("=");
            builder.append(URLEncoder.encode(params[1], "UTF-8"));
            username = builder.toString();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            writer.write(username);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();

            int responseCode = conn.getResponseCode();
            Log.d("chatapp_registration", "Response Code = " + responseCode);

            is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String u_id = br.readLine();
            br.close();

            is.close();
            conn.disconnect();
            return u_id;
        }catch (MalformedURLException e){
            Log.d("Registration","MalformedURLException = " +e);
            return "malformed url : " +e;
        }
        catch (IOException e){
            Log.d("Registration","IOException = " +e);
            return "IOE : " + e;
        }
    }

    @Override
    public void onPostExecute(String uid){
        user_id = Integer.parseInt(uid);
        //Toast.makeText(activity,"UserID : " + uid,Toast.LENGTH_SHORT).show();
        activity.setUserID(user_id);
        Log.d("Registration", "User ID : " + user_id);
    }
}

*/