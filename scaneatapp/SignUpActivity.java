package com.app.project.scaneatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.Firebase;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import scaneat.R;


public class SignUpActivity extends AppCompatActivity {

    private static final int REQUEST_SIGNUP = 0;
    android.support.v7.widget.Toolbar toolbar;

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    JSONObject json = new JSONObject();


    EditText _nameText;
    EditText _emailText;
    EditText _passwordText;
    EditText _confirmPasswordText;
    Button _signupButton;
    TextView _loginLink;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // SET TOOLBAR PARAMETERS
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.registration);
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
        _emailText = (EditText)findViewById(R.id.input_email);
        _passwordText = (EditText)findViewById(R.id.input_password);
        _confirmPasswordText = (EditText) findViewById(R.id.input_confirm_password);
        _signupButton = (Button)findViewById(R.id.btn_signup);
        _loginLink = (TextView)findViewById(R.id.link_login);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = _nameText.getText().toString();
                String email = _emailText.getText().toString();
                String password = _passwordText.getText().toString();
                String confirmPassword =_confirmPasswordText.getText().toString();
                String token = getFirebaseToken();

                //formattazione
                email.toLowerCase();
                name = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();

                //controlla il formato di immissione

                if (!validate())  {
                    _signupButton.setEnabled(true);

                }else {
                    if(!Utils.isOnline(SignUpActivity.this))
                        Utils.displayNoConnectionAlert(SignUpActivity.this,false);
                    else
                        new SignupTask().execute(name, email, password, token);
                }
            }

        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the SignUp activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }


    class SignupTask extends AsyncTask<String,Void,Void> {
        private ProgressDialog progressDialog;
        private int success;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        protected Void doInBackground(String... args) {

            String name = args[0];
            String email = args[1];
            String password = args[2];
            String token = args[3];

            // Building parameters

            HashMap<String,String> hashMap = new HashMap<>(3);
            hashMap.put("nome",name);
            hashMap.put("email",email);
            hashMap.put("password",password);
            hashMap.put("token",token);

            json = jsonParser.serverRequest(Constants.REGISTER_URL,hashMap);

            // check for success tag
            try {
                success = json.getInt(TAG_SUCCESS);

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

                /*******************
                 * SIGN UP SUCCESFUL
                 ******************/
                Toast.makeText(getBaseContext(), getResources().getString(R.string.account_created), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

                // closing this screen
                finish();
            } else{
                if(success==2) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
                    builder.setTitle(getResources().getString(R.string.registration_error))
                            .setMessage(getResources().getString(R.string.already_registered))
                            .setPositiveButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }

                            })
                            .setCancelable(true)
                            .show();
                }else {
                    /*********************
                     *  SIGN UP FAILED
                     *******************/
                    // ALERT ERROR SIGNUP
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
                    builder1.setTitle(getResources().getString(R.string.registration_error));
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(getResources().getString(R.string.try_again), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.dismiss();
                        }

                    });

                    // create alert dialog
                    AlertDialog alertDialog = builder1.create();

                    // show it
                    alertDialog.show();

                    //Toast.makeText(getBaseContext(), getResources().getString(R.string.registration_failed), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean validate() {
        boolean valid = true;

        String _passwordText = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String newPsw = this._passwordText.getText().toString();
        String confNewPsw = _confirmPasswordText.getText().toString();


        //controlli nome
        if (_passwordText.isEmpty() || _passwordText.length() < 3) {
            _nameText.setError("almeno 3 caratteri");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        // controlli email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("inserisci un indirizzo email valido");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        //controlli password
        if (newPsw.isEmpty() || newPsw.length() < 4 || newPsw.length() > 10) {

            this._passwordText.setError("fra 4 e 10 caratteri alfanumerici");
            valid = false;
        } else {
            if(!this._passwordText.getText().toString().equals(_confirmPasswordText.getText().toString())) {
                this._passwordText.setError("Le password non coincidono");
                _confirmPasswordText.setError("Le password non coincidono");
            }else {
                this._passwordText.setError(null);
            }
        }

        if (confNewPsw.isEmpty() || confNewPsw.length() < 4 || confNewPsw.length() > 10) {

            _confirmPasswordText.setError("fra 4 e 10 caratteri alfanumerici");
            valid = false;
        } else {
            if(!this._passwordText.getText().toString().equals(_confirmPasswordText.getText().toString())) {
                this._passwordText.setError("Le password non coincidono");
                _confirmPasswordText.setError("Le password non coincidono");
                valid = false;
            } else {
                _confirmPasswordText.setError(null);
            }
        }

        return valid;
    }

    private String getFirebaseToken() {
        //Creating a firebase object
        Firebase firebase = new Firebase(Constants.FIREBASE_APP);

        //Pushing a new element to firebase it will automatically create a unique id
        Firebase newFirebase = firebase.push();

        //Creating a map to store name value pair
        Map<String, String> val = new HashMap<>();

        //pushing msg = none in the map
        val.put("msg", "none");

        //saving the map to firebase
        newFirebase.setValue(val);

        //Getting the unique id generated at firebase
        String token = newFirebase.getKey();

        return  token;
    }
}