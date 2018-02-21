package com.app.project.scaneatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.HashMap;
import scaneat.R;

public class ProposeAProduct extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText prodName;
    private EditText prodBrand;
    private TextView prodBarcode;
    private String barcode; // the string that will be sent via json
    private EditText prodIngredients;
    private CheckBox prodNoGluten;
    private CheckBox prodNoLactose;
    private CheckBox prodVegan;
    private Button send;

    private final static String url = Constants.PROPOSE_A_PRODUCT_URL;
    private HashMap<String, String> toBeSent = new HashMap<>(8);
    private SharedPreferences settings;
    private String userMail;


    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);

        setContentView(R.layout.activity_propose_product);

        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.propose_product_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        prodName = (EditText) findViewById(R.id.propose_product_name);
        prodBrand = (EditText) findViewById(R.id.propose_product_brand);
        prodBarcode = (TextView) findViewById(R.id.propose_product_barcode);
        barcode = String.valueOf(extras.getLong("barcode"));
        prodBarcode.setText(getResources().getString(R.string.barcode) + " " + barcode);
        prodIngredients = (EditText) findViewById(R.id.propose_product_ingredients);
        prodNoGluten = (CheckBox) findViewById(R.id.propose_product_no_gluten);
        prodNoLactose = (CheckBox) findViewById(R.id.propose_product_no_lactose);
        prodVegan = (CheckBox) findViewById(R.id.propose_product_vegan);
        send = (Button) findViewById(R.id.propose_product_send_button);

        settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        userMail = settings.getString("email", "");

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ingredientsReplaced = replaceNewLine();

                try {
                    toBeSent = prepareHashMap(ingredientsReplaced);
                    boolean [] hashMapOk = everyFieldFilled(toBeSent);
                    if (!hashMapOk[1]) { // somethingWentWrong variable
                        if(hashMapOk[0]) { // complete variable
                            if (!Utils.isOnline(ProposeAProduct.this))
                                Utils.displayNoConnectionAlert(ProposeAProduct.this,false);
                            else
                                new SendTask().execute(toBeSent);
                        }
                    }
                    else
                        displaySomethingWentWrongAlert();

                } catch (Exception e) {
                    e.printStackTrace();
                    displaySomethingWentWrongAlert();
                }
            }
        });
    }


    /*
    RITORNO:
        - 0 per l'errore
        - 1 per l'ok
        - 2 se il prodotto esiste già
    */

    private String replaceNewLine(){
       return prodIngredients.getText().toString().replace('\n', ',');

    }


    private HashMap<String, String> prepareHashMap(String ingredientsReplaced) {
        // put all the strings of the propose_product_dialog in a hashmap, plus the user mail
        HashMap<String, String> toBeSent = new HashMap<>(8);

        toBeSent.put("userMail", userMail);
        toBeSent.put("name", prodName.getText().toString());
        toBeSent.put("brand", prodBrand.getText().toString());
        toBeSent.put("barcode", barcode.toString());
        toBeSent.put("ingredients", ingredientsReplaced);
        if(prodNoGluten.isChecked())
            toBeSent.put("noGluten", "1");
        else
            toBeSent.put("noGluten", "0");
        if(prodNoLactose.isChecked())
            toBeSent.put("noLactose", "1");
        else
            toBeSent.put("noLactose", "0");
        if(prodVegan.isChecked())
            toBeSent.put("vegan", "1");
        else
            toBeSent.put("vegan", "0");
        return toBeSent;
    }

    private boolean[] everyFieldFilled(HashMap<String,String> hashMap){
        String empty = "";
        boolean complete = true;
        boolean somethingWentWrong = false; // checks if other fields not written by the user (e.g. usermail, barcode etc)
                                            // have been filled in the hashmap
        if(hashMap.get("userMail").equals(empty)) {
            complete = false;
            somethingWentWrong = true;
        }
        if(hashMap.get("name").equals(empty)) {
            prodName.setError(getResources().getString(R.string.propose_error_name));
            complete = false;
        }
        if(hashMap.get("brand").equals(empty)) {
            prodBrand.setError(getResources().getString(R.string.propose_error_brand));
            complete = false;
        }
        if(hashMap.get("barcode").equals(empty)) {
            complete = false;
            somethingWentWrong = true;
        }
        if(hashMap.get("ingredients").equals(empty)) {
            prodIngredients.setError(getResources().getString(R.string.propose_error_ingredients));
            complete = false;
        }
        if(hashMap.get("noGluten").equals(empty)) {
            complete = false;
            somethingWentWrong = true;
        }
        if(hashMap.get("noLactose").equals(empty)) {
            complete = false;
            somethingWentWrong = true;
        }
        if(hashMap.get("vegan").equals(empty)) {
            complete = false;
            somethingWentWrong = true;
        }
        return new boolean[]{complete, somethingWentWrong};
    }


    private void displaySomethingWentWrongAlert(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ProposeAProduct.this, R.style.AppTheme_Dark_Dialog);
        builder1.setTitle(R.string.oops_error);
        builder1.setMessage(R.string.something_went_wrong);
        builder1.setCancelable(false);
        builder1.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // create alert dialog
        AlertDialog alertDialog  = builder1.create();

        // show it
        alertDialog.show();
    }

    private void displayProductAlreadyInDBAlert(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ProposeAProduct.this, R.style.AppTheme_Dark_Dialog);
        builder1.setTitle(R.string.product_already_in_db_title);
        builder1.setMessage(R.string.product_already_in_db_message);
        builder1.setCancelable(false);
        builder1.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        // create alert dialog
        AlertDialog alertDialog  = builder1.create();

        // show it
        alertDialog.show();
    }

    private void displayProductAlreadyProposedByYourselfAlert(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ProposeAProduct.this, R.style.AppTheme_Dark_Dialog);
        builder1.setTitle(R.string.you_already_proposed_this_title);
        builder1.setMessage(R.string.you_already_proposed_this_message);
        builder1.setCancelable(false);
        builder1.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        // create alert dialog
        AlertDialog alertDialog  = builder1.create();

        // show it
        alertDialog.show();
    }

    private void displayProposalReceived(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ProposeAProduct.this, R.style.AppTheme_Dark_Dialog);
        builder1.setTitle(R.string.proposal_received_title);
        builder1.setMessage(R.string.proposal_received_message);
        builder1.setCancelable(false);
        builder1.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();

            }
        });
        // create alert dialog
        AlertDialog alertDialog  = builder1.create();

        // show it
        alertDialog.show();
    }



    class SendTask extends AsyncTask<HashMap<String,String>, Void, Void> {
        private ProgressDialog progressDialog;
        private JSONObject json;
        private int success;
        private String message;
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ProposeAProduct.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(HashMap<String,String>... params) {
            HashMap<String,String> toBeSent = params[0];
            Log.d("Propose a Product", "");
            Log.d("doInBackground: ", toBeSent.toString());
            json = JSONParser.serverRequest(url,toBeSent);


            try {
                success = json.getInt(TAG_SUCCESS);
                message=json.getString(TAG_MESSAGE);
                Log.d("Propose Product succ: " , "" + success );

                Log.d("Propose Product msg: " , "" + message );
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // dismiss the dialog once done
            progressDialog.dismiss();
            if(success == 0)  // an error occurred
                displaySomethingWentWrongAlert();
            else if(success == 2) // the proposed product was already in the db
                displayProductAlreadyInDBAlert();
            else if(success == 3) // the user already proposed to add this product
                displayProductAlreadyProposedByYourselfAlert();
            else // proposal received
                displayProposalReceived();
        }
    }
}


