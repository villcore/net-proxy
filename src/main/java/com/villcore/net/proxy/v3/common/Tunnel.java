package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import io.netty.channel.Channel;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * client 与 server 对每个连接的代理通道
 */
public class Tunnel extends BasicWriteableImpl {

    //tunnelManager
    private TunnelManager tunnelManager;

    //sendQueueSize
    private int sendQueueSize;
    private int recvQueueSize;

    //代理通道 Channel
    private Channel channel;

    //本地端对应的connId
    private Integer connId;

    //远端对应的ConnId
    private Integer correspondConnId = -1;

    private ConnectReqPackage connectPackage;

    //数据队列（线程安全的双端队列）
    private BlockingQueue<Package> sendQueue;

    //接收到的Package, 由send service 写入channel
    private BlockingQueue<Package> recvQueue;

    //lasttouch
    private volatile long lastTouch;

    //是否与远端建立连接
    private volatile boolean connected = false;

    //是否应该关闭
    private volatile boolean shouldClose = false;

    //wait for connect
    private volatile boolean waitConnect = false;

    public Tunnel(Channel channel, Integer connId, int sendQueueSize, int recvQueueSize) {
        this.channel = channel;
        this.connId = connId;
        this.sendQueueSize = sendQueueSize;
        this.recvQueueSize = recvQueueSize > 0 ? recvQueueSize : Integer.MAX_VALUE;

        this.sendQueue = new LinkedBlockingQueue<>(sendQueueSize);
        this.recvQueue = new LinkedBlockingQueue<>(recvQueueSize);
    }

    public void markConnected(int correspondConnId) {
        this.correspondConnId = correspondConnId;
        this.connected = true;
    }

    public Integer getConnId() {
        return connId;
    }

    public boolean shouldClose() {
        return shouldClose;
    }

    public boolean sendQueueIsFull() {
        return sendQueue.isEmpty();
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
        this.connectPackage = connectPackage;
    }

    public void addSendPackage(DefaultDataPackage dataPackage) {
        sendQueue.add(dataPackage);
    }

    public List<Package> drainSendPackages() {
        List<Package> avaliablePackages = new LinkedList<>();
        sendQueue.drainTo(avaliablePackages);
        System.out.println(avaliablePackages.size());
        return avaliablePackages;
    }
    //isFull()
    //readPackage(Package) -> sendQueue
    //writePackage() 返回已经准备好的package，如果connedted == false， 只返回连接请求package

    //channelOpen()

    //avaliableSendPackage


    public void needClose() {
        this.shouldClose = true;
    }

    public void setConnect(boolean connect) {
        this.connected = connect;
    }

    public void setCorrespondConnId(int correspondConnId) {
        this.correspondConnId = correspondConnId;
    }

    public void addRecvPackage(DefaultDataPackage pkg) {
        recvQueue.add(pkg);
    }


    /** sendable **/

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public boolean write(Package pkg) {
        if(channel == null || !channel.isOpen()) {
            pkg.toByteBuf().release();
        } else {
            channel.writeAndFlush(pkg);
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
        return drainSendPackages();
    }
}
