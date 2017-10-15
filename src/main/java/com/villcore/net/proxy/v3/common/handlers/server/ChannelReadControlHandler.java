package com.villcore.net.proxy.v3.common.handlers.server;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.v2.*;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChannelReadControlHandler implements PackageHandler  {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelReadControlHandler.class);

    private TunnelManager tunnelManager;

    public static boolean read = true;
    public ChannelReadControlHandler(TunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        List<Package> channelReadControlPackages = packages.stream().filter(pkg -> (pkg.getPkgType() == PackageType.PKG_CHANNEL_CONTROL_PAUSE || pkg.getPkgType() == PackageType.PKG_CHANNEL_CONTROL_START))
                .collect(Collectors.toList());

        List<Package> otherPackage = packages.stream().filter(pkg -> !(pkg.getPkgType() == PackageType.PKG_CHANNEL_CONTROL_PAUSE || pkg.getPkgType() == PackageType.PKG_CHANNEL_CONTROL_START))
                .collect(Collectors.toList());

        channelReadControlPackages.forEach(pkg -> {

            if (pkg.getPkgType() == PackageType.PKG_CHANNEL_CONTROL_PAUSE) {
                ChannelReadPausePackage pausePackage = ChannelReadPausePackage.class.cast(pkg);
                int connId = pausePackage.getLocalConnId();
                Tunnel curTunnel = tunnelManager.tunnelFor(connId);
                if(curTunnel != null && curTunnel.getChannel() != null) {
                    curTunnel.getChannel().config().setAutoRead(false);
//                    Channel channel = curTunnel.getChannel();
//                    Attribute<Boolean> pause = channel.attr(AttributeKey.<Boolean>valueOf("pause"));
//                    pause.set(true);
                    curTunnel.setPause(true);
                    LOGGER.debug("pause read for tunnel [{}] -> [{}] ...", curTunnel.getConnId(), curTunnel.getCorrespondConnId());
                }
            }

            if (pkg.getPkgType() == PackageType.PKG_CHANNEL_CONTROL_START) {
                ChannelReadStartPackage startPackage = ChannelReadStartPackage.class.cast(pkg);
                int connId = startPackage.getLocalConnId();
                Tunnel curTunnel = tunnelManager.tunnelFor(connId);
                if(curTunnel != null && curTunnel.getChannel() != null) {
                    curTunnel.getChannel().config().setAutoRead(true);
                    Channel channel = curTunnel.getChannel();
//                    Attribute<Boolean> pause = channel.attr(AttributeKey.<Boolean>valueOf("pause"));
//                    pause.set(false);
                    curTunnel.setPause(false);
                    channel.read();
                    channel.pipeline().firstContext().fireChannelReadComplete();
                    LOGGER.debug("start read for tunnel [{}] -> [{}] ...", curTunnel.getConnId(), curTunnel.getCorrespondConnId());
                }
            }
        });
        return otherPackage;
    }
}
