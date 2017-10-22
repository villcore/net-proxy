package com.villcore.net.proxy.v3.pkg.v2;

import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthReqPackage;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthRespPackage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Optional;

public class PackageUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PackageUtils.class);

    public static ConnectReqPackage buildConnectPackage(String hostName, short port, int localConnId, long userFlag) throws UnsupportedEncodingException {
        byte[] header = ConnectReqPackage.newHeader(hostName, port, localConnId, userFlag);

        ConnectReqPackage pkg = new ConnectReqPackage();
        pkg.setHeader(header);
        pkg.setBody(new byte[0]);

//        System.out.println(pkg.getBodyLen() + "" + pkg.getBody().length);
        return pkg;
    }

    public static DefaultDataPackage buildDataPackage(int localConnId, int remoteConnId, long userFlag, byte[] data) {
        byte[] header = DefaultDataPackage.newHeader(localConnId, remoteConnId, userFlag);

        DefaultDataPackage pkg = new DefaultDataPackage();
        pkg.setHeader(header);
        pkg.setBody(data);
        return pkg;
    }

    public static ConnectRespPackage buildConnectRespPackage(int localConnId, int remoteConnId, long userFlag) {
        byte[] header = ConnectRespPackage.newHeader(localConnId, remoteConnId, userFlag);
        ConnectRespPackage pkg = new ConnectRespPackage();
        pkg.setHeader(header);
        pkg.setBody(new byte[0]);
        return pkg;
    }

    public static ChannelClosePackage buildChannelClosePackage(int localConnId, int remoteConnId, long userFlag) {
        byte[] header = ChannelClosePackage.newHeader(localConnId, remoteConnId, userFlag);
        ChannelClosePackage pkg = new ChannelClosePackage();
        pkg.setHeader(header);
        pkg.setBody(new byte[0]);
        return pkg;
    }

    public static ChannelReadPausePackage buildChannelReadPausePackage(int localConnId, int remoteConnId, long userFlag) {
        byte[] header = ChannelReadPausePackage.newHeader(localConnId, remoteConnId, userFlag);
        ChannelReadPausePackage pkg = new ChannelReadPausePackage();
        pkg.setHeader(header);
        pkg.setBody(new byte[0]);
        return pkg;
    }

    public static ChannelReadStartPackage buildChannelReadStartPackage(int localConnId, int remoteConnId, long userFlag) {
        byte[] header = ChannelReadStartPackage.newHeader(localConnId, remoteConnId, userFlag);
        ChannelReadStartPackage pkg = new ChannelReadStartPackage();
        pkg.setHeader(header);
        pkg.setBody(new byte[0]);
        return pkg;
    }

    public static ConnectAuthReqPackage buildConnectAuthReqPackage(String username, String password) throws UnsupportedEncodingException {
        byte[] header = ConnectAuthReqPackage.newHeader(username, password);
        ConnectAuthReqPackage pkg = new ConnectAuthReqPackage();
        pkg.setHeader(header);
        pkg.setBody(new byte[0]);
        return pkg;
    }

    public static ConnectAuthRespPackage buildConnectAuthRespPackage(String username, short stateCode) throws UnsupportedEncodingException {
        byte[] header = ConnectAuthRespPackage.newHeader(username, stateCode);
        ConnectAuthRespPackage pkg = new ConnectAuthRespPackage();
        pkg.setHeader(header);
        pkg.setBody(new byte[0]);
        return pkg;
    }

    public static TransferPackage buildTransferPackage(short pkgType, short compressType, byte[] ivBytes, byte[] body) throws UnsupportedEncodingException {
        byte[] header = TransferPackage.newHeader(pkgType, compressType, ivBytes);
        TransferPackage pkg = new TransferPackage();
        pkg.setHeader(header);
        pkg.setBody(body);

        return pkg;
    }

    public static void release(ByteBuf byteBuf) {
        byteBuf.release();
    }

    public static void release(Optional<Package> pkg) {

    }
}
