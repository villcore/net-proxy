package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.client.ClientPackageDecoder;
import com.villcore.net.proxy.v3.client.ConnectionRecvPackageGatherHandler;
import com.villcore.net.proxy.v3.client.PackageToByteBufOutHandler;
import com.villcore.net.proxy.v3.pkg.ChannelClosePackage;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ConnectionManager
 * <p>
 * <p>
 * 定期关闭空的Connectjion
 */
public class ConnectionManager implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private static final short MAX_RETRY_COUNT = 500;

    private EventLoopGroup eventLoopGroup;
    private TunnelManager tunnelManager;

    //addr:port -> conn
    private Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private Map<String, Short> retryCountMap = new ConcurrentHashMap<>();
    private Object updateLock = new Object();

    private WriteService writeService;

    public ConnectionManager(EventLoopGroup eventLoopGroup, TunnelManager tunnelManager, WriteService writeService) {
        this.eventLoopGroup = eventLoopGroup;
        this.tunnelManager = tunnelManager;
        this.writeService = writeService;
    }

    private Bootstrap initBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.SO_SNDBUF, 128 * 1024)

                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)

                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ClientPackageDecoder());
                        ch.pipeline().addLast(new ConnIdConvertChannelHandler2());
                        ch.pipeline().addLast(new ConnectionRecvPackageGatherHandler(ConnectionManager.this));
                        ch.pipeline().addLast(new PackageToByteBufOutHandler());
                    }
                });
        return bootstrap;
    }

    //TODO need sync

    /**
     * server side invoke
     *
     * @param channel
     * @return
     */
    public Connection acceptConnectTo(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        String addr = address.getAddress().getHostAddress();
        int port = address.getPort();
        LOG.debug(">>>>>>>>>>>>>>>>>server accept client connection [{}:{}] ...", addr, port);
        String connectionKey = addrAndPortKey(addr, port);

        synchronized (updateLock) {
            if (connectionMap.containsKey(connectionKey)) {
                Connection connection = connectionMap.get(connectionKey);
                connection.getRemoteChannel().close();
                connection.setRemoteChannel(channel);
                connection.setConnected(true);

                return connection;
            }
        }

        synchronized (updateLock) {
            Connection connection = new Connection(address.getHostName(), address.getPort(), tunnelManager);
            connection.setRemoteChannel(channel);
            connection.setConnected(true);
            connection.connectionTouch(System.currentTimeMillis());

            channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        connection.setConnected(false);
                    }
                }
            });
            connectionMap.put(addrAndPortKey(addr, port), connection);
            writeService.addWrite(connection);
            return connection;
        }
    }


    //TODO need sync

    /**
     * client side invoke, 客户端调用
     *
     * @param addr
     * @param port
     * @return
     */
    public Connection connectTo(String addr, int port) {
        Connection connection = null;
        synchronized (updateLock) {
            if (connection == null) {
                connection = connectionMap.getOrDefault(addrAndPortKey(addr, port), new Connection(addr, port, tunnelManager));
                connectionMap.putIfAbsent(addrAndPortKey(addr, port), connection);
                tunnelManager.addConnection(connection);
            }

            try {
                Connection finalConnection = connection;

                Channel channel = initBootstrap().connect(new InetSocketAddress(addr, port), new InetSocketAddress(60070)).sync().addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (future.isSuccess()) {
                            connectSuccess(addr, port, finalConnection);

                        } else {
                            connectFailed(addr, port, finalConnection);
                        }
                    }
                }).channel();

                channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        connectFailed(addr, port, finalConnection);
                    }
                });
                connection.setRemoteChannel(channel);
                //channel.writeAndFlush(Unpooled.EMPTY_BUFFER);
            } catch (Exception e) {
                connectFailed(addr, port, connection);
            }
            return connection;
        }
    }

    private void connectFailed(String addr, int port, Connection finalConnection) {

        //TODO failed
        LOG.debug("connect to remote [{}:{}] server failed...", addr, port);
        finalConnection.setConnected(false);
//        try {
//            finalConnection.getRemoteChannel().closeFuture().sync();
//        } catch (InterruptedException e) {
//            LOG.error(e.getMessage(), e);
//        }

        Short curRetry = retryCountMap.getOrDefault(addrAndPortKey(addr, port), Short.valueOf((short) 0));
        LOG.debug("cur retry = {}", curRetry);
        if (curRetry++ < MAX_RETRY_COUNT) {
            retryCountMap.put(addrAndPortKey(addr, port), Short.valueOf(curRetry));
            eventLoopGroup.schedule(new Runnable() {
                @Override
                public void run() {
                    connectTo(addr, port);
                }
            }, 1000, TimeUnit.MILLISECONDS);
            //connectTo(addr, port);
        } else {
            finalConnection.setConnected(false);
            LOG.debug("retry for [{}] exceed max retry count, this connection will closed...", addrAndPortKey(addr, port));
        }
        writeService.removeWrite(finalConnection);
    }

    private void connectSuccess(String addr, int port, Connection finalConnection) {
        //TODO success
        LOG.debug("connect to remote [{}:{}] server success...", addr, port);
        finalConnection.setConnected(true);
        finalConnection.connectionTouch(System.currentTimeMillis());
        retryCountMap.put(addrAndPortKey(addr, port), Short.valueOf((short) 0));
        writeService.addWrite(finalConnection);

        //TODO 构建ConnectionReqPackage, 添加到Queue中
    }

    private String addrAndPortKey(String addr, int port) {
        return addr + ":" + String.valueOf(port);
        //return addr;
    }

    public void closeConnection(Connection connection) {
        //发送所有package
        //channel#close
        //state clear
    }

    public Connection channelFor(Channel channel) {
        InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();
        String addr = remoteAddr.getAddress().getHostAddress();
        int port = remoteAddr.getPort();
//        LOG.debug("key = {}, channel map = {}", addrAndPortKey(addr, port), connectionMap.toString());
        return connectionMap.getOrDefault(addrAndPortKey(addr, port), null);
    }

    //TODO need sync
    public List<Connection> allConnected() {
        synchronized (updateLock) {
            return connectionMap.values().stream()
                    .filter(conn -> {
//                        LOG.debug("conn {} ", conn.toString() + conn.isConnected());
                        return conn.isConnected();
                    })
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void run() {
//        自动调度任务，用来清理长时间无响应的Connection
//        过滤touch超时的connection
//        TODO 这个逻辑如果关闭了connection，客户端如何才能新建连接，需要再考虑设计，服务端可以这样使用
        synchronized (updateLock) {
            List<String> connectionKeys = connectionMap.keySet().stream().collect(Collectors.toList());
            for (String connectionKey : connectionKeys) {
                Connection connection = connectionMap.remove(connectionKey);
                if (System.currentTimeMillis() - connection.lastTouch() > 3 * 60 * 1000)
                    retryCountMap.remove(connectionKey);
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }

    public Connection getConnection(String remoteAddr, int remotePort) {
        String connectionKey = addrAndPortKey(remoteAddr, remotePort);
        synchronized (updateLock) {
            if (connectionMap.containsKey(connectionKey)) {
                return connectionMap.get(connectionKey);
            }
        }

        // already sync and put into map ...
        Connection connection = connectTo(remoteAddr, remotePort);
        return connection;
    }
}
