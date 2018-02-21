package com.app.project.scaneatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.HashMap;
import scaneat.R;


public class ChangePasswordActivity extends AppCompatActivity {
    private static final String url_signup = Constants.URL_SIGN_UP;
    android.support.v7.widget.Toolbar toolbar;

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    JSONObject json = new JSONObject();

    EditText _oldPswText;
    EditText _newPswText;
    EditText _confirmNewPswText;
    Button _submitButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // SET TOOLBAR PARAMETERS
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.change_password);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        _oldPswText = (EditText)findViewById(R.id.input_old_password);
        _newPswText = (EditText)findViewById(R.id.input_new_password);
        _confirmNewPswText = (EditText)findViewById(R.id.input_confirm_new_password);
        _submitButton = (Button)findViewById(R.id.btn_submit_change_psw);


        _submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPsw = _oldPswText.getText().toString();
                String newPsw = _newPswText.getText().toString();
                String ConfNewPsw = _confirmNewPswText.getText().toString();

                //controlla il formato di immissione

                if (!validate())  {
                    onChangeFailed();

                }else {
                    if(!Utils.isOnline(ChangePasswordActivity.this))
                        Utils.displayNoConnectionAlert(ChangePasswordActivity.this,false);
                    else
                        new ChangePsw().execute(oldPsw, newPsw, ConfNewPsw);
                }
            }

        });

    }


    class ChangePsw extends AsyncTask<String,Void,Void> {
        private ProgressDialog progressDialog;
        private int success;
        private String name;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ChangePasswordActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        protected Void doInBackground(String... args) {

            String oldPsw = args[0];
            String newPsw = args[1];
            String confNewPsw = args[2];

            // Building parameters
            SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
            boolean loggin = settings.getBoolean("logged", false);
            if (!loggin) {
                onChangeFailed();
            } else {

                String email = settings.getString("email", "");
                HashMap<String, String> hashMap = new HashMap<>(2);
                hashMap.put("email", email);
                hashMap.put("oldPsw", oldPsw);
                hashMap.put("newPsw", newPsw);

                json = jsonParser.serverRequest(url_signup, hashMap);

                // check for success tag
                try {
                    success = json.getInt(TAG_SUCCESS);
                    name = json.getString(TAG_MESSAGE);
                    Log.d("Success change  ", "" + success);

                    Log.d("Message change ", "" + name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // dismiss the dialog once done

            progressDialog.dismiss();

            if (success==1) {

                /****************************
                 * CHANGE PASSWORD SUCCESFUL
                 ****************************/
                Toast.makeText(getBaseContext(), getResources().getString(R.string.change_password_ok), Toast.LENGTH_LONG).show();

            } else {
                if(success==0){
                    _oldPswText.setError("Password errata");
                }else{
                    onChangeFailed();
                }
            }
        }
    }


    public void onChangeFailed() {
        Toast.makeText(getBaseContext(), R.string.change_password_error, Toast.LENGTH_LONG).show();

        _submitButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String oldPsw = _oldPswText.getText().toString();
        String newPsw = _newPswText.getText().toString();
        String ConfNewPsw = _confirmNewPswText.getText().toString();


        if (oldPsw.isEmpty() || oldPsw.length() < 4 || oldPsw.length() > 10) {
            _oldPswText.setError("fra 4 e 10 caratteri alfanumerici");
            valid = false;
        } else {
            _oldPswText.setError(null);
        }


        if (newPsw.isEmpty() || newPsw.length() < 4 || newPsw.length() > 10) {

            _newPswText.setError("fra 4 e 10 caratteri alfanumerici");
            valid = false;
        } else {
            if(!_newPswText.getText().toString().equals(_confirmNewPswText.getText().toString())) {
                _newPswText.setError("Le password non coincidono");
                _confirmNewPswText.setError("Le password non coincidono");
            }else {
                _newPswText.setError(null);
            }
        }

        if (ConfNewPsw.isEmpty() || ConfNewPsw.length() < 4 || ConfNewPsw.length() > 10) {

            _confirmNewPswText.setError("fra 4 e 10 caratteri alfanumerici");
            valid = false;
        } else {
            if(!_newPswText.getText().toString().equals(_confirmNewPswText.getText().toString())) {
                _newPswText.setError("Le password non coincidono");
                _confirmNewPswText.setError("Le password non coincidono");
                valid = false;
            }
            else if(_newPswText.getText().toString().equals(_oldPswText.getText().toString())){
                _newPswText.setError("La password nuova coincide con quella vecchia");
                valid = false;
            }
            else {
                _confirmNewPswText.setError(null);
            }
        }

        return valid;
    }
}
