package com.example.marko.nsdcomm;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

/**
 * Created by Marko on 4.9.2015..
 */
public class NsdSingleton {
    public static final String DISCOVERED_SERVICES_CHANGED = "com.example.marko.nsdcomm.DISCOVERED_SERVICES_CHANGED";
    public static final String DISCOVERED_SERVICES_DATA = "com.example.marko.nsdcomm.DISCOVERED_SERVICES_DATA";
    private static final boolean ADD = false;
    private static final boolean REMOVE = true;
    private static NsdSingleton ourInstance = null;

    private Context mContext;

    protected static NsdSingleton getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new NsdSingleton(context);
        }
        return ourInstance;
    }

    private NsdSingleton(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        try {
            wifiIP = getWifiIp();
            Log.e(TAG, "Wifi IP" + wifiIP);
        } catch (SocketException e) {
            Log.e(TAG, "Wifi IP error:" + e);
        }
        mNsdDeviceName = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        serviceName = "nsd-".concat(mNsdDeviceName);
        discoveredServices = new ArrayList<NsdServiceInfo>();
        addedServices = new ArrayList<NsdServiceInfo>();
        removedServices = new ArrayList<NsdServiceInfo>();
        servicesList = new ArrayList<ServiceInfoDetails>();
    }

    private final String TAG = getClass().getName();

    private final String mNsdDeviceName;
    private String serviceName;
    private final String SERVICE_TYPE = "_presence._tcp.";
    private final int SERVICE_PORT = 5298;

    private String wifiIP;
    private ArrayList<NsdServiceInfo> discoveredServices;
    private ArrayList<ServiceInfoDetails> servicesList;

    private NsdServiceInfo mServiceInfo;
    private final NsdManager mNsdManager;

    boolean timer = false;
    ArrayList<NsdServiceInfo> removedServices;
    ArrayList<NsdServiceInfo> addedServices;

    private final NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            mNsdManager.unregisterService(this);
            Log.d(TAG, "Service registration FAIL - " + serviceInfo + " " + errorCode);
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            mNsdManager.unregisterService(this);
            Log.d(TAG, "Service unregistration FAIL - " + serviceInfo + " " + errorCode);
        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Service registered - " + serviceInfo);
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Service UNREGISTERED - " + serviceInfo);
        }
    };

    private final NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.d(TAG, "Service discovery start FAIL - " + serviceType + " " + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.d(TAG, "Service discovery stop FAIL - " + serviceType + " " + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.d(TAG, "Service discovery started - " + serviceType);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.d(TAG, "Service discovery STOPPED - " + serviceType);
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Service discovery success - " + serviceInfo);
            if (serviceInfo.getServiceType() != SERVICE_TYPE) {
                Log.d(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
            } else if (serviceInfo.getServiceName() == serviceName) {
                Log.d(TAG, "Same Name:  " + serviceInfo.getServiceName());
            }
            try {
                mNsdManager.resolveService(serviceInfo, mResolveListener);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "listener already in use" + e);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Service LOST - removing service - " + serviceInfo);
            prepareServicesList(serviceInfo, REMOVE);
//            removeServiceFromList(serviceInfo);
        }

    };

    private final NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(TAG, "Service resolve FAIL - " + serviceInfo + " " + errorCode);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Service resolved - " + serviceInfo);
            if (serviceInfo.getServiceName() == serviceName) { // && wifiIP.equals(host.getHostAddress())) {
                Log.d(TAG, "Same named device: " + serviceInfo);
            } else {
                prepareServicesList(serviceInfo, ADD);
//                addServiceToList(serviceInfo);
                Log.d(TAG, "Adding service - " + serviceInfo);
            }
            Log.d(TAG, "Service resolved - " + serviceInfo);
        }
    };

    protected void startRegistrationService() {
        if (mServiceInfo == null) {
            mServiceInfo = new NsdServiceInfo();
            mServiceInfo.setServiceName(serviceName);
            mServiceInfo.setServiceType(SERVICE_TYPE);
            mServiceInfo.setPort(SERVICE_PORT);
            Log.d(TAG, "mservice " + mServiceInfo.toString());
        }
        try {
            mNsdManager.registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        } catch (RuntimeException e) {
            Log.e(TAG, "RegistrationService start ERROR - " + e);
        }
    }

    protected void stopRegistrationService() {
        try {
            mNsdManager.unregisterService(mRegistrationListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "RegistratonListener stop ERROR " + e);
        }
    }

    protected void startDiscoverService() {
        try {
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        } catch (RuntimeException e) {
            Log.e(TAG, "DiscoveryService start ERROR - " + e);
        }
    }

    protected void stopDiscoverService() {
        if (!(mDiscoveryListener == null) && !(mNsdManager == null)) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "DiscoveryListener stop ERROR " + e);
            }
        }
    }

    protected void stopAllServices() {
        stopDiscoverService();
        stopRegistrationService();
    }

    private String getWifiIp() throws SocketException {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
             en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            if (intf.isLoopback() || intf.isVirtual() || !intf.isUp() || intf.isPointToPoint() || (intf.getHardwareAddress() == null)) {
                continue;
            }
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                 enumIpAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (inetAddress.getAddress().length == 4) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        return null;
    }

    private String getRandomStringSizeOf(final int sizeOfRandomString) {
        final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    private synchronized void prepareServicesList(NsdServiceInfo serviceInfo, boolean remove) {

        for (ServiceInfoDetails details : servicesList) {
            if (serviceInfo.getServiceName().equals(details.getName())) {
                servicesList.remove(details);
                break;
            }
        }
        if (!remove) {
            servicesList.add(new ServiceInfoDetails(serviceInfo));

        }
        broadcastServices();
    }

    private void broadcastServices() {
        if (!servicesList.isEmpty()) {
            Log.i(TAG, "number of services : " + servicesList.size());
            Intent intent = new Intent();
            intent.setAction(DISCOVERED_SERVICES_CHANGED);
            intent.putExtra(DISCOVERED_SERVICES_DATA, (Serializable) servicesList);
            LocalBroadcastManager.getInstance(mContext).
                    sendBroadcast(intent);
        }
    }

}
