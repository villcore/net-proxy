package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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

    //本地端对应的connId
    private Integer connId;

    //远端对应的ConnId
    private Integer correspondConnId = -1;

    //sendQueueSize
    private int sendQueueSize;

    //recvQueueSize
    private int recvQueueSize;

    //client side 用于建立连接的Package
    private ConnectReqPackage connectPackage;

    //数据队列（线程安全的双端队列）
    private BlockingQueue<Package> sendQueue;

    //接收到的Package, 由send service 写入channel
    private BlockingQueue<Package> recvQueue;

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

    public Tunnel(Channel channel, Integer connId, int sendQueueSize, int recvQueueSize) {
        this.channel = channel;
        this.connId = connId;
        this.sendQueueSize = sendQueueSize;
        this.recvQueueSize = recvQueueSize > 0 ? recvQueueSize : Integer.MAX_VALUE;

        this.sendQueue = new LinkedBlockingQueue<>(sendQueueSize + 2); //extra two more space);
        this.recvQueue = new LinkedBlockingQueue<>(recvQueueSize);
    }

    public Integer getConnId() {
        return connId;
    }

    public boolean sendQueueIsFull() {
        return sendQueue.size() >= sendQueueSize ;
    }

    public boolean getConnected() {
        return connected;
    }

    public boolean isWaitConnect() {
        return waitConnect;
    }

    public void setWaitConnect(boolean waitConnect) {
        this.waitConnect = waitConnect;
    }

    public ConnectReqPackage getConnectPackage() {
        return this.connectPackage;
    }

    public void setConnectPackage(ConnectReqPackage connectPackage) {
        LOG.debug("set connect package ...{}", connId);
        this.connectPackage = connectPackage;
    }

    public void addSendPackage(Package dataPackage) {
        //LOG.debug("add send package for [{}]...,", connId);
        sendQueue.add(dataPackage);
    }

    public List<Package> drainSendPackages() {
        if(!connected) {
            return Collections.emptyList();
        }
        List<Package> avaliablePackages = new LinkedList<>();
        sendQueue.drainTo(avaliablePackages);
        //LOG.debug("drain send package, size = {}", avaliablePackages.size());
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
        LOG.debug("cur tunnel send queue size = {}", sendQueue.size());
        this.connected = connect;
        if(connect) {
            channel.config().setAutoRead(true);
        }
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

        if (sendQueueSize != tunnel.sendQueueSize) return false;
        if (recvQueueSize != tunnel.recvQueueSize) return false;
        if (!channel.equals(tunnel.channel)) return false;
        return connId.equals(tunnel.connId);
    }

    @Override
    public int hashCode() {
        int result = sendQueueSize;
        result = 31 * result + recvQueueSize;
        result = 31 * result + channel.hashCode();
        result = 31 * result + connId.hashCode();
        return result;
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
            channel.writeAndFlush(pkg.getBody());
            DefaultDataPackage dataPackage = DefaultDataPackage.class.cast(pkg);
            LOG.debug("!!! write data pkg [{}] --> [{}]", dataPackage.getRemoteConnId(), dataPackage.getLocalConnId());
//            try {
//                LOG.debug("write pkg to {} >>>>>>>>>>\n [{}]\n... success...", connId, PackageUtils.toString(pkg.getBody()));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
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
        LOG.debug("rebuild send packages ... {}, queue size = {}", correspondConnId, sendQueue.size());
        //LOG.debug("drain send package size = {}, connected = {}", drainSendPackages().size(), connected);
        drainSendPackages().stream().map(pkg -> DefaultDataPackage.class.cast(pkg))
                .collect(Collectors.toList())
                .forEach(pkg -> {
                    int localConnId = pkg.getLocalConnId();
                    ByteBuf data = pkg.getBody();
                    Package correctPkg = PackageUtils.buildDataPackage(localConnId, correspondConnId, 1L, data);
                    sendQueue.add(correctPkg);
                    LOG.debug("correct pkg = {}", correctPkg);
                });
    }
}
