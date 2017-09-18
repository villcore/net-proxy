package com.villcore.net.proxy.v3.common;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConnectionManager
 *
 * 定期关闭空的Connectjion
 *
 */
public class ConnectionManager implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private EventLoopGroup eventLoopGroup;

    //channel -> conn
    private Map<Channel, Connection> connectionMap = new ConcurrentHashMap<>();

    @Override
    public void run() {
        //自动调度任务，用来清理长时间无响应的Connection
    }
}
