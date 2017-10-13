package com.example.trey.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Pair;
import android.os.AsyncTask;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.R.attr.id;
import static android.R.attr.label;
import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;
import static com.example.trey.myapplication.R.id.connectionStatus;

/**
 * Created by KellyErnest on 4/11/17.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();



        // set the view now
        setContentView(R.layout.activity_login);


        inputEmail = (EditText) findViewById(R.id.emailBox);
        inputPassword = (EditText) findViewById(R.id.passwordBox);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (Button) findViewById(R.id.btn_reset_password);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }


                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {

                                    sendRequestToServer();

                                }
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
            urlParamJSON.put("UserNameInfo", email);
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
        Intent intent = new Intent(LoginActivity.this, MainMenu.class);
        intent.putExtra("emailName", email);
        intent.putExtra("connectionStatus", isConnected);
        startActivity(intent);
        finish();
    }

    /**
     * This is a private class that will do something/anything asynchronously. It will perform
     * the HTTP POST request asynchronously.
     *
     * Some explanation for the way this class structure works.
     * https://androidresearch.wordpress.com/2012/03/17/understanding-asynctask-once-and-forever/
     *
     * AsyncTask< Params, Progress, Result>
     * Params will be url and url parameter
     * Result will be the result of the request from the server
     *
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