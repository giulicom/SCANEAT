package com.app.project.scaneatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.app.project.scaneatapp.db_products.MyProduct_DAO;
import com.app.project.scaneatapp.db_products.MyProduct_SQLiteHelper;
import com.app.project.scaneatapp.db_products.MyProduct_impl;

import java.io.File;
import java.util.Calendar;

import scaneat.R;

public class SyncDbActivity extends Activity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        boolean syncUpdate = settings.getBoolean("updateDB", true);
        boolean newDbVersion = settings.getBoolean("newDbVersion",false);


        if (syncUpdate) {
            Calendar calendar = Calendar.getInstance();
            java.util.Date now = calendar.getTime();
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

            SharedPreferences.Editor editor = settings.edit();
            editor.putString("lastUpdate", currentTimestamp.toString());
            editor.apply();


            String dbName = MyProduct_SQLiteHelper.DATABASE_NAME;
            File dbFile = SyncDbActivity.this.getDatabasePath(dbName);

            MyProduct_DAO myProduct_dao = new MyProduct_impl();
            myProduct_dao.open();
            int rows = myProduct_dao.countProducts();
            myProduct_dao.close();

            // check existing DB
            if(dbFile.length()==0 || rows<1 || newDbVersion){

                setContentView(R.layout.activity_sync_db);
                LinearLayout l = (LinearLayout) findViewById(R.id.layout);
                l.setBackgroundColor(getResources().getColor(R.color.primary_dark));
                AlertDialog.Builder builder1 = new AlertDialog.Builder(SyncDbActivity.this, R.style.AppTheme_Dark_Dialog);
                builder1.setTitle(R.string.download_ext_db_title);
                builder1.setMessage(R.string.download_ext_db_message);
                builder1.setCancelable(false);
                builder1.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        goAhead();
                    }
                });
                builder1.setPositiveButton(R.string.download_it, new DialogInterface.OnClickListener() {
                    // if you are logged, propose a new product
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SyncExtDB syncExtDB = new SyncExtDB(MyApplication.getAppContext(), SyncDbActivity.this);
                        syncExtDB.syncDB("-1",true);

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("newDbVersion",false);
                        editor.apply();

                        goAhead();
                    }
                });
                builder1.show();
            }
            else {
                goAhead();
            }
        }
        else {
            goAhead();
        }
    }

    public void goAhead() {
        Intent intent = new Intent(SyncDbActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

}
