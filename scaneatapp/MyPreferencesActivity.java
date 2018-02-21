package com.app.project.scaneatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.HashMap;
import scaneat.R;

public class MyPreferencesActivity extends AppCompatActivity{ //PreferenceActivity {

    android.support.v7.widget.Toolbar toolbar;

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private SharedPreferences settings;
    JSONObject json = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        // SET TOOLBAR PARAMETERS
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });


        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MyPreferenceFragment())
                .commit();


        settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
    }

    public class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);


            addPreferencesFromResource(R.xml.preferences);

            /************************************
             *  CAMBIO DIALOG MODIFICA PASSWORD
             ***********************************/

            Preference changePsw = findPreference("prefUserPassword");

            changePsw.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    boolean loggin = settings.getBoolean("logged", false);

                    if(loggin) {
                        Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                        startActivityForResult(intent,1);
                    }else{
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyPreferencesActivity.this);
                        alertDialog.setTitle(getResources().getString(R.string.not_login));
                        alertDialog.setMessage(getResources().getString(R.string.reserved_area_login_required_message));

                        alertDialog.setPositiveButton("Login",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                                        startActivity(intent);
                                    }
                                });

                        alertDialog.setNegativeButton("Annulla",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        alertDialog.show();
                    }

                    return false;
                }

            }); // end change password

            /***************************************
             *  CAMBIO DIALOG MODIFICA NOME UTENTE
             ***************************************/

            Preference changeName = findPreference("username");

            changeName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    boolean loggin = settings.getBoolean("logged", false);
                    final String email = settings.getString("email", "");
                    final String oldName =settings.getString("name","");

                    if(loggin) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyPreferencesActivity.this);
                        alertDialog.setTitle("Nome");
                        alertDialog.setMessage("Modifica nome");

                        final EditText input = new EditText(MyPreferencesActivity.this);
                        input.setHint("Inserisci il tuo nome");
                        input.setText(oldName);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);
                        alertDialog.setView(input);
                        alertDialog.setIcon(R.drawable.ic_face_black_24dp);


                        alertDialog.setPositiveButton("Modifica",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        String newName = input.getText().toString();

                                        if(newName.isEmpty() || newName.length()<3){
                                            input.setError("Inserire un nome valido");
                                        } else{
                                            if(newName.equals(oldName)){
                                                dialog.cancel();
                                            }else {
                                                if(!Utils.isOnline(MyPreferencesActivity.this))
                                                    Utils.displayNoConnectionAlert(MyPreferencesActivity.this,false);
                                                else
                                                    new changeNameTask().execute(email, newName);
                                                dialog.cancel();
                                            }
                                        }

                                    }
                                });

                        alertDialog.setNegativeButton("Annulla",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        alertDialog.show();
                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyPreferencesActivity.this);
                        alertDialog.setTitle(getResources().getString(R.string.not_login));
                        alertDialog.setMessage(getResources().getString(R.string.reserved_area_login_required_message));

                        alertDialog.setPositiveButton("Login",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                                        startActivity(intent);
                                    }
                                });

                        alertDialog.setNegativeButton("Annulla",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        alertDialog.show();
                    }

                    return false;
                }
            }); // end change name


            /***************************
             * AGGIORNAMENTO AUTOMATICO
             ***************************/

            final SwitchPreference s = (SwitchPreference) findPreference("DBSync");
            s.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    s.setSwitchTextOn("On");
                    s.setSwitchTextOff("Off");
                    s.getSwitchTextOff();
                    s.getSwitchTextOn();

                    SharedPreferences.Editor editor = settings.edit();
                    if(s.isChecked())
                    {
                        editor.putBoolean("updateDB", true);
                        editor.apply();
                    } else {
                        editor.putBoolean("updateDB", false);
                        editor.apply();
                    }

                    return false;
                }
            }); // end AutomaticallyDBSync

            /*************
             * NOTIFICHE
             *************/

            final SwitchPreference n = (SwitchPreference) findPreference("NotifySync");
            n.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    n.setSwitchTextOn("On");
                    n.setSwitchTextOff("Off");
                    n.getSwitchTextOff();
                    n.getSwitchTextOn();

                    SharedPreferences.Editor editor = settings.edit();
                    if(n.isChecked())
                    {
                        editor.putBoolean("notify", true);
                        editor.apply();
                    } else {
                        editor.putBoolean("notify", false);
                        editor.apply();
                    }

                    return false;
                }
            }); // end Notify

        }
    }


    /*******************
     * TASK CHANGE NAME
     ******************/
    class changeNameTask extends AsyncTask<String,Void,Void> {
        private ProgressDialog progressDialog;
        private int success;
        private String message;
        private String newName;
        private String email;
        private static final String url_change_name = Constants.URL_CHANGE_NAME;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MyPreferencesActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        protected Void doInBackground(String... args) {

            email = args[0];
            newName = args[1];
            email = email.toLowerCase();
            newName = newName.substring(0,1).toUpperCase() + newName.substring(1).toLowerCase();

            // Building parameters

            HashMap<String,String> hashMap = new HashMap<>(2);
            hashMap.put("email",email);
            hashMap.put("name",newName);

            Log.d("Create Response", "params " + hashMap.toString());

            json = jsonParser.serverRequest(url_change_name,hashMap);

            // check for success tag
            try {
                success = json.getInt(TAG_SUCCESS);
                message = json.getString(TAG_MESSAGE);

                Log.d("Success change name: " , ""+success );

                Log.d("Message change name: " , ""+message );
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
                Toast.makeText(getBaseContext(), R.string.change_name_ok, Toast.LENGTH_LONG).show();

                /***********************
                 * CHANGE NAME SUCCESFUL
                 ***********************/

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("name", newName);
                editor.apply();

            } else {
                /**********************
                 *  CHANGE NAME FAILED
                 **********************/

                Log.d("Error change name","");
                Toast.makeText(getBaseContext(), R.string.change_name_err, Toast.LENGTH_LONG).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(MyPreferencesActivity.this, R.style.AppTheme_Dark_Dialog);
                builder.setTitle(getResources().getString(R.string.oops_error))
                        .setMessage(getResources().getString(R.string.change_name_err))
                        .setPositiveButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }

                        })
                        .setCancelable(true)
                        .show();
            }


        }
    }

}
