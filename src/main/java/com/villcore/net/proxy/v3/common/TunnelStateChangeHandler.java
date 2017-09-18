package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;

import java.util.List;
import java.util.stream.Collectors;

public class TunnelStateChangeHandler implements PackageHandler {
    private TunnelManager tunnelManager;

    @Override
    public List<Package> handlePackage(List<Package> packages) {
        return packages.stream().filter(pkg -> {
            if(pkg instanceof ConnectRespPackage) {
                ConnectRespPackage connectRespPackage = (ConnectRespPackage) pkg;
                tunnelManager.tunnelResp(connectRespPackage);
                return false;
            }

            if(pkg instanceof ChannelClosePackage) {
                ChannelClosePackage connectRespPackage = (ChannelClosePackage) pkg;
                tunnelManager.tunnelClose(connectRespPackage);
                return false;
            }

            if(pkg instanceof DefaultDataPackage) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }
}
