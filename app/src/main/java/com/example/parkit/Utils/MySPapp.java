package com.example.parkit.Utils;

import android.app.Application;

public class MySPapp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MySP.init(this);
    }
}