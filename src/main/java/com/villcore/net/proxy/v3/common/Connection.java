package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 主要的通信连接，该连接主要面向数据传输，从上层的虚拟通道
 */
public class Connection extends BasicWriteableImpl {
    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    private int highWater = 1000;
    private volatile boolean connected;

    private String remoteAddr;
    private int remotePort;
    private Channel remoteChannel;

    //EventLoop
    private EventLoopGroup eventLoopGroup;

    //TunnelManager
    private TunnelManager tunnelManager;

    //SendDequeue
    private BlockingDeque<Package> sendQueue = new LinkedBlockingDeque<>();

    //RecvQueue
    private BlockingQueue<Package> recvQueue = new LinkedBlockingQueue<>();

    //authorized
    //负责管理与远端的连接,重试
    //维护sendQueue
    //提供drainSendPcakges
    //提供sendPackages方法供SendService调用，该方法在链路断开后会直接返回
    //

    //drainRecvPackage
    //addSendPackage

    //isReady()

    //AvaliableSendPackages() 调用TunnelManager#avaliablePackages

    //connectRemoteServer()
    //closeHandler
    private volatile int waterMarker = 0;

    private long lastTouch;

    public Connection(String addr, int port, TunnelManager tunnelManager) {
        this.remoteAddr = addr;
        this.remotePort = port;
        this.tunnelManager = tunnelManager;
    }

    public void addSendPackages(List<Package> avaliableSendPackages) {
        waterMarker += avaliableSendPackages.size();
        avaliableSendPackages.stream().forEach(pkg -> sendQueue.addLast(pkg));
    }

    public void addRecvPackages(List<Package> packages) {
        packages.forEach(pkg -> {
            recvQueue.add(pkg);
        });
    }

    public boolean sendPackageReady() {
        return connected && (waterMarker <= highWater);
    }

    public List<Package> getRecvPackages() {
        List<Package> recvPackages = new LinkedList<>();
        recvQueue.drainTo(recvPackages);
        return recvPackages;
    }

    public void connectionTouch(long time) {
        lastTouch = time;
    }

    public long lastTouch() {
        return lastTouch;
    }

    public void setRemoteChannel(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void close() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Connection that = (Connection) o;

        if (remotePort != that.remotePort) return false;
        return remoteAddr.equals(that.remoteAddr);
    }

    @Override
    public int hashCode() {
        int result = remoteAddr.hashCode();
        result = 31 * result + remotePort;
        return result;
    }

    /** sendable **/
    @Override
    public boolean canWrite() {
        //LOG.debug("connection can write...{}", connected);
        return connected;
    }

    @Override
    public boolean write(Package pkg) {
        if(remoteChannel == null || !remoteChannel.isOpen()) {
            LOG.debug("connection write...{}, remoteChannel == null ? {}", false, remoteChannel == null);
            return false;
        }
        remoteChannel.write(pkg);
        connectionTouch(System.currentTimeMillis());
        LOG.debug("connection write...{}", true);
        return true;
    }

    @Override
    public void touch(Package pkg) {
        lastTouch = System.currentTimeMillis();
        tunnelManager.touch(pkg);
        //LOG.debug("connection touch...{}", true);
    }

    @Override
    public void failWrite(Package pkg) {
        waterMarker ++;
        sendQueue.addFirst(pkg);
        //LOG.debug("connection faile write ...{}", true);
    }

    @Override
    public List<Package> getWritePackages() {
        //LOG.debug("connection get write packages ...{}", true);

        List<Package> packages = new LinkedList<>();
        sendQueue.drainTo(packages);
        waterMarker -= packages.size();
        return packages;
    }
}
