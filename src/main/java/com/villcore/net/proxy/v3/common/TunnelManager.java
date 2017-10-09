package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tunnel 管理类
 * <p>
 * TunnelManager 负责新增加Tunnel，并通过定期任务对Tunnel进行清理
 */
public class TunnelManager implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(TunnelManager.class);

    private static final long MAX_TOUCH_INTERVAL = 30 * 1000L;

    private ConnIdGenerator connIdGenerator;

    /**
     * Tunnel相关状态字段
     **/
    private Map<Integer, Tunnel> connIdTunnelMap = new ConcurrentHashMap<>();
    private Map<Channel, Tunnel> channelTunnelMap = new ConcurrentHashMap<>();
    private Map<Connection, Set<Tunnel>> connectionSetMap = new ConcurrentHashMap<>();

    private Object stateLock = new Object();

    private WriteService writeService;

    public TunnelManager(int idStart) {
        connIdGenerator = new ConnIdGenerator(idStart);
    }

    public void setWriteService(WriteService writeService) {
        this.writeService = writeService;
    }

    public Tunnel newTunnel(Channel channel) {
        if(channel == null) {
            throw new IllegalArgumentException("channe can not be null ...");
        }
        //分配connId
        Integer connId = connIdGenerator.generateConnId();
        Tunnel tunnel = newTunnel(channel, connId);
        return tunnel;
    }

    // sync
    private Tunnel newTunnel(Channel channel, Integer connId) {
        synchronized (stateLock) {
            //new Tunnel
            Tunnel tunnel = new Tunnel(writeService, channel, new Integer(connId));
            connIdTunnelMap.put(new Integer(connId), tunnel);
            channelTunnelMap.put(channel, tunnel);
            writeService.addWrite(tunnel);
            return tunnel;
        }
    }

    // sync
    public void bindConnection(Connection connection, Tunnel tunnel) {
        synchronized (stateLock) {
            Set<Tunnel> tunnels = connectionSetMap.getOrDefault(connection, Collections.synchronizedSet(new HashSet<>()));
            tunnels.add(tunnel);
            connectionSetMap.put(connection, tunnels);
        }
    }

    // sync
    public Tunnel tunnelFor(Channel channel) {
        synchronized (stateLock) {
            return channelTunnelMap.get(channel);
        }
    }

    //sync
    public List<Package> gatherSendPackages(Connection connection) {
        //LOG.debug(connectionSetMap.get(connection).toString());
        Set<Tunnel> tunnelSet = Collections.EMPTY_SET;
        synchronized (stateLock) {
            tunnelSet = connectionSetMap.getOrDefault(connection, Collections.synchronizedSet(new HashSet<>()));
            //LOG.debug("cur tunnel size = {}", tunnelSet.size());

            return tunnelSet.stream().flatMap(t -> {
                //LOG.debug("tunner[{}] -> send", t.getConnId());
                List<Package> packages = Collections.emptyList();
                if (t.getConnected()) {
                    packages = t.drainSendPackages();
                    //LOG.debug("tunnel [{}] write package sizle = [{}]", t.getConnId(), packages.size());
                } else {
                    //LOG.debug("tunnel connect package is null ? {}", t.getConnectPackage() == null);
                    if (t.getConnectPackage() != null) {
                        ConnectReqPackage oriConnectReqPackage = t.getConnectPackage();
                        try {
                            ConnectReqPackage connectReqPackage =
                                    PackageUtils.buildConnectPackage(oriConnectReqPackage.getHostname(), (short) oriConnectReqPackage.getPort(), t.getConnId(), 1L);
                            packages = Collections.singletonList(connectReqPackage);
                            t.setConnectPackage(null);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        //LOG.debug("connect pkg == null {}", t.getConnectPackage());
                        //packages = Collections.emptyList();
                    }
                }
                return packages.stream();
            }).collect(Collectors.toList());
        }
    }

    //TODO need sync
    /**
     * 该方法接收DefaultDataPackage, 会丢弃其他的Package
     *
     * @param avaliableRecvPackages
     */
    public void scatterRecvPackage(List<Package> avaliableRecvPackages) {
        List<DefaultDataPackage> dataPackages = avaliableRecvPackages.stream()
                .filter(pkg -> pkg instanceof DefaultDataPackage)
                .map(pkg -> DefaultDataPackage.class.cast(pkg))
                .collect(Collectors.toList());

                dataPackages.forEach(pkg -> {
                    int connId = pkg.getLocalConnId();
                    int corrspondConnId = pkg.getRemoteConnId();

                    Tunnel tunnel = tunnelFor(connId);
//                    LOG.debug("tunnel [{}] -> [{}] need send {} bytes ...", corrspondConnId, connId, pkg.getBody().readableBytes());
//                    try {
//                        LOG.debug("connId = {}, corrspondConnId = {}, recv {x}", connId, corrspondConnId/*, PackageUtils.toString(pkg)*/);
//                        LOG.debug("search tunnel = {}", tunnel == null ? " null" : tunnel.getConnId());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                    if (tunnel == null || tunnel.shouldClose()) {
                        pkg.toByteBuf().release();
                        //LOG.debug("tunnel == null or tunnel should close...");
                    } else {
                        tunnel.addRecvPackage(pkg);
                        //tunnel.getChannel().writeAndFlush(pkg.getBody());
                        //LOG.debug("tunnel[{}] connected [{}] add recv package ...", tunnel.getConnId(), tunnel.getConnected());
                    }
                });

                dataPackages.clear();
                dataPackages = null;
        //connId
        //tunnel#putRecvQueue
    }

    //sync
    public void tunnelClose(int connId) {
        Tunnel tunnel = connIdTunnelMap.get(new Integer((connId)));
        if (tunnel != null) {
            tunnel.needClose();
            writeService.removeWrite(tunnel);
        }
    }

    //sync
    public Tunnel tunnelFor(int connId) {
        synchronized (stateLock) {
            //LOG.debug("search id = [{}], connIdTunnel map = {}", connId, connIdTunnelMap.toString());
            return connIdTunnelMap.get(new Integer((connId)));
        }
    }

