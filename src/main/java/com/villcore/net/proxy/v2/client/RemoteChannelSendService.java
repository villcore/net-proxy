//package com.villcore.net.proxy.v2.client;
//
//import com.villcore.net.proxy.v2.pkg.ConnectPackage;
//import com.villcore.net.proxy.v2.pkg.DefaultDataPackage;
//import com.villcore.net.proxy.v2.pkg.Package;
//import io.netty.channel.socket.nio.NioSocketChannel;
//
//import java.util.List;
//
//public class RemoteChannelSendService extends ChannelSendService {
//
//    private ConnectionManager connectionManager;
//
//    public RemoteChannelSendService(PackageQeueu packageQeueu, ConnectionManager connectionManager) {
//        super(packageQeueu);
//        this.connectionManager = connectionManager;
//    }
//
//    @Override
//    protected void sendPackages(List<Package> packages) throws Exception {
//        for(Package pkg : packages) {
//            //取得localConnId
//            //找到相应channel
//            //发送
//            int localConnId = -1;
//            if(pkg instanceof ConnectPackage) {
//                ConnectPackage connectPackage = (ConnectPackage) pkg;
//                localConnId = connectPackage.getLocalConnectionId();
//            }
//
//            if(pkg instanceof DefaultDataPackage) {
//                DefaultDataPackage defaultDataPackage = (DefaultDataPackage) pkg;
//                localConnId = defaultDataPackage.getLocalConnectionId();
//            }
//
//            NioSocketChannel channel = connectionManager.getChannel(localConnId);
//            if(channel == null) {
//                continue;
//            }
//            channel.writeAndFlush(pkg.toByteBuf());
//        }
//    }
//
//    @Override
//    protected List<Package> processPackages(List<Package> packages) throws Exception {
//        return packages;
//    }
//}
