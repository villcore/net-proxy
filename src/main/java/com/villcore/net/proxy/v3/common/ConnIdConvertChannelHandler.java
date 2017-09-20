package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * 根据需要，在发送端将connId, corrspondConnId顺序对调，保证在两端接收到ByteBuf转Pacakge时
 *
 * connId顺序均为
 *
 * connId, dorspondConnId
 */
public class ConnIdConvertChannelHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof Package) {
            Package pkg = (Package) msg;
            ByteBuf header = pkg.getHeader().copy();
            ByteBuf body = pkg.getBody().copy();

            switch (pkg.getPkgType()) {
                case PackageType.PKG_CONNECT_REQ: //do nothing
                    ConnectReqPackage connectReqPackage = new ConnectReqPackage();
                    connectReqPackage.setHeader(header);
                    connectReqPackage.setBody(body);
                    pkg = connectReqPackage;
                    break;
                case PackageType.PKG_CONNECT_RESP: //convert connId pari
                    ConnectRespPackage connectRespPackage = new ConnectRespPackage();
                    connectRespPackage.setHeader(header);
                    connectRespPackage.setBody(body);

                    int connId = connectRespPackage.getLocalConnId();
                    int corspondConnId = connectRespPackage.getRemoteConnId();
                    ConnectRespPackage newPkg = PackageUtils.buildConnectRespPackage(corspondConnId, connId, 1L);

                    pkg = newPkg;
                    break;
                case PackageType.PKG_CHANNEL_CLOSE: //convert connId pair
                    ChannelClosePackage channelClosePackage = new ChannelClosePackage();
                    channelClosePackage.setHeader(header);
                    channelClosePackage.setBody(body);
                    pkg = channelClosePackage;
                    break;
                case PackageType.PKG_DEFAULT_DATA: //convert connId pair
                    DefaultDataPackage defaultDataPackage = new DefaultDataPackage();
                    defaultDataPackage.setHeader(header);
                    defaultDataPackage.setBody(body);
                    pkg = defaultDataPackage;
                    break;
                default:
                    break;
            }
        }
        ctx.write(msg);
    }
}
