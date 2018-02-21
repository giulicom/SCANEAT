package com.app.project.scaneatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import scaneat.R;

public class Utils {

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void displayNoConnectionAlert(final Context context, final boolean fromSplash){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dark_Dialog);
        builder.setTitle(R.string.no_connection_alert);
        builder.setMessage(R.string.no_connection_message);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(fromSplash==true){
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);

                    ((Activity)context).finish();
                }

            }
        });

        AlertDialog alertDialog  = builder.create();

        // show it
        alertDialog.show();
    }
}
