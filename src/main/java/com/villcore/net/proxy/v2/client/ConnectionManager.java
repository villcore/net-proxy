package com.villcore.net.proxy.v2.client;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionManager implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private final AtomicInteger idCount = new AtomicInteger(0);
    private final Map<Integer, NioSocketChannel> channelMap = new Hashtable<>();
    private final Map<NioSocketChannel, Integer> connIdMap = new Hashtable<>();
    private final Map<Integer, Long> lastTouchMap = new ConcurrentHashMap<>();

    private Object updateLock = new Object();
    private static long idleScanInterval = 30 * 1000;
    private volatile boolean running;
    //负责维护本地connId -> channel映射
    //负责关闭超时的链接
    //负责新建链接
    public void start() {
        running = true;
    }

    public Integer getConnId(NioSocketChannel channel) {
        return connIdMap.get(channel);
    }

    public Integer addConnection(NioSocketChannel channel) {
        Integer connId = getConnId();
        synchronized (updateLock) {
            channelMap.put(connId, channel);
            connIdMap.put(channel, connId);
            lastTouchMap.putIfAbsent(connId, Long.valueOf(System.currentTimeMillis()));
        }
        return connId;
    }

    public Integer getConnId() {
        return idCount.getAndIncrement() & 0x7fffffff;
    }

    public void touch(Integer connId) {
        lastTouchMap.put(connId, System.currentTimeMillis());
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && running) {
            try {
                Thread.sleep(idleScanInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            synchronized (updateLock) {
                Iterator<Map.Entry<Integer, Long>> it = lastTouchMap.entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<Integer, Long> entry = it.next();
                    Integer connId = entry.getKey();
                    Long lastTouch = entry.getValue();

                    if(System.currentTimeMillis() - lastTouch > 1.5 * idleScanInterval) {
                        LOG.debug("connection {} shoud being closed ...", connId);
                        it.remove();
                        NioSocketChannel channel = channelMap.remove(connId);
                        connIdMap.remove(channel);
                        if(channel != null && !channel.isOpen()) {
                            channel.closeFuture();
                        }
                    }

                }
            }
        }
    }
}
