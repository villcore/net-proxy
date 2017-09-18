package com.villcore.net.proxy.v3.util;

public class ThreadUtils {
    public static Thread newThread(String name, Runnable target, boolean daemon) {
        Thread t = new Thread(target, name);
        t.setDaemon(daemon);
        return t;
    }
}
