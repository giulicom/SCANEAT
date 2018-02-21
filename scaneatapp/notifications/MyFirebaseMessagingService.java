package com.app.project.scaneatapp.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.app.project.scaneatapp.Constants;
import com.app.project.scaneatapp.MainActivity;
import com.app.project.scaneatapp.PersonalAreaActivity;
import com.app.project.scaneatapp.SyncDbActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import scaneat.R;

/**
 * Created by Giulia on 24/08/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG_LOG = FirebaseMessagingService.class.getSimpleName();

    public final static String FROM_NOTIFICATION = "FROM_NOTIFICATION";
    private static List<String> messages = new ArrayList<>();
    private static int notificationID = 0;

    public final static int INSERT_PROPOSE_REFUSED = 0;
    public final static int INSERT_PROPOSE_ACCEPTED = 1;
    public final static int MODIFY_PROPOSE_REFUSED = 2;
    public final static int MODIFY_PROPOSE_ACCEPPTED = 3;
    public final static int BROADCAST_NOTIFICATION = 4;

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(TAG_LOG,"onDeletedMessages()");
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d(TAG_LOG,"onMessageSent()");
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        Log.d(TAG_LOG,"onSendError()");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String type = remoteMessage.getData().get("type");
        Log.d("onmessagereceived","sono qui");

        try {

            String productDetails = new String(remoteMessage.getData().get("message"));

            SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

            boolean switchOn = preferences.getBoolean("notify",true);
            if(switchOn && Integer.valueOf(type) != BROADCAST_NOTIFICATION) {
                showNotification(type, productDetails);
            }
            else {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("newDbVersion",true);
                editor.apply();

                Log.d("onMessageReceived","broadcast notification");
            }

        } catch (JSONException e) {
            Log.d("onMessageReceived","exception");
            e.printStackTrace();
        }
    }

    private void showNotification(String type, String productDetails) throws JSONException{
        String title = getResources().getString(R.string.app_name);
        String message = new String();

        int t = Integer.valueOf(type);
        Intent i = new Intent();

        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        switch(t){
            case INSERT_PROPOSE_REFUSED:
                message = Html.fromHtml("<br>" + productDetails + "</br> ") + getResources().getString(R.string.notification_insert_product_refused);
                i = new Intent(this, MainActivity.class);
                break;
            case INSERT_PROPOSE_ACCEPTED:
                message = Html.fromHtml("<br>" + productDetails + "</br> ") + getResources().getString(R.string.notification_insert_product_approved);
                editor.putBoolean("newDbVersion",true);
                editor.apply();
                i = new Intent(this, SyncDbActivity.class);
                break;
            case MODIFY_PROPOSE_REFUSED:
                message = getResources().getString(R.string.notification_modify_product_refused)+ Html.fromHtml(" <br>" + productDetails + "</br>");
                i = new Intent(this, MainActivity.class);
                break;
            case MODIFY_PROPOSE_ACCEPPTED:
                message = Html.fromHtml("<br>" + productDetails + "</br> ") + getResources().getString(R.string.notification_modify_product_approved);
                editor.putBoolean("newDbVersion",true);
                editor.apply();
                i = new Intent(this, SyncDbActivity.class);
                break;
            default:
                break;
        }

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(FROM_NOTIFICATION,"true");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(i);


        Log.d("shownotification", message);

        // Grouping notifications
        messages.add(message);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for(String str: messages){
            inboxStyle.addLine(Html.fromHtml(str));
        }
        boolean summary = notificationID == 0;
        String contentText = getResources().getQuantityString(R.plurals.notification_counter,notificationID+1,notificationID+1);
        inboxStyle.setBigContentTitle(getString(R.string.app_name));
        inboxStyle.setSummaryText(contentText);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setTicker(getResources().getString(R.string.app_name))
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.logo))
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(contentText)
                .setGroupSummary(summary)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent)
                .build();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int smallIconViewId = getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());

            if (smallIconViewId != 0) {
                if (notification.contentIntent != null)
                    notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                if (notification.headsUpContentView != null)
                    notification.headsUpContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                if (notification.bigContentView != null)
                    notification.bigContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
            }
        }


        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        notificationID++;

        notificationManager.notify(0, notification);
        // vibration for 200 milliseconds
        ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(200);
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d("LOG","Service msg Stopped");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("NotificationID", "Destroyed");
    }

    public static void clearMessages(){
        messages.clear();
    }

    public static void resetNotificationID(){
        notificationID = 0;
    }
}
