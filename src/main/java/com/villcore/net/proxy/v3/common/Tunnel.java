package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * client 与 server 对每个连接的代理通道
 */
public class Tunnel extends BasicWriteableImpl {
    private static final Logger LOG = LoggerFactory.getLogger(Tunnel.class);

    public static final int MAX_READ_WATER_MARKER = 100;
    private volatile int curReadWaterMarker = 0;

    //本地端对应的connId
    private volatile int connId;

    //远端对应的ConnId
    private volatile int correspondConnId = -1;

    //sendQueueSize
    //private int sendQueueSize;

    //recvQueueSize
    //private int recvQueueSize;

    //client side 用于建立连接的Package
    private ConnectReqPackage connectPackage;

    //数据队列（线程安全的双端队列）
    private BlockingQueue<Package> sendQueue = new LinkedBlockingQueue<>();

    //接收到的Package, 由send service 写入channel
    private BlockingQueue<Package> recvQueue = new LinkedBlockingQueue<>();

    //代理通道 Channel
    private Channel channel;

    //lasttouch
    private volatile long lastTouch;

    //是否与远端建立连接
    private volatile boolean connected = false;

    //是否应该关闭
    private volatile boolean shouldClose = false;

    //wait for connect
    private volatile boolean waitConnect = false;

    //bind connection, 该Tunnel绑定的Connection
    private Connection bindConnection;

    //https
    private boolean isHttps;

    public Tunnel(Channel channel, Integer connId) {
        this.channel = channel;
        this.connId = connId;
    }

    public Integer getConnId() {
        return connId;
    }

//    public boolean sendQueueIsFull() {
//        return sendQueue.size() >= sendQueueSize ;
//    }

    public boolean getConnected() {
        return connected;
    }

//    public boolean isWaitConnect() {
//        return waitConnect;
//    }
//
//    public void setWaitConnect(boolean waitConnect) {
//        this.waitConnect = waitConnect;
//    }

    public ConnectReqPackage getConnectPackage() {
        return this.connectPackage;
    }

    public void setConnectPackage(ConnectReqPackage connectPackage) {
//        LOG.debug("set connect package ...{}", connId);
        if(connectPackage != null) {
            LOG.debug("tunnel [{}] need to cennect [{}:{}]", connId, connectPackage.getHostname(), connectPackage.getPort());
        }
        this.connectPackage = connectPackage;
    }

    public void addSendPackage(Package dataPackage) {
        //LOG.debug("add send package for [{}]...,", connId);
        sendQueue.add(dataPackage);
        incReadWaterMarker(1);
        resetReadState();
    }

    public List<Package> drainSendPackages() {
//        if(!connected) {
//            return Collections.emptyList();
//        }
        List<Package> avaliablePackages = new LinkedList<>();
        sendQueue.drainTo(avaliablePackages);
        //LOG.debug("drain send package, size = {}", avaliablePackages.size());
        deincReadWaterMarker(avaliablePackages.size());
        resetReadState();
        return avaliablePackages;
    }

    public List<Package> drainRecvPackages() {
        if(!connected) {
            return Collections.emptyList();
        }
        List<Package> avaliablePackages = new LinkedList<>();
        recvQueue.drainTo(avaliablePackages);
        //LOG.debug("drain recv package, size = {}", avaliablePackages.size());
        return avaliablePackages;
    }

    public void needClose() {
        this.shouldClose = true;
    }

    public boolean shouldClose() {
        return shouldClose;
    }

    public void setConnect(boolean connect) {
        //LOG.debug("cur tunnel send queue size = {}", sendQueue.size());
        this.connected = connect;
//        if(connect) {
//            channel.config().setAutoRead(true);
//        }
    }

    public void setCorrespondConnId(int correspondConnId) {
        this.correspondConnId = correspondConnId;
    }

    public void addRecvPackage(DefaultDataPackage pkg) {
        recvQueue.add(pkg);
    }

    public Connection getBindConnection() {
        return bindConnection;
    }

