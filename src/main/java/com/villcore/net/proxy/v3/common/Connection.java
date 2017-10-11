package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v1.Package;
import com.villcore.net.proxy.v3.pkg.v1.PackageUtils;
import io.netty.channel.Channel;
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

    private static final int SEND_HIGH_WATER_MARKER = 1000;
    private volatile boolean connected;

    private String remoteAddr;
    private int remotePort;
    private Channel remoteChannel;

    //TunnelManager
    private TunnelManager tunnelManager;

    //SendDequeue
    private BlockingDeque<Package> sendQueue = new LinkedBlockingDeque<>();

    //RecvQueue
    private BlockingQueue<Package> recvQueue = new LinkedBlockingQueue<>();

    private volatile int curSendWaterMarker = 0;

    private long lastTouch;

    private long authId = -1L;



    public Connection(String addr, int port, TunnelManager tunnelManager) {
        this.remoteAddr = addr;
        this.remotePort = port;
        this.tunnelManager = tunnelManager;
    }

    public void addSendPackages(List<Package> avaliableSendPackages) {
//        LOG.debug(">>>>>>>>>>>>>>>>>>>>>>{}", avaliableSendPackages.size());
        curSendWaterMarker += avaliableSendPackages.size();
        try {
            avaliableSendPackages.stream().forEach(pkg -> {
                //LOG.debug("pkg == null {}", pkg == null);
                sendQueue.addLast(pkg);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void addSendPackage(Package pkg) {
//        curSendWaterMarker ++;
//        sendQueue.add(pkg);
//    }
//
//    public void addRecvPackages(List<Package> packages) {
//        packages.forEach(pkg -> {
//            recvQueue.add(pkg);
//        });
//    }

    public void addRecvPackage(Package pkg) {
        recvQueue.add(pkg);
    }

    public boolean sendPackageReady() {
        //LOG.debug("cur send water marker = {}", curSendWaterMarker);
        return connected && (curSendWaterMarker <= SEND_HIGH_WATER_MARKER);
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

    public Channel getRemoteChannel() {
        return this.remoteChannel;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void close() {
        tunnelManager.closeConnection(this);
        LOG.debug("%%%%%%%%%%%%%%%%%%%%%connection close...");
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
            //LOG.debug("connection write...{}, remoteChannel == null ? {}", false, remoteChannel == null);
            return false;
        }
        //LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> connection send ->>{}", pkg.toByteBuf().readableBytes());
        //remoteChannel.writeAndFlush(pkg);
        remoteChannel.write(pkg);
        PackageUtils.printRef("connection write pkg ------------------"+getClass().getSimpleName(), pkg);

        //remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
        connectionTouch(System.currentTimeMillis());
//        LOG.debug("connection write...{}", true);
        return true;
    }

//    @Override
//    public void touch(Package pkg) {
//        lastTouch = System.currentTimeMillis();
//        tunnelManager.touch(pkg);
//        LOG.debug("connection touch...{}", true);
//    }

    @Override
    public void touch(int tunnelId) {
        lastTouch = System.currentTimeMillis();
        tunnelManager.touch(tunnelId);
//        LOG.debug("connection touch...{}", true);
    }

    @Override
    public void failWrite(Package pkg) {
        curSendWaterMarker ++;
        sendQueue.addFirst(pkg);
        //LOG.debug("connection faile write ...{}", true);
    }

    @Override
    public List<Package> getWritePackages() {
        //LOG.debug("connection get write packages ...{}", true);

        List<Package> packages = new LinkedList<>();
        sendQueue.drainTo(packages);
        curSendWaterMarker -= packages.size();
        return packages;
    }

    @Override
    public void flush() {
        remoteChannel.flush();
    }
}
