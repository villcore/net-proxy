package com.villcore.net.proxy.nio;

/**
 * Created by Administrator on 2017/7/10.
 */
public class ThreadUtils {
    public static Thread newThread(String name, Runnable runnable) {
        String tname = name + "-" + System.currentTimeMillis();
        Thread thread = new Thread(runnable);
        thread.setName(tname);
        return thread;
    }
}
