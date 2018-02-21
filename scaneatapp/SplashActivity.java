package com.app.project.scaneatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import scaneat.R;

public class SplashActivity extends Activity {

    private static final long MIN_WAIT_INTERVAL = 1000L;
    private static final long MAX_WAIT_INTERVAL = 3000L;
    private static final String TAG_LOG = SplashActivity.class.getName();
    private long mStartTime; // first visualization instant
    private boolean goneAhead;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        goneAhead = false;

        if(savedInstanceState != null)
        {
            this.mStartTime = SystemClock.uptimeMillis();
        }

        final ImageView logoImageView = (ImageView)findViewById(R.id.splash_imageview);

        logoImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG_LOG, "ImageView touched!");
                long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                if (elapsedTime >= MIN_WAIT_INTERVAL) {
                    Log.d(TAG_LOG, "OK! Let's go ahead...");
                    goneAhead = true;
                    goAhead();
                } else {
                    Log.d(TAG_LOG, "Too much early! ");
                }
                return false;
            }
        });

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                if(!goneAhead) {
                    goAhead();
                    finish();
                }
            }
        }, MAX_WAIT_INTERVAL);
    }

    private void goAhead()
    {
        final Intent intent = new Intent(this, SyncDbActivity.class);
        startActivity(intent);
        finish();
    }
}