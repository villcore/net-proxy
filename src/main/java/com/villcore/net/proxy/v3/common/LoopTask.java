package com.villcore.net.proxy.v3.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoopTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(LoopTask.class);

    private volatile boolean running;

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while(running && !Thread.currentThread().isInterrupted()) {
            try {
                loop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.error(e.getMessage(), e);
            }
        }
    }

    abstract void loop() throws InterruptedException;
}