//    public void touch(Package pkg) {
//        int connId = -1;
//
//        //connect req
//        if (pkg instanceof ConnectReqPackage) {
//            connId = ConnectReqPackage.class.cast(pkg).getConnId();
//        }
//
//        //data
//        if (pkg instanceof DefaultDataPackage) {
//            connId = DefaultDataPackage.class.cast(pkg).getLocalConnId();
//        }
//
//        Tunnel tunnel = tunnelFor(connId);
//        if (tunnel != null && !tunnel.shouldClose()) {
//            tunnel.touch(pkg);
//        } else {
//            //pkg.toByteBuf().release();
//        }
//    }

    public void touch(int tunnelId) {
        int connId = -1;

//        //connect req
//        if (pkg instanceof ConnectReqPackage) {
//            connId = ConnectReqPackage.class.cast(pkg).getConnId();
//        }
//
//        //data
//        if (pkg instanceof DefaultDataPackage) {
//            connId = DefaultDataPackage.class.cast(pkg).getLocalConnId();
//        }
//
//        Tunnel tunnel = tunnelFor(connId);
//        if (tunnel != null && !tunnel.shouldClose()) {
//            tunnel.touch(pkg);
//        } else {
//            //pkg.toByteBuf().release();
//        }

        Tunnel tunnel = tunnelFor(connId);
        if (tunnel != null && !tunnel.shouldClose()) {
            tunnel.touch(tunnelId);
        } else {
            //pkg.toByteBuf().release();
        }

    }

    //sync
    public void addConnection(Connection connection) {
        synchronized (stateLock) {
            connectionSetMap.put(connection, Collections.synchronizedSet(new HashSet<>()));
        }
    }

    private long lastTouchInterval(long lastTouch) {
        return System.currentTimeMillis() - lastTouch;
    }

    //sync
    public void closeConnection(Connection connection) {
        synchronized (stateLock) {
            Set<Tunnel> tunnelSet = connectionSetMap.remove(connection);
            if(tunnelSet == null) {
                tunnelSet = Collections.EMPTY_SET;
            }

            tunnelSet.forEach(t -> {
                connIdTunnelMap.remove(t.getConnId());
                channelTunnelMap.remove(t.getChannel());
                t.close();
            });
//            connection.getWritePackages().forEach(pkg -> pkg.toByteBuf().release());
//            connection.getRecvPackages().forEach(pkg -> pkg.toByteBuf().release());
            connection.getWritePackages().forEach(pkg -> PackageUtils.release(pkg));
            connection.getRecvPackages().forEach(pkg -> PackageUtils.release(pkg));
            writeService.removeWrite(connection);
        }
    }

    @Override
    public void run() {
        clean();
    }

    //sync
    private void clean() {
        synchronized (stateLock) {
            try {
            connIdTunnelMap.values().stream()
                    .filter(t -> lastTouchInterval(t.getLastTouch()) > MAX_TOUCH_INTERVAL)
                    .collect(Collectors.toList()).forEach(t -> t.needClose());

            List<Tunnel> needClearTunners = connIdTunnelMap.values().stream().filter(t -> t.shouldClose()).collect(Collectors.toList());
            for (Tunnel tunnel : needClearTunners) {
                Integer connId = tunnel.getConnId();
                Channel channel = tunnel.getChannel();

                Connection connection = tunnel.getBindConnection();

                connIdTunnelMap.remove(connId);
                connectionSetMap.getOrDefault(connection, Collections.EMPTY_SET).remove(tunnel);
                tunnel.close();
                channelTunnelMap.remove(channel);
                if(channelTunnelMap.size() == 0) {
                    connection.getWritePackages().forEach(pkg -> pkg.toByteBuf().release());
                    connection.getRecvPackages().forEach(pkg -> pkg.toByteBuf().release());
                }
            }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            LOG.debug("clean tunnels ... alive tunnels = {}, {}", connIdTunnelMap.size(), channelTunnelMap.size());

        }
    }
}
