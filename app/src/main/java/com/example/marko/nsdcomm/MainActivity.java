package com.example.marko.nsdcomm;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, NsdService.class));
        NsdSingleton.getInstance(this).startRegistrationService();
//        Toast.makeText(this, "MainActivity onCreate", Toast.LENGTH_SHORT).show();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DiscoveredServicesFragment fragment = new DiscoveredServicesFragment();
        fragmentTransaction.add(R.id.main_activity, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
//        Toast.makeText(this, "MainActivity onDestroy", Toast.LENGTH_SHORT).show();
        stopService(new Intent(this, NsdService.class));
        NsdSingleton.getInstance(this).stopAllServices();
        super.onDestroy();
    }

    @Override
    protected void onPostResume() {
//        Toast.makeText(this, "MainActivity onPostResume", Toast.LENGTH_SHORT).show();
        super.onPostResume();
    }

    @Override
    protected void onStop() {
//        Toast.makeText(this, "MainActivity onStop", Toast.LENGTH_SHORT).show();
        super.onStop();
    }

    @Override
    protected void onPause() {
//        Toast.makeText(this, "MainActivity onPause", Toast.LENGTH_SHORT).show();
        NsdSingleton.getInstance(this).stopDiscoverService();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNsdServiceDiscoveredServices);
        super.onPause();
    }

    @Override
    protected void onResume() {
//        Toast.makeText(this, "MainActivity onResume", Toast.LENGTH_SHORT).show();
        super.onResume();
        NsdSingleton.getInstance(this).startDiscoverService();
        LocalBroadcastManager.getInstance(this).registerReceiver(mNsdServiceDiscoveredServices, new IntentFilter(NsdSingleton.DISCOVERED_SERVICES_CHANGED));
    }

    @Override
    protected void onStart() {
//        Toast.makeText(this, "MainActivity onStart", Toast.LENGTH_SHORT).show();
        super.onStart();
    }

    private void updateDiscoveredServicesFragment(ArrayList<ServiceInfoDetails> discoveredServices) {
        ArrayList<String> services = new ArrayList<>();
        for (ServiceInfoDetails details : discoveredServices) {
            services.add(details.toString());
        }
        ListView listView = (ListView) findViewById(R.id.listview_discovered_service);
        ArrayAdapter<String> mDiscoveredServices = new ArrayAdapter<>(
                this,
                R.layout.list_item_discovered_services,
                R.id.list_item_discovered_service_textview,
                services);
        listView.setAdapter(mDiscoveredServices);
    }

    private BroadcastReceiver mNsdServiceDiscoveredServices = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<ServiceInfoDetails> discoveredServices = (ArrayList<ServiceInfoDetails>) intent.getSerializableExtra(NsdSingleton.DISCOVERED_SERVICES_DATA);
            updateDiscoveredServicesFragment(discoveredServices);
        }
    };
}
