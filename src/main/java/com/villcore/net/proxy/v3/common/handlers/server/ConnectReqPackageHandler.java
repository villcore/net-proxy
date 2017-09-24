package com.villcore.net.proxy.v3.common.handlers.server;

import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.server.DNS;
import com.villcore.net.proxy.v3.server.ServerTunnelChannelReadHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * server side handler
 */
public class ConnectReqPackageHandler implements PackageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectReqPackage.class);

    private EventLoopGroup eventLoopGroup;

    private WriteService writeService;
    private TunnelManager tunnelManager;
    private ConnectionManager connectionManager;

    private static final short MAX_CONNECT_RETRY = 3;

    public ConnectReqPackageHandler(EventLoopGroup eventLoopGroup, WriteService writeService, TunnelManager tunnelManager, ConnectionManager connectionManager) {
        this.eventLoopGroup = eventLoopGroup;
        this.writeService = writeService;
        this.tunnelManager = tunnelManager;
        this.connectionManager = connectionManager;
    }

    private Bootstrap initBoostrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                .channel(NioSocketChannel.class)
                .handler(new ServerTunnelChannelReadHandler(tunnelManager));
        return bootstrap;
    }

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        List<Package> connectReqPackage = packages.stream()
                .filter(pkg -> pkg.getPkgType() == PackageType.PKG_CONNECT_REQ)
                .collect(Collectors.toList());
//        LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>{}, {}", packages.size(), connectReqPackage.size());
        connectReqPackage.stream()
                .map(pkg -> ConnectReqPackage.class.cast(pkg))
                .collect(Collectors.toList())
                .forEach(pkg -> {
                    Integer correspondConnId = pkg.getConnId();
                    String hostname = pkg.getHostname();
                    int port = pkg.getPort();
                    LOG.debug("handle connect pkg, req address -> [{}:{}] ...", hostname, port);
                    //connectToDst(hostname, port, correspondConnId, connection);
                    connectToDst(hostname, port, correspondConnId, connection, 0);
//                    hostname = "127.0.0.1";
//                    port = 3128;
//                    connectToDst(hostname, port, correspondConnId, connection, 0);

                });

        List<Package> otherPackage = packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECT_REQ).collect(Collectors.toList());
        return otherPackage;
    }

    /**
     * 连接到目的地址, 并建立通道, 支持重试, 实际情况表面, 建立到目的连接经常出现fail, shi'yo重试很有必要
     *
     * 具体思路, 因为该逻辑是在业务线程中, 尽量避免阻塞等待
     *
     * 1.单独包装一个线程池, 由其进行同步sync等待调用
     * 2.使用共用的线程, 不进行同步sync调用, 而是使用future机制, 使用回调接口, 这种代码更加复杂
     *
     * @param hostname
     * @param port
     * @param correspondConnId
     * @param connection
     *
     */

    /****
     * 以下是方法1的实现
     */
