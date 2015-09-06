package com.example.marko.nsdcomm;

import android.app.Application;
import android.content.Intent;

/**
 * Created by Marko on 5.9.2015..
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        Toast.makeText(this, "MyApp onCreate", Toast.LENGTH_SHORT).show();
        startService(new Intent(this, NsdService.class));
    }

    @Override
    public void onTerminate() {
        NsdSingleton.getInstance(getApplicationContext()).stopAllServices();
        stopService(new Intent(this, NsdService.class));
        super.onTerminate();
    }
}
