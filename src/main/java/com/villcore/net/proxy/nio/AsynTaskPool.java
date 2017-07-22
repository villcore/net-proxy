package com.villcore.net.proxy.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by villcore on 2017/7/17.
 */
public class AsynTaskPool {
    private static final Logger LOG = LoggerFactory.getLogger(AsynTaskPool.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public static Future<Void> execute(Runnable runnable) throws ExecutionException, InterruptedException {
        LOG.debug("submit task...");
        return (Future<Void>) EXECUTOR_SERVICE.submit(runnable);
    }
}
