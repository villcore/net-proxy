//package com.villcore.net.proxy.v3.client;
//
//import com.villcore.net.proxy.v2.client.ConnectionManager;
//import com.villcore.net.proxy.v2.client.PackageQeueu;
//import com.villcore.net.proxy.v2.pkg.ConnectRespPackage;
//import com.villcore.net.proxy.v2.pkg.DefaultDataPackage;
//import com.villcore.net.proxy.v2.pkg.Package;
//import com.villcore.net.proxy.v2.pkg.PackageType;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.socket.nio.NioSocketChannel;
//
//@ChannelHandler.Sharable
//public class RemoteReadHandler extends ChannelInboundHandlerAdapter {
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if(msg instanceof Package) {
//            Package pkg = (Package) msg;
//
//            ByteBuf header = pkg.getHeader().copy();
//            ByteBuf body = pkg.getBody().copy();
////                LOG.debug(">>>>>>>>>>>>>>>>client recv package = {}", PackageUtils.toString(pkg));
//
//            short pkgType = pkg.getPkgType();
//            if (pkgType == PackageType.PKG_CONNECT_RESP) {
//                ConnectRespPackage connectRespPackage = new ConnectRespPackage();
//                connectRespPackage.setHeader(header);
//                connectRespPackage.setBody(body);
//
//                //ConnectRespPackage connectRespPackage = (ConnectRespPackage) msg;
//                int localConnId = connectRespPackage.getLocalConnId();
//                int remoteConnId = connectRespPackage.getRemoteConnId();
//                NioSocketChannel socketChannel = connectionManager.getChannel(localConnId);
//
//                if(remoteConnId == -1) {
//                    socketChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
//                    connectionManager.closeConnection(socketChannel);
//                    return;
//                }
//
//                connectionManager.channelConnected(socketChannel);
//                connectionManager.makeConnectionMap(localConnId, remoteConnId);
//                for(Package dataPkg : connectionManager.drainPendingPackages(socketChannel)) {
//                    DefaultDataPackage defaultDataPackage = (DefaultDataPackage) dataPkg;
//                    defaultDataPackage.setRemoteConnId(remoteConnId);
//                    //LOG.debug("recv resp package, drain data package to write queue...{}", PackageUtils.toString(dataPkg));
//                    sendQueue.putPackage(dataPkg);
//                }
//                connectionManager.touch(localConnId);
//            }
//
//            if(pkgType == PackageType.PKG_DEFAULT_DATA) {
//                DefaultDataPackage dataPackage = new DefaultDataPackage();
//                dataPackage.setHeader(header);
//                dataPackage.setBody(body);
////                    DefaultDataPackage dataPackage = (DefaultDataPackage) msg;
//                recvQueue.putPackage(dataPackage);
//            }
//        } else {
//            ctx.fireChannelRead(msg);
//        }
//    }
//};
//
