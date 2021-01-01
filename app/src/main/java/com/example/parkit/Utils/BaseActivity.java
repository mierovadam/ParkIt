package com.example.parkit.Utils;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity   {

    public final static String SP_RECORDS_KEY = "sharedPreferencesFile";

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyScreenUtils.hideSystemUI(this);
        }
    }

    //Double back press
    protected boolean isDoubleBackPressToClose = true;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        if (isDoubleBackPressToClose) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            }
            else {
                Toast.makeText(this, "Tap back button again to exit", Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

}
