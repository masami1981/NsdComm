package com.example.marko.nsdcomm;

import android.net.nsd.NsdServiceInfo;

import java.net.InetAddress;

/**
 * Created by Marko on 6.9.2015..
 */
public class ServiceInfoDetails {
    private long nanoTime;
    private String name;
    private String type;
    private InetAddress host;
    private int port;

    public ServiceInfoDetails(NsdServiceInfo service) {
        nanoTime = System.nanoTime();
        name = service.getServiceName();
        type = service.getServiceType();
        host = service.getHost();
        port = service.getPort();
    }

    public InetAddress getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public long getNanoTime() {
        return nanoTime;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return "name: " + name
                + ", host:" + host.getHostAddress()
                + ", port: " + port
                + ", type: " + type
                + ", time: " + nanoTime;
    }
}
