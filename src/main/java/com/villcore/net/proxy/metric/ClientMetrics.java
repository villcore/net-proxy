package com.villcore.net.proxy.metric;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * created by WangTao on 2019-07-24
 */
public class ClientMetrics {

    private static final AtomicInteger openLocalChannelCounter = new AtomicInteger(0);
    private static final AtomicInteger openRemoteChannelCounter = new AtomicInteger(0);
    private static final AtomicLong rxSec = new AtomicLong(0);
    private static final AtomicLong txSec = new AtomicLong(0);
    private static final AtomicLong rxTotal = new AtomicLong(0);
    private static final AtomicLong txTotal = new AtomicLong(0);

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

    public static void incRx(long rx) {
        rxSec.addAndGet(rx);
        rxTotal.addAndGet(rx);
    }

    public static void incTx(long tx) {
        txSec.addAndGet(tx);
        txTotal.addAndGet(tx);
    }

    public static long rx() {
        return rxSec.getAndSet(0);
    }

    public static long tx() {
        return txSec.getAndSet(0);
    }

    public static long rxTotal() {
        return rxTotal.get();
    }

    public static long txTotal() {
        return txTotal.get();
    }
}
