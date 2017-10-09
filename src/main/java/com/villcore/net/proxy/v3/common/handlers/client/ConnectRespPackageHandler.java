package com.villcore.net.proxy.v3.common.handlers.client;

import com.villcore.net.proxy.v2.server.DNS;
import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
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
            //
            connectReqPackage.stream()
                    .map(pkg -> ConnectRespPackage.class.cast(pkg))
                    .collect(Collectors.toList())
                    .forEach(pkg -> {
                        int connId = pkg.getLocalConnId();
                        int corrspondId = pkg.getRemoteConnId();
                        LOG.debug(">> client connect resp ... [{}:{}]", connId, corrspondId);

                        Tunnel tunnel = tunnelManager.tunnelFor(connId);
                        if(tunnel == null) {
                            LOG.error("can not find client tunnel [{}] , this tunnel maybe cleanup ...", connId);
                            return;
                        }

                        //server tunnel can not connect to dst ...
                        if(corrspondId < 0) {
                            LOG.debug("server can not build tunnel for client tunnel [{}] ...", connId);
                            tunnel.setConnect(false);
                        } else {
                            LOG.debug(" tunnels build success for  [{}] <======> [{}]...", connId, corrspondId);
                            tunnel.setCorrespondConnId(corrspondId);
                            tunnel.rebuildSendPackages(corrspondId);
                            tunnel.setConnect(true);
                            tunnel.tunnelConnected();
                            tunnel.touch(-1);
//                            tunnel.touch(pkg);
//                            if(tunnel.isHttps()) {
//                                String connectResponse = "HTTP/1.0 200 Connection Established\r\n\r\n";
//                                tunnel.addRecvPackage(PackageUtils.buildDataPackage(connId, corrspondId, 1L, Unpooled.wrappedBuffer(connectResponse.getBytes())));
//                            }
                        }
                    });

        List<Package> otherPackage = packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECT_RESP).collect(Collectors.toList());
        return otherPackage;
    }
}
