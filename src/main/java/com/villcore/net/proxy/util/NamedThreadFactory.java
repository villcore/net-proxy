package com.villcore.net.proxy.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by WangTao on 2019-06-28
 */
public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger threadSeq = new AtomicInteger();
    private final String namePrefix;

    public NamedThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, namePrefix + "-" + threadSeq.getAndIncrement());
    }
}
