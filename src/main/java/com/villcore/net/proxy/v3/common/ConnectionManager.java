package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.client.ClientPackageDecoder;
import com.villcore.net.proxy.v3.client.ConnectionRecvPackageGatherHandler;
import com.villcore.net.proxy.v3.client.PackageToByteBufOutHandler;
import com.villcore.net.proxy.v3.pkg.ChannelClosePackage;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
import java.util.stream.Collectors;

/**
 * ConnectionManager
 * <p>
 * <p>
 * 定期关闭空的Connectjion
 */
public class ConnectionManager implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private static final short MAX_RETRY_COUNT = 5000;

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

    ;

    //TODO need sync

    /**
     * server side invoke
     *
     * @param channel
     * @return
     */
    public Connection acceptConnectTo(Channel channel) {
        synchronized (updateLock) {
            InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
            String addr = address.getHostName();
            int port = address.getPort();
            LOG.debug(">>>>>>>>>>>>>>>>>server accept client connection [{}:{}] ...", addr, port);

            Connection connection = new Connection(address.getHostName(), address.getPort(), tunnelManager);
            connection.setRemoteChannel(channel);
            connection.setConnected(true);
            connection.connectionTouch(System.currentTimeMillis());

//            channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
//                @Override
//                public void operationComplete(Future<? super Void> future) throws Exception {
//                    if (future.isSuccess()) {
//                        connection.setConnected(false);
//                        Tunnel tunnel = tunnelManager.tunnelFor(channel);
//                        tunnel.shouldClose();
//                        ChannelClosePackage channelClosePackage = PackageUtils
//                                .buildChannelClosePackage(tunnel.getConnId(), tunnel.getCorrespondConnId(), 1L);
//                        connection.addSendPackages(Collections.singletonList(channelClosePackage));
//                    }
//                }
//            });
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
            if (connectionMap.containsKey(addrAndPortKey(addr, port))) {
                connection = connectionMap.get(addrAndPortKey(addr, port));
            }
        }

        if (connection == null) {
            connection = connectionMap.getOrDefault(addrAndPortKey(addr, port), new Connection(addr, port, tunnelManager));
            synchronized (updateLock) {
                connectionMap.putIfAbsent(addrAndPortKey(addr, port), connection);
                tunnelManager.addConnection(connection);
            }
        }

        try {
            Connection finalConnection = connection;
            Channel channel = initBootstrap().connect(addr, port).sync().addListener(new GenericFutureListener<Future<? super Void>>() {
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
        } catch (Exception e) {
            connectFailed(addr, port, connection);
        }
        return connection;
    }

    private void connectFailed(String addr, int port, Connection finalConnection) {
        //TODO failed
        LOG.debug("connect to remote [{}:{}] server failed...", addr, port);
        finalConnection.setConnected(false);

        Short curRetry = retryCountMap.getOrDefault(addrAndPortKey(addr, port), Short.valueOf((short) 0));
        LOG.debug("cur retry = {}", curRetry);
        if (curRetry++ < MAX_RETRY_COUNT) {
            retryCountMap.put(addrAndPortKey(addr, port), Short.valueOf(curRetry));
            connectTo(addr, port);
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
    }

    private String addrAndPortKey(String addr, int port) {
        //return addr + ":" + String.valueOf(port);
        return addr;
    }

    public void closeConnection(Connection connection) {
        //发送所有package
        //channel#close
        //state clear
    }

    public Connection channelFor(Channel channel) {
        InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();
        String addr = remoteAddr.getHostName();
        int port = remoteAddr.getPort();
        return connectionMap.getOrDefault(addrAndPortKey(addr, port), null);
    }

    //TODO need sync
    public List<Connection> allConnected() {
        synchronized (updateLock) {
            return connectionMap.values().stream()
                    .filter(conn -> conn.isConnected())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void run() {
        //自动调度任务，用来清理长时间无响应的Connection
        //过滤touch超时的connection
        //TODO 这个逻辑如果关闭了connection，客户端如何才能新建连接，需要再考虑设计，服务端可以这样使用
        synchronized (updateLock) {
            List<String> connectionKeys = connectionMap.keySet().stream().collect(Collectors.toList());
            for(String connectionKey : connectionKeys) {
                Connection connection = connectionMap.remove(connectionKey);
                retryCountMap.remove(connectionKey);
                if(connection != null) {
                    connection.close();
                }
            }
        }
    }
}
