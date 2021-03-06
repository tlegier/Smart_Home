package com.example.trey.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by KellyErnest on 4/11/17.
 */

public class MainMenu extends AppCompatActivity {
    Spinner OnLineList;
    Spinner OffLineList;
    ArrayAdapter<CharSequence> OnlineAdapter;
    ArrayAdapter<CharSequence> OfflineAdapter;
    Button Settings;
    String userName = "Admin";
    String isConnected = "";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();
        userName = extras.getString("emailName");

            isConnected = extras.getString("connectionStatus");


        Button Refresh = (Button) findViewById(R.id.refreshButton);
        TextView ConnectionStatus = (TextView) findViewById(R.id.connectionStatus);
        ConnectionStatus.setText(isConnected);
        ConnectionStatus.setTextColor(Color.GREEN);
        TextView UserLabel = (TextView) findViewById(R.id.userLabel);
        UserLabel.setText(userName);
        Settings = (Button) findViewById(R.id.Settingsbutton);
        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequestToServer();

            }


        });
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), Settings.class);
                intent.putExtra("UserName", userName);
                startActivityForResult(intent, 0);


            }


        });

        OnLineList = (Spinner) findViewById(R.id.OnlineDeviceList);
        OnlineAdapter = ArrayAdapter.createFromResource(this, R.array.OnlineDevices, android.R.layout.simple_dropdown_item_1line);
        OnlineAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        OnLineList.setAdapter(OnlineAdapter);


        OffLineList = (Spinner) findViewById(R.id.OfflineDeviceList);
        OfflineAdapter = ArrayAdapter.createFromResource(this, R.array.OfflineDevices, android.R.layout.simple_dropdown_item_1line);
        OfflineAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        OffLineList.setAdapter(OfflineAdapter);

        OnLineList.post(new Runnable() {
            @Override
            public void run() {

                OnLineList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override

                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        Intent intent = new Intent(view.getContext(), DeviceType.class);
                        String label = OnLineList.getSelectedItem().toString();
                        intent.putExtra("DeviceLabel", label);
                        intent.putExtra("UserName", userName);
                        startActivityForResult(intent, 0);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }
        });


    }

    protected void sendRequestToServer() {

        // Instantiate a JSON Object.
        JSONObject urlParamJSON = new JSONObject();

        try {
            // Put the Base64 string into the JSON Object.
            urlParamJSON.put("requestType", "Query");
            urlParamJSON.put("UserNameInfo", userName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            // Construct the URL where the image will be sent.
            String url = "http://192.168.43.155:80";
            // Execute the Async process that will perform the actual HTTP POST Request.
            new EndpointsAsyncTask().execute(new Pair<String, String>(url, urlParamJSON.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets called when the server responds to the HTTP Request or when there's an error.
     *
     * @param response
     */
    protected void httpPOSTRequestCallback(String response) {
        String isConnected = "";
        if (response.equals("Connected")) {
            isConnected = "Connected";
        } else {
            isConnected = "Disconnected";
        }
        Intent intent = new Intent(MainMenu.this, MainMenu.class);
        intent.putExtra("emailName", userName);
        intent.putExtra("connectionStatus", isConnected);
        startActivity(intent);
        finish();
    }

    /**
     * This is a private class that will do something/anything asynchronously. It will perform
     * the HTTP POST request asynchronously.
     * <p>
     * Some explanation for the way this class structure works.
     * https://androidresearch.wordpress.com/2012/03/17/understanding-asynctask-once-and-forever/
     * <p>
     * AsyncTask< Params, Progress, Result>
     * Params will be url and url parameter
     * Result will be the result of the request from the server
     */
    private class EndpointsAsyncTask extends AsyncTask<Pair<String, String>, Void, String> {


        protected String doInBackground(Pair<String, String>... params) {
            // Everything in this function is performed in the background/asynchronously.

            String url = params[0].first;
            String urlParams = params[0].second;
            return executePost(url, urlParams);
        }

        @Override
        protected void onPostExecute(String result) {
            // The word "post" in onPostExecute has nothing to do with the HTTP POST request. It
            // just means that this function is called post/after execution of the async task.
            httpPOSTRequestCallback(result);
        }

        /**
         * A helper function to do the ACTUAL HTTP POST request.
         * If you DO NOT know how HTTP Requests, especially POST request are sent over a network,
         * stop now and go learn. Otherwise, this code will look like gibberish.
         *
         * @param targetURL
         * @param urlParameters
         * @return HTTP Post server response
         */
        protected String executePost(String targetURL, String urlParameters) {
            HttpURLConnection connection = null;
            String isConnected = "";
            try {
                // Create connection with the target URL.
                URL url = new URL(targetURL);

                // Open the connection and configure the connection the way a HTTP POST connection
                // that communicates via JSON should be configured.
                // Notice that we didn't specify a timeout. There's a default timeout (idk what it
                // is) but i didn't specify a timeout because who knows how long it'll take Howard's
                // network to send this request.
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/json; charset=UTF-8");
                connection.setRequestProperty("Content-Length",
                        Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setUseCaches(false);

                // Create a stream with the server where data can be sent.
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                // Send the url parameters as data.
                wr.writeBytes(urlParameters);
                // Close the stream.
                wr.close();

                // Create a stream with the server where data can be received.
                InputStream is;
                // Get the response code of the POST request. 400 is error. 200 is OK.
                int status = connection.getResponseCode();
                if (status >= 400) {
                    // This call will block. In other words, the JVM will pause execution of this
                    // app until this function finishes returning the error stream.
                    is = connection.getErrorStream();

                } else {
                    // This call will block. In other words, the JVM will pause execution of this
                    // app until this function finishes returning the input stream.
                    is = connection.getInputStream();

                }

                // Once either an error stream or input stream has been received, it the actual
                // contents of the stream need to be parsed via a BufferedReader.
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                // StringBuilder will be used to, well, build a string out of the input stream data.
                StringBuilder response = new StringBuilder();
                String line;
                // While the buffered reader is able to read data from the InputStream
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\n');
                }
                // Close BufferedReader.
                rd.close();

                // Return the built string.
                return "Connected";
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return null;
        }
    }
}
