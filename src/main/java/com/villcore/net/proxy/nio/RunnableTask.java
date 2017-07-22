package com.villcore.net.proxy.nio;

/**
 * Created by Administrator on 2017/7/10.
 */
public abstract class RunnableTask implements Runnable {
    protected volatile boolean running;
    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public Thread startThread() {
        Thread thread = ThreadUtils.newThread(getClass().getSimpleName(), this);
        thread.start();
        return thread;
    }
}
