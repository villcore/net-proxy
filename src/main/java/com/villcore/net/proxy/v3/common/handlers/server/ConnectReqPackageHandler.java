package com.villcore.net.proxy.v3.common.handlers.server;

import com.villcore.net.proxy.v2.server.DNS;
import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * server side handler
 */
public class ConnectReqPackageHandler implements PackageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectReqPackage.class);

    private Bootstrap bootstrap;
    private TunnelManager tunnelManager;
    private Connection connection;

    public ConnectReqPackageHandler(Bootstrap bootstrap, TunnelManager tunnelManager, Connection connection) {
        this.bootstrap = bootstrap;
        this.tunnelManager = tunnelManager;
        this.connection = connection;
    }

    @Override
    public List<Package> handlePackage(List<Package> packages) {
        List<Package> connectReqPackage = packages.stream().filter(pkg -> pkg.getPkgType() == PackageType.PKG_CONNECT_REQ).collect(Collectors.toList());
        connectReqPackage.stream()
                .map(pkg -> ConnectReqPackage.class.cast(pkg))
                .collect(Collectors.toList())
                .forEach(pkg -> {
                    Integer connId = pkg.getConnId();
                    String hostname = pkg.getHostname();
                    int port = pkg.getPort();
                    connectToDst(hostname, port, connId);
                });
        List<Package> otherPackage = packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECT_REQ).collect(Collectors.toList());
        return otherPackage;
    }

    private void connectToDst(String hostname, int port, int connId) {
        String[] addrInfo = DNS.parseIp(hostname, port);
        String ip = addrInfo[0] == null || addrInfo[0].isEmpty() ? hostname : addrInfo[0];
        Tunnel[] tunnel = new Tunnel[1];
        try {
            final Channel channel = bootstrap.connect(ip, port).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(future.isSuccess()) {
                        if (tunnel[0] != null) {
                            tunnel[0].setConnect(true);
                        }
                    } else {
                        if (tunnel[0] != null) {
                            tunnel[0].setConnect(true);
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
                        }
                    }
                }
            });

            tunnel[0] = tunnelManager.newTunnel(channel);
            tunnel[0].setConnect(false);
            tunnelManager.bindConnection(connection, tunnel[0]);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
