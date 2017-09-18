package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tunnel 管理类
 *
 * TunnelManager 负责新增加Tunnel，并通过定期任务对Tunnel进行清理
 */
public class TunnelManager {
    private static final Logger LOG = LoggerFactory.getLogger(TunnelManager.class);

    private ConnIdGenerator connIdGenerator = new ConnIdGenerator();

    /** Tunnel相关状态字段 **/
    private Map<Integer, Tunnel> connIdTunnelMap = new ConcurrentHashMap<>();
    private Map<Channel, Tunnel> channelTunnelMap = new ConcurrentHashMap<>();
    private Map<Integer, Tunnel> correspondConnIdTunnelMap = new ConcurrentHashMap<>();
    //用于再清理中加锁，可能不需要
    private Object tunnelStateLock = new Object();

    private WriteService writeService;

    public void newTunnel(Channel channel) {
        //分配connId
        Integer connId = connIdGenerator.generateConnId();

        //new Tunnel
        Tunnel tunnel = new Tunnel(channel, connId, 20, Integer.MAX_VALUE);
        connIdTunnelMap.put(Integer.valueOf(connId), tunnel);
        channelTunnelMap.put(channel, tunnel);
        writeService.addWrite(tunnel);
    }

    public void setWriteService(WriteService writeService) {
        this.writeService = writeService;
    }

    /**
     * 清理不响应的Tunnel
     */
    private void clearIdleTunnels() {
        //Tunnel#shouldColose
        //Tunnel#close()
        //清理Tunnel相关状态
    }

    public void markConnected(int connId, int correspondConnId) {
        Tunnel tunnel = connIdTunnelMap.get(Integer.valueOf(connId));
        tunnel.markConnected(correspondConnId);
        correspondConnIdTunnelMap.put(Integer.valueOf(correspondConnId), tunnel);
    }

    public Tunnel tunnelFor(Channel channel) {
        return channelTunnelMap.get(channel);
    }

    public List<Package> gatherSendPackages() {
        return connIdTunnelMap.values().stream().flatMap( t -> {
            List<Package> packages = Collections.emptyList();
            if(t.getConnected()) {
                packages = t.getWritePackages();
            }
            else if (!t.isWaitConnect()) {
                if(t.getConnectPackage() == null) {
                    //LOG.debug("connect pkg null for tunnel {}", t.getConnId());
                    packages = Collections.emptyList();
                } else {
                    t.setWaitConnect(true);
                    packages = Collections.singletonList(t.getConnectPackage());
                    //LOG.debug("connect pkg = {}", t.getConnectPackage());
                }
            }
            else {
                packages = Collections.emptyList();
            }
            return packages.stream();
        }).collect(Collectors.toList());
    }

    public void scatterRecvPackage(List<Package> avaliableRecvPackages) {
        avaliableRecvPackages.stream().filter(pkg -> pkg instanceof DefaultDataPackage)
                .map(pkg -> DefaultDataPackage.class.cast(pkg))
                .collect(Collectors.toList())
                .forEach(pkg -> {
                    int connId = pkg.getLocalConnId();
                    Tunnel tunnel = tunnelFor(connId);

                    if(tunnel == null && tunnel.shouldClose()) {
                        pkg.toByteBuf().release();
                    } else {
                        tunnel.addRecvPackage(pkg);
                    }
                });
        //connId
        //tunnel#putRecvQueue
    }

    public void tunnelResp(ConnectRespPackage connectRespPackage) {
        int connId = connectRespPackage.getLocalConnId();
        int correspondConnId = connectRespPackage.getRemoteConnId();

        Tunnel tunnel = channelTunnelMap.get(Integer.valueOf(connId));
        if(tunnel != null) {
            if(correspondConnId >= 0) {
                tunnel.setConnect(true);
                tunnel.setCorrespondConnId(correspondConnId);
            } else {
                tunnel.needClose();
            }
        }
    }

    public void tunnelClose(ChannelClosePackage channelClosePackage) {
        int connId = channelClosePackage.getLocalConnId();
        Tunnel tunnel = connIdTunnelMap.get(Integer.valueOf(connId));
        if(tunnel != null) {
            tunnel.needClose();
            writeService.removeWrite(tunnel);
        }
    }

    public Tunnel tunnelFor(int connId) {
        return connIdTunnelMap.get(Integer.valueOf(connId));
    }

    public void touch(Package pkg) {
        int connId = -1;

        //connect req
        if(pkg instanceof ConnectReqPackage) {
            connId = ConnectReqPackage.class.cast(pkg).getConnId();
        }

        //data
        if(pkg instanceof DefaultDataPackage) {
            connId = DefaultDataPackage.class.cast(pkg).getLocalConnId();
        }

        Tunnel tunnel = tunnelFor(connId);
        if(tunnel == null && tunnel.shouldClose()) {
            tunnel.touch(pkg);
        }
    }

    //定时服务相关
    //调度线程池，
    //定时任务runnable

    //ConnIdGenerator

    //addTunnel(Channel channel)
    //addTunnel(Integer connId, Channel channel)
    //getConnId()

    //cleanTunnels()
    //closeTunnel()

    //needCloseCTunnel(Integer connId)
    //shouldCloseTunnel(Integer connId)

    //channelFor(Channel channel) Tunnel

    //getTunnel(int connId)

    //drainSendPackages()

    //AvaliableSendPackages() 对Tunnel遍历，将可以发送的package打包，包括connect package 和 已经连接的Tunnel的data package
}