//    private void connectToDst(String hostname, int port, int correspondConnId, Connection connection) {
//        connectDstExecutor.submit(new Runnable() {
//            @Override
//            public void run() {
//                String[] addrInfo = DNS.parseIp(hostname, port);
//                String ip = addrInfo[0] == null || addrInfo[0].isEmpty() ? hostname : addrInfo[0];
//                ip = hostname;
//
//                Channel channel = null;
//                short retry = 0;
//
//                while (retry < MAX_CONNECT_RETRY) {
//                    try {
//                        channel = initBoostrap().connect(ip, port).sync().channel();
//                        LOG.debug("===== connect [{}:{}] success ...", hostname, port);
//                        break;
//                    } catch (Exception e) {
//                        LOG.error(e.getMessage(), e);
//                        //LOG.debug("===== connect [{}:{}] failed, retry count [{}] ...", hostname, port, retry);
//                        retry++;
//                    }
//                }
//
//                if(channel == null || !channel.isOpen()) {
//                    LOG.debug("===== connect [{}:{}] failed  ...", hostname, port);
//                    ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(-1, correspondConnId, 1L);
//                    connection.addSendPackages(Collections.singletonList(connectRespPackage));
//                    LOG.debug("connect resp [CID{}:CCID{}]", -1, correspondConnId);
//                    LOG.debug("connect [{}:{}] failed ...", hostname, port);
//                    channel.close();
//                    return;
//                }
//
//                //connect success...
//                //channel success, build tunnel
//                if(channel != null && channel.isOpen()) {
//                    Tunnel tunnel = tunnelManager.newTunnel(channel);
//                    tunnel.setBindConnection(connection);
//                    tunnel.setConnect(false);
//                    tunnel.setCorrespondConnId(correspondConnId);
//                    tunnelManager.bindConnection(connection, tunnel);
//                    tunnel.setConnect(true);
//                    ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(tunnel.getConnId(), correspondConnId, 1L);
//                    LOG.debug("connect resp [CID{}:CCID{}]", tunnel.getConnId(), correspondConnId);
//                    tunnel.addSendPackage(connectRespPackage);
//                    writeService.addWrite(tunnel);
//                    LOG.debug("connect [{}:{}] success ...", hostname, port);
//
//                    channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
//                        @Override
//                        public void operationComplete(Future<? super Void> future) throws Exception {
//                            if (future.isSuccess()) {
//                                tunnel.setConnect(false);
//                                //TODO build channel close package and send
//                            }
//                        }
//                    });
//                }
//            }
//        });
//    }

    //TODO
    /***
     * 方法2实现, 从效率角度来说, 方法2更好一些
     */
    private void connectToDst(String hostname, int port, int correspondConnId, Connection connection, int retry) {
                String[] addrInfo = DNS.parseIp(hostname, port);
                String ip = addrInfo[0] == null || addrInfo[0].isEmpty() ? hostname : addrInfo[0];
                ip = hostname;


                Channel[] channels = new Channel[1];
                final int curRetry = retry;

                        channels[0] = initBoostrap().connect(ip, port).addListener(new GenericFutureListener<Future<? super Void>>() {
                            @Override
                            public void operationComplete(Future<? super Void> future) throws Exception {
                                Channel channel = channels[0];

                                if(future.isSuccess()) {
                                    //connect success...
                                    //channel success, build tunnel
                                    if(channel != null && channel.isOpen()) {
                                        Tunnel tunnel = tunnelManager.newTunnel(channel);
                                        tunnel.setBindConnection(connection);
                                        tunnel.setConnect(false);
                                        tunnel.setCorrespondConnId(correspondConnId);
                                        tunnelManager.bindConnection(connection, tunnel);
                                        tunnel.setConnect(true);
                                        ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(tunnel.getConnId(), correspondConnId, 1L);
                                        //LOG.debug("connect resp [CID{}:CCID{}]", tunnel.getConnId(), correspondConnId);
                                        tunnel.addSendPackage(connectRespPackage);
                                        writeService.addWrite(tunnel);
                                        LOG.debug("connect [{}:{}] success for tunnels [CID{}:CCID{}] ...", hostname, port, tunnel.getConnId(), correspondConnId);

                                        channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                                            @Override
                                            public void operationComplete(Future<? super Void> future) throws Exception {
                                                if (future.isSuccess()) {
                                                    tunnel.setConnect(false);
                                                    //TODO build channel close package and send
                                                }
                                            }
                                        });
                                    }
                                }
                                else {
                                    int newRetry = curRetry + 1;
                                    if(newRetry > MAX_CONNECT_RETRY) {
                                        if(channel == null || !channel.isOpen()) {
                                            //LOG.debug("===== connect [{}:{}] failed  ...", hostname, port);
                                            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(-1, correspondConnId, 1L);
                                            connection.addSendPackages(Collections.singletonList(connectRespPackage));
                                            //LOG.debug("connect resp [CID{}:CCID{}]", -1, correspondConnId);
                                            LOG.debug("connect [{}:{}] failed for tunnels [CID{}:CCID{}] ...", hostname, port, -1, correspondConnId);
                                            if(channel != null) {
                                                channel.close();
                                            }
                                            return;
                                        }
                                    } else {
                                        //retry connect
                                        connectToDst(hostname, port, correspondConnId, connection, newRetry);
                                    }
                                }
                            }
                        }).channel();
    }
}
