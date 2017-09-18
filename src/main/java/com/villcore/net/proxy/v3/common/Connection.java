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
    private Channel remoteChannel;

    public void addSendPackages(List<Package> avaliableSendPackages) {
        waterMarker += avaliableSendPackages.size();
        avaliableSendPackages.stream().forEach(pkg -> sendQueue.addLast(pkg));
    }

    public boolean sendPackageReady() {
        return connected && (waterMarker <= highWater);
    }

    public List<Package> getRecvPackages() {
        List<Package> recvPackages = new LinkedList<>();
        recvQueue.drainTo(recvPackages);
        return recvPackages;
    }

    /** sendable **/
    @Override
    public boolean canWrite() {
        return connected;
    }

    @Override
    public boolean write(Package pkg) {
        if(remoteChannel == null || !remoteChannel.isOpen()) {
            return false;
        }
        remoteChannel.write(pkg);
        return true;
    }

    @Override
    public void touch(Package pkg) {
        lastTouch = System.currentTimeMillis();
        tunnelManager.touch(pkg);
    }

    @Override
    public void failWrite(Package pkg) {
        sendQueue.addFirst(pkg);
    }

    @Override
    public List<Package> getWritePackages() {
        List<Package> packages = new LinkedList<>();
        sendQueue.drainTo(packages);
        return packages;
    }
}