    public void setBindConnection(Connection bindConnection) {
        this.bindConnection = bindConnection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tunnel tunnel = (Tunnel) o;

        return connId == tunnel.connId;
    }

    @Override
    public int hashCode() {
        return connId;
    }

    /** sendable **/

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public boolean write(Package pkg) {
        //LOG.debug("write pkg ...");
        if(channel == null || !channel.isOpen()) {
            //LOG.debug("write pkg ...failed ...");
            pkg.toByteBuf().release();
        } else {
            DefaultDataPackage dataPackage = DefaultDataPackage.class.cast(pkg);
            int connId = dataPackage.getLocalConnId();
            int corrspondConnId = dataPackage.getRemoteConnId();
            int bytes = dataPackage.getBody().readableBytes();
            channel.writeAndFlush(pkg.getBody());

            //LOG.debug("!!! write data pkg [{}] --> [{}]", dataPackage.getRemoteConnId(), dataPackage.getLocalConnId());


            LOG.debug("tunnel [{}] -> [{}] need send {} bytes ...", corrspondConnId, connId, bytes);

            try {
                //LOG.debug("write pkg to {} >>>>>>>>>>\n [{}]\n... success...", connId, PackageUtils.toString(pkg.getBody()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void touch(Package pkg) {
        lastTouch = System.currentTimeMillis();
    }

    @Override
    public void failWrite(Package pkg) {
    }

    @Override
    public List<Package> getWritePackages() {
        return drainRecvPackages();
    }

    public long getLastTouch() {
        return lastTouch;
    }

    public Channel getChannel() {
        return channel;
    }

    public Integer getCorrespondConnId() {
        return correspondConnId;
    }

    public void rebuildSendPackages(int correspondConnId) {
        //LOG.debug("rebuild send packages ... {}, queue size = {}", correspondConnId, sendQueue.size());
        //LOG.debug("drain send package size = {}, connected = {}", drainSendPackages().size(), connected);
        drainSendPackages().stream().map(pkg -> DefaultDataPackage.class.cast(pkg))
                .collect(Collectors.toList())
                .forEach(pkg -> {
                    int localConnId = pkg.getLocalConnId();

                    ByteBuf data = pkg.getBody();
                    Package correctPkg = PackageUtils.buildDataPackage(localConnId, correspondConnId, 1L, data);
                    sendQueue.add(correctPkg);
                    //LOG.debug("correct pkg = {}", correctPkg);
                });
    }


    /***以下两个方法用在client端, 用于在读取到ConnectPackage后,等待服务端建立连接之前,规避读取数据
     * 增加复杂性。
     */

     /**
     * 等待服务端Tunnel成功连接, 该操作会将对应的Channel 自动读取关闭
     */
    public void waitTunnelConnect() {
        channel.config().setAutoRead(false);
    }

    /**
     * 服务端建立连接, 重新打开对应Channel自动读取功能
     */
    public void tunnelConnected() {
        channel.config().setAutoRead(true);
    }

    /*** 以下两个方法用于client与server, 用来做流量控制, 防止在读取大量数据不能及时发送(如http下载),
     * 造成网卡跑满,或者内存OOM
     */
    public void incReadWaterMarker(int val) {
        curReadWaterMarker += val;
    }

    public void deincReadWaterMarker(int val) {
        curReadWaterMarker -= val;
    }

    public boolean readWaterMarkerSafe() {
        return curReadWaterMarker < MAX_READ_WATER_MARKER;
    }

    public void resetReadState() {
        if(readWaterMarkerSafe()) {
            channel.config().setAutoRead(true);
        } else {
            channel.config().setAutoRead(false);
        }
    }

    public void stopRead() {
        channel.config().setAutoRead(false);
    }

    public void close() {
        waitTunnelConnect();
        drainRecvPackages();
        drainSendPackages();
        channel.close().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess()) {
                    LOG.debug("tunnel [{}] close ...", connId);
                }
            }
        });
    }

    public boolean isHttps() {
        return isHttps;
    }

    public void setHttps(boolean https) {
        isHttps = https;
    }
}
