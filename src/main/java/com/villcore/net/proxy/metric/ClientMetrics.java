package com.villcore.net.proxy.metric;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by WangTao on 2019-07-24
 */
public class ClientMetrics {

    private static final AtomicInteger openLocalChannelCounter = new AtomicInteger(0);
    private static final AtomicInteger openRemoteChannelCounter = new AtomicInteger(0);

    public static void incrOpenLocalChannelCounter(int count) {
        openLocalChannelCounter.addAndGet(count);
    }

    public static int openLocalChannels() {
        return openLocalChannelCounter.get();
    }

    public static void incrOpenRemoteChannelCounter(int count) {
        openRemoteChannelCounter.addAndGet(count);
    }

    public static int openRemoteChannels() {
        return openRemoteChannelCounter.get();
    }

    public static int openChanels() {
        return openLocalChannels() + openRemoteChannels();
    }
}
