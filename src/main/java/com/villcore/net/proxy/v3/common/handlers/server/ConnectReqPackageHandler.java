package com.villcore.net.proxy.v3.common.handlers.server;

import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.server.DNS;
import com.villcore.net.proxy.v3.server.ServerChannelSendService;
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

import java.util.Collections;
import java.util.List;
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
        
        connectReqPackage.stream()
                .map(pkg -> ConnectReqPackage.class.cast(pkg))
                .collect(Collectors.toList())
                .forEach(pkg -> {
                    Integer connId = pkg.getConnId();
                    String hostname = pkg.getHostname();
                    int port = pkg.getPort();
                    LOG.debug("handle connect pkg, req address -> [{}:{}] ...", hostname, port);
                    connectToDst(hostname, port, connId, connection);
                });
        
        List<Package> otherPackage = packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECT_REQ).collect(Collectors.toList());
        return otherPackage;
    }

    private void connectToDst(String hostname, int port, int correspondConnId, Connection connection) {
        Bootstrap bootstrap = initBoostrap();
        String[] addrInfo = DNS.parseIp(hostname, port);
        String ip = addrInfo[0] == null || addrInfo[0].isEmpty() ? hostname : addrInfo[0];
        //ip = hostname;
        Tunnel[] tunnel = new Tunnel[1];
        try {
            LOG.debug(">>>>>>>>>>>>>>{}:{}", ip, port);
            final Channel channel = bootstrap.connect(ip, port).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(future.isSuccess()) {
                        if (tunnel[0] != null) {
                            tunnel[0].setConnect(true);
                            tunnel[0].setCorrespondConnId(correspondConnId);
                            tunnelManager.bindConnection(connection, tunnel[0]);

                            //server 构建 resp package ...
//                            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(tunnel[0].getCorrespondConnId(), tunnel[0].getConnId(), 1L);
                            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(tunnel[0].getConnId(), correspondConnId, 1L);
                            tunnel[0].addSendPackage(connectRespPackage);
                            LOG.debug("connect [{}:{}] success ...", hostname, port);
                        }
                    } else {
                        if (tunnel[0] != null) {
                            tunnel[0].setConnect(false);
                            tunnel[0].setCorrespondConnId(correspondConnId);
                            tunnelManager.bindConnection(connection, tunnel[0]);

//                            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(tunnel[0].getCorrespondConnId(), -1, 1L);
                            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(-1, correspondConnId, 1L);
                            tunnel[0].addSendPackage(connectRespPackage);
                            LOG.debug("connect [{}:{}] failed ...", hostname, port);
                        }
                    }
                }
            }).channel();

            channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(future.isSuccess()) {
                        if (tunnel[0] != null) {
                            tunnel[0].setConnect(false);
                            tunnel[0].setCorrespondConnId(correspondConnId);
                            tunnelManager.bindConnection(connection, tunnel[0]);

//                            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(tunnel[0].getCorrespondConnId(), -1, 1L);
                            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(-1, correspondConnId, 1L);
                            tunnel[0].addSendPackage(connectRespPackage);
                            LOG.debug("connect [{}:{}] failed ...", hostname, port);
                        }
                    }
                }
            });

            tunnel[0] = tunnelManager.newTunnel(channel);
            tunnel[0].setBindConnection(connection);
            tunnel[0].setConnect(false);
            tunnel[0].setCorrespondConnId(correspondConnId);
            tunnelManager.bindConnection(connection, tunnel[0]);
            writeService.addWrite(tunnel[0]);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            tunnel[0].setConnect(false);
            tunnel[0].setCorrespondConnId(correspondConnId);
            tunnelManager.bindConnection(connection, tunnel[0]);

//          ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(tunnel[0].getCorrespondConnId(), -1, 1L);
            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(-1, correspondConnId, 1L);
            tunnel[0].addSendPackage(connectRespPackage);
            LOG.debug("connect [{}:{}] failed ...", hostname, port);
        }
    }
}
