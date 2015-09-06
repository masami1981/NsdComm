package com.example.marko.nsdcomm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class NsdService extends Service {
    private NsdSingleton nsdSingleton;
    public NsdService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Toast.makeText(this, "NSD OnBind", Toast.LENGTH_SHORT).show();
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        Toast.makeText(this, "NSD OnRebind", Toast.LENGTH_SHORT).show();
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "NSD OnUnbind", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "NSD onStartCommand", Toast.LENGTH_SHORT).show();
        nsdSingleton.startRegistrationService();
        return START_NOT_STICKY;
//        return super.onStartCommand(intent, flags, startId);
//        return  START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "NSD onCreate", Toast.LENGTH_SHORT).show();
        nsdSingleton = NsdSingleton.getInstance(getBaseContext());
//        TestSingleton testSingleton = TestSingleton.getInstance();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "NSD onDestroy", Toast.LENGTH_SHORT).show();
        nsdSingleton.stopAllServices();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Toast.makeText(this, "NSD onTaskRemoved", Toast.LENGTH_SHORT).show();
        nsdSingleton.stopAllServices();
        super.onTaskRemoved(rootIntent);
    }
}
