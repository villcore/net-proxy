package com.villcore.net.proxy.v3.common.handlers;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.v1.ChannelClosePackage;
import com.villcore.net.proxy.v3.pkg.v1.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.v1.Package;
import com.villcore.net.proxy.v3.pkg.v1.PackageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 该handler 过滤掉发往为空的Tunnel或shouldClose的Tunnel，并将重建channelClose Package 发往Connection的 sendQueue
 */

public class InvalidDataPackageHandler implements PackageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(InvalidDataPackageHandler.class);

    private TunnelManager tunnelManager;

    public InvalidDataPackageHandler(TunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        List<Package> avaliablePackages = packages.stream().filter(pkg -> pkg instanceof DefaultDataPackage)
                .map(pkg -> (DefaultDataPackage)pkg)
                .filter(pkg -> {
                    int connId = Integer.valueOf(pkg.getLocalConnId());
                    Tunnel tunnel = tunnelManager.tunnelFor(connId);
                    return tunnel != null;
                })
                .collect(Collectors.toList());
        //LOG.debug("handle invalid data pacakge ..., ori size = {}, cur size = {}", packages.size(), avaliablePackages.size());

        //
        List<DefaultDataPackage> invalidPackages = packages.stream().filter(pkg -> pkg instanceof DefaultDataPackage)
                .map(pkg -> (DefaultDataPackage)pkg)
                .filter(pkg -> {
                    int connId = Integer.valueOf(pkg.getLocalConnId());
                    Tunnel tunnel = tunnelManager.tunnelFor(connId);
                    return tunnel == null;
                })
                .collect(Collectors.toList());

        invalidPackages.forEach(pkg -> {
            int connId = Integer.valueOf(pkg.getLocalConnId());
            ChannelClosePackage channelClosePackage = PackageUtils.buildChannelClosePackage(connId, pkg.getRemoteConnId(), 1L);
                        connection.addSendPackages(Collections.singletonList(channelClosePackage));
                        PackageUtils.release(Optional.of(pkg));
        });
        return avaliablePackages;
    }
}
