package com.villcore.net.proxy.v3.common.handlers.client;

import com.villcore.net.proxy.v2.server.DNS;
import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.ConnectRespPackage;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * server side handler
 */
public class ConnectRespPackageHandler implements PackageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectReqPackage.class);

    private TunnelManager tunnelManager;

    public ConnectRespPackageHandler(TunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        List<Package> connectReqPackage = packages.stream().filter(pkg -> pkg.getPkgType() == PackageType.PKG_CONNECT_RESP).collect(Collectors.toList());

            connectReqPackage.stream()
                    .map(pkg -> ConnectRespPackage.class.cast(pkg))
                    .collect(Collectors.toList())
                    .forEach(pkg -> {
                        int connId = pkg.getLocalConnId();
                        int corrspondId = pkg.getRemoteConnId();
                        LOG.debug("connect resp ... [{}:{}]", connId, corrspondId);
                        Tunnel tunnel = tunnelManager.tunnelFor(connId);
                        tunnel.setCorrespondConnId(connId);
                        tunnel.rebuildSendPackages(corrspondId);
                        tunnel.setConnect(true);
                        tunnel.touch(pkg);
                    });

        List<Package> otherPackage = packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECT_RESP).collect(Collectors.toList());
        return otherPackage;
    }
}
