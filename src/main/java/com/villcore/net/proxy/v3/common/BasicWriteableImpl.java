package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v2.*;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * 发送服务接口
 */
public abstract class BasicWriteableImpl implements Writeable {
    public void write() {
        List<Package> writePackages = getWritePackages();

        for(Package pkg : writePackages) {
            int tunnelId = parseTunnelId(pkg);
            if(!write(pkg)) {
                failWrite(pkg);
                continue;
            }
            touch(tunnelId);
        }

        if(!writePackages.isEmpty()) {
            flush();
            writePackages.clear();
            writePackages = null;
        }
    }

    public int parseTunnelId(Package pkg) {
        int tunnelId = -1;

        switch (pkg.getPkgType()) {
            case PackageType.PKG_CONNECT_REQ: {//do nothing
                break;
            }

            case PackageType.PKG_CONNECT_RESP: { //convert connId pari
                ConnectRespPackage connectRespPackage = ConnectRespPackage.class.cast(pkg);
                int connId = connectRespPackage.getLocalConnId();
                tunnelId = connId;
                break;
            }

            case PackageType.PKG_CHANNEL_CLOSE: {//convert connId pair
                ChannelClosePackage channelClosePackage = ChannelClosePackage.class.cast(pkg);
                int connId = channelClosePackage.getLocalConnId();
                tunnelId = connId;
                break;
            }

            case PackageType.PKG_DEFAULT_DATA: {//convert connId pair
                DefaultDataPackage defaultDataPackage = DefaultDataPackage.class.cast(pkg);
                int connId = defaultDataPackage.getLocalConnId();
                tunnelId = connId;
                break;
            }
            default:
                break;
        }
        return tunnelId;
    }
}
