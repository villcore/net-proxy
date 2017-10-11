//package com.villcore.net.proxy.v3.common;
//
//import com.villcore.net.proxy.v3.pkg.*;
//import com.villcore.net.proxy.v3.pkg.Package;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.ChannelOutboundHandlerAdapter;
//import io.netty.channel.ChannelPromise;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * 根据需要，在发送端将connId, corrspondConnId顺序对调，保证在两端接收到ByteBuf转Pacakge时
// * <p>
// * connId顺序均为
// * <p>
// * connId, corspondConnId
// */
//public class ConnIdConvertChannelHandler2 extends ChannelInboundHandlerAdapter {
//    private static final Logger LOG = LoggerFactory.getLogger(ConnIdConvertChannelHandler2.class);
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//
//        if (msg instanceof Package) {
//            Package pkg = (Package) msg;
//
//            short pkgType = pkg.getPkgType();
//            ByteBuf header = pkg.getHeader().copy();
//            ByteBuf body = pkg.getBody().copy();
//
//            //LOG.debug("************connId convert running.........{}", pkg.getPkgType());
//            PackageUtils.printRef("convert before package >>>>>>>>>", pkg);
//
//            switch (pkgType) {
//                case PackageType.PKG_CONNECT_REQ: {//do nothing
//                    ConnectReqPackage connectReqPackage = new ConnectReqPackage();
//                    connectReqPackage.setHeader(header);
//                    connectReqPackage.setBody(body);
//
//                    PackageUtils.printRef("convert to connect req pkg, ori = ", connectReqPackage);
//                    pkg = connectReqPackage;
//                    PackageUtils.printRef("convert to connect req pkg, cur = ", pkg);
//                    break;
//                }
//
//                case PackageType.PKG_CONNECT_RESP: { //convert connId pari
//                    ConnectRespPackage connectRespPackage = new ConnectRespPackage();
//                    connectRespPackage.setHeader(header);
//                    connectRespPackage.setBody(body);
//
//                    int connId = connectRespPackage.getLocalConnId();
//                    int corspondConnId = connectRespPackage.getRemoteConnId();
//
//                    //PackageUtils.release(connectRespPackage);
//                    PackageUtils.release(connectRespPackage);
//
//                    //LOG.debug("convert connId for connect resp from [{}:{}] to [{}:{}]", new Object[]{connId, corspondConnId, corspondConnId, connId});
//                    ConnectRespPackage newPkg = PackageUtils.buildConnectRespPackage(corspondConnId, connId, 1L);
//                    PackageUtils.printRef("convert to connect resp pkg, ori = ", connectRespPackage);
//                    pkg = newPkg;
//                    PackageUtils.printRef("convert to connect resp pkg, cur = ", pkg);
//
//                    break;
//                }
//                case PackageType.PKG_CHANNEL_CLOSE: {//convert connId pair
//                    ChannelClosePackage channelClosePackage = new ChannelClosePackage();
//                    channelClosePackage.setHeader(header);
//                    channelClosePackage.setBody(body);
//
//                    int connId = channelClosePackage.getLocalConnId();
//                    int corspondConnId = channelClosePackage.getRemoteConnId();
//
////                    PackageUtils.printRef("--------------------" + getClass().getSimpleName() + "close pkg", channelClosePackage);
//                    PackageUtils.release(channelClosePackage);
////                    PackageUtils.printRef("-after-------------------" + getClass().getSimpleName() + "close pkg", channelClosePackage);
//
//                    ChannelClosePackage newPkg = PackageUtils.buildChannelClosePackage(corspondConnId, connId, 1L);
////                    channelClosePackage.toByteBuf().release(1);
//                    //PackageUtils.release(channelClosePackage);
//                    PackageUtils.printRef("convert to channel close pkg, ori = ", channelClosePackage);
//                    pkg = newPkg;
//                    PackageUtils.printRef("convert to channel close resp pkg, cur = ", pkg);
//                    break;
//                }
//                case PackageType.PKG_DEFAULT_DATA: {//convert connId pair
//                    DefaultDataPackage defaultDataPackage = new DefaultDataPackage();
//                    defaultDataPackage.setHeader(header);
//                    defaultDataPackage.setBody(body);
//
//                    int connId = defaultDataPackage.getLocalConnId();
//                    int corspondConnId = defaultDataPackage.getRemoteConnId();
//
//                    DefaultDataPackage newPkg = PackageUtils.buildDataPackage(corspondConnId, connId, 1L, body);
//                    PackageUtils.printRef("convert to data pkg, ori = ", defaultDataPackage);
//                    pkg = newPkg;
//                    PackageUtils.printRef("convert to data pkg, cur = ", pkg);
//                    break;
//                }
//                default:
//                    break;
//            }
//
//            PackageUtils.printRef("converted package >>>>>>>>>", pkg);
//            ctx.fireChannelRead(pkg);
//        } else {
//            ctx.fireChannelRead(msg);
//        }
//    }
//}
