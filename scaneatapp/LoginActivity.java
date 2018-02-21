package com.app.project.scaneatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import scaneat.R;


public class LoginActivity extends AppCompatActivity {

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_TOKEN = "token";
    private JSONObject json = new JSONObject();

    android.support.v7.widget.Toolbar toolbar;

    EditText _nameText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;

    SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // SET TOOLBAR PARAMETERS
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.login);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        _nameText = (EditText)findViewById(R.id.input_name);
        _passwordText = (EditText)findViewById(R.id.input_password);
        _loginButton = (Button)findViewById(R.id.btn_login);
        _signupLink = (TextView)findViewById(R.id.link_signup);

        // Click on login-button
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = _nameText.getText().toString();
                String password = _passwordText.getText().toString();

                //check username and passwords's formats
                if (!validate())  {
                    _loginButton.setEnabled(true);

                }else {
                    if(!Utils.isOnline(LoginActivity.this))
                        Utils.displayNoConnectionAlert(LoginActivity.this,false);
                    else
                        new LoginTask().execute(email, password);
                }
            }
        });

        // Click on sign-up link
        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the SignUp activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }

    class LoginTask extends AsyncTask<String,Void,Void> {
        private ProgressDialog progressDialog;
        private int success;
        private String name;
        private String token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        protected Void doInBackground(String... args) {

            String email = args[0];
            String password = args[1];
            email = email.toLowerCase();

            // Building parameters
            HashMap<String,String> hashMap = new HashMap<>(2);
            hashMap.put("email",email);
            hashMap.put("password",password);

            json = jsonParser.serverRequest(Constants.LOGIN_URL,hashMap);

            // check for success tag
            try {
                success = json.getInt(TAG_SUCCESS);
                name=json.getString(TAG_MESSAGE);
                token = json.getString(TAG_TOKEN);
                Log.d("Success log in " , ""+success );

                Log.d("Message log in " , ""+name );
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
            protected void onPostExecute(Void result) {
                // dismiss the dialog once done
                progressDialog.dismiss();


            if (success==1) {
                Toast.makeText(getBaseContext(), R.string.login_done, Toast.LENGTH_LONG).show();

                /*******************
                 * LOGIN SUCCESFUL
                 ******************/

                settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("logged", true);
                editor.putString("email",_nameText.getText().toString().toLowerCase());
                editor.putString("name", name);
                editor.putString("token",token);
                editor.apply();

                Bundle bundle = getIntent().getExtras();

                if(getIntent().hasExtra("origin") && bundle.get("origin").toString().equals("scan")) {
                    Intent intent = new Intent(getApplicationContext(), ProposeAProduct.class);
                    intent.putExtra("barcode", getIntent().getLongExtra("barcode", 0));
                    startActivity(intent);
                }
                else if(getIntent().hasExtra("origin") && bundle.get("origin").toString().equals("modify")){
                    Intent intent = new Intent(getApplicationContext(), ProposeAModify.class);
                    intent.putExtra("barcode", getIntent().getLongExtra("barcode", 0));
                    startActivity(intent);
                }

                boolean logged = settings.getBoolean("logged",false);
                if(logged && Utils.isOnline(LoginActivity.this)){
                    onTokenRefresh();
                }

                // closing this screen
                finish();

            } else {
                /*********************
                 *  LOGIN FAILED
                 *******************/
                // ALERT ERROR LOGIN
                AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
                builder1.setTitle(R.string.login_failed);
                builder1.setMessage("Email o password errati");
                builder1.setCancelable(false);
                builder1.setPositiveButton(Html.fromHtml("<b>Registrati</b>"),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                        startActivity(intent);
                    }

                });
                builder1.setNegativeButton(Html.fromHtml("<b>Login</b>"),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close current activity
                        dialog.dismiss();
                    }

                });

                // create alert dialog
                AlertDialog alertDialog  = builder1.create();

                // show it
                alertDialog.show();

                Log.d("Error login","");
            }
        }
    }

    public boolean validate() {
        boolean valid = true;

        String email = _nameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _nameText.setError("inserisci un'email valida");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("fra 4 e 10 caratteri alfanumerici");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("onTokenRefresh", "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(final String uniqueId) {
        //getting the email entered
        final String email = settings.getString("email","");
        //Creating a string request
        StringRequest req = new StringRequest(Request.Method.POST, Constants.REGISTER_TOKEN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //if the server returned the string success
                        if (response.trim().equalsIgnoreCase("success")) {
                            //Displaying a success toast
                            //Toast.makeText(LoginActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();

                            //Opening shared preference
                            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, 0);

                            //Opening the shared preferences editor to save values
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Storing the unique id
                            editor.putString(Constants.TOKEN, uniqueId);

                            //Saving the boolean as true i.e. the device is registered
                            editor.putBoolean("logged", true);

                            //Applying the changes on sharedpreferences
                            editor.apply();

                        } else {
                            Log.d("sendRegistration","error ");
                            //Toast.makeText(LoginActivity.this, "Choose a different email", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //adding parameters to post request as we need to send firebase id and email
                params.put("token", uniqueId);
                params.put("email", email);
                Log.d("onTokenRefresh-send","Params: "+params);
                return params;
            }
        };

        //Adding the request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }
}