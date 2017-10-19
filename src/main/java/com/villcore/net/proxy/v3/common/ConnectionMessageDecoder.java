package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v2.*;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthReqPackage;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthRespPackage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 读取ByteBuf并根据length信息构建 {@link Package}
 */
//TODO byte to message
public class ConnectionMessageDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionMessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //LOGGER.debug("in reable bytes size = {}", in.readableBytes());
        //System.out.println("connection event thread = " + Thread.currentThread().getName());
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();
        int totalLen = in.readInt();

        if (in.readableBytes() < totalLen - 4) {
            in.resetReaderIndex();
            return;
        }

        in.resetReaderIndex();

        byte[] all = new byte[totalLen];
        in.readBytes(all);
        ByteBuffer allBuffer = ByteBuffer.wrap(all);

        int headerLen = allBuffer.getInt(4);
        int bodyLen = allBuffer.getInt(4 + 4);
        short pkgType = allBuffer.getShort(4 + 4 + 4);

        byte[] fixed = new byte[Package.FIXED_LEN];
        byte[] header = new byte[headerLen];
        byte[] body = new byte[bodyLen];

        System.arraycopy(all, 0, fixed, 0, Package.FIXED_LEN);
        System.arraycopy(all, Package.FIXED_LEN, header, 0, headerLen);
        System.arraycopy(all, Package.FIXED_LEN + headerLen, body, 0, bodyLen);

        Package correctPackage = correctPkg(all, fixed, header, body, pkgType);
        out.add(correctPackage);
    }

    private Package correctPkg(byte[] all, byte[] fixed, byte[] header, byte[] body, short pkgType) {
        Package pkg = null;

        //LOGGER.debug("pkg type = {}, all = {}, fixed = {}, header = {}, body = {}", new Object[]{pkgType, all.length, fixed.length, header.length, body.length});

        switch (pkgType) {
            case PackageType.PKG_CONNECT_REQ: {
                //不做处理
                ConnectReqPackage newPkg = (ConnectReqPackage) new ConnectReqPackage().valueOf(all);
                pkg = newPkg;
                //LOGGER.debug("pkg = {}, need connect = {}", pkg.getClass().getSimpleName(), newPkg.getHostname() + ":" + newPkg.getPort());
            }
            break;

            case PackageType.PKG_CONNECT_RESP: {
                ConnectRespPackage connectRespPackage = (ConnectRespPackage) new ConnectRespPackage().valueOf(all);
                int connId = connectRespPackage.getLocalConnId();
                int corospondConnId = connectRespPackage.getRemoteConnId();

                ConnectRespPackage newPkg = PackageUtils.buildConnectRespPackage(corospondConnId, connId, 1L);
                pkg = newPkg;
                //LOGGER.debug("pkg = {}, tunnel success for [{}] <===> [{}]", pkg.getClass().getSimpleName(), newPkg.getLocalConnId(), newPkg.getRemoteConnId());
            }
            break;

            case PackageType.PKG_CHANNEL_CLOSE: {
                ChannelClosePackage channelClosePackage = (ChannelClosePackage) new ChannelClosePackage().valueOf(all);
                int connId = channelClosePackage.getLocalConnId();
                int corospondConnId = channelClosePackage.getRemoteConnId();

                ChannelClosePackage newPkg = PackageUtils.buildChannelClosePackage(corospondConnId, connId, 1L);
                pkg = newPkg;
                LOGGER.debug("pkg = {}, need close [{}]", pkg.getClass().getSimpleName(), corospondConnId);
            }
            break;

            case PackageType.PKG_DEFAULT_DATA: {
                DefaultDataPackage dataPackage = (DefaultDataPackage) new DefaultDataPackage().valueOf(all);
                int connId = dataPackage.getLocalConnId();
                int corospondConnId = dataPackage.getRemoteConnId();

                DefaultDataPackage newPkg = PackageUtils.buildDataPackage(corospondConnId, connId, 1L, dataPackage.getBody());
                pkg = newPkg;
//                LOGGER.debug("pkg = {}, data send [{}] -> [{}]", pkg.getClass().getSimpleName(), newPkg.getRemoteConnId(), newPkg.getLocalConnId());
            }
            break;

            case PackageType.PKG_CHANNEL_CONTROL_PAUSE: {
                ChannelReadPausePackage channelReadPausePackage = (ChannelReadPausePackage) new ChannelReadPausePackage().valueOf(all);
                int connId = channelReadPausePackage.getLocalConnId();
                int corospondConnId = channelReadPausePackage.getRemoteConnId();

                ChannelReadPausePackage newPkg = PackageUtils.buildChannelReadPausePackage(corospondConnId, connId, 1L);
                pkg = newPkg;
                //LOGGER.debug("pkg = {}, need close [{}]", pkg.getClass().getSimpleName(), newPkg.getLocalConnId());
            }
            break;

            case PackageType.PKG_CHANNEL_CONTROL_START: {
                ChannelReadStartPackage channelReadStartPackage = (ChannelReadStartPackage) new ChannelReadStartPackage().valueOf(all);
                int connId = channelReadStartPackage.getLocalConnId();
                int corospondConnId = channelReadStartPackage.getRemoteConnId();

                ChannelReadStartPackage newPkg = PackageUtils.buildChannelReadStartPackage(corospondConnId, connId, 1L);
                pkg = newPkg;
                //LOGGER.debug("pkg = {}, need close [{}]", pkg.getClass().getSimpleName(), newPkg.getLocalConnId());
            }
            break;

            case PackageType.PKG_CONNECTION_AUTH_REQ:
                ConnectAuthReqPackage connectAuthReqPackage = (ConnectAuthReqPackage) new ConnectAuthReqPackage().valueOf(all);
                pkg = connectAuthReqPackage;
                break;

            case PackageType.PKG_CONNECTION_AUTH_RESP:
                ConnectAuthRespPackage connectAuthRespPackage = (ConnectAuthRespPackage) new ConnectAuthRespPackage().valueOf(all);
                pkg = connectAuthRespPackage;
                break;
            default:
                break;
        }
        return pkg;
    }
}
