package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionManager implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private final AtomicInteger idCount = new AtomicInteger(0);
    private final Map<Integer, NioSocketChannel> channelMap = new Hashtable<>();
    private final Map<NioSocketChannel, Integer> connIdMap = new Hashtable<>();
    private final Map<Integer, Long> lastTouchMap = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> connectionMap = new ConcurrentHashMap<>();

    private final Map<NioSocketChannel, Boolean> channelConnectMap = new ConcurrentHashMap<>();
    private final Map<NioSocketChannel, PackageQeueu> pendingPackage = new ConcurrentHashMap<>();

    private Object updateLock = new Object();
    private static long idleScanInterval = 120 * 1000;
    private volatile boolean running;

    public void start() {
        running = true;
    }

    /**
     * 将待发送的Package放到待发送队列，该方法主要使用在浏览器第一次建立连接，将连接请求同内容一同发给代理端，
     * 此时先处理连接信息，待连接建立后，发送排队的信息
     *
     * @param channel
     * @param pkg
     * @throws InterruptedException
     */
    public void pendingPackage(NioSocketChannel channel, Package pkg) throws InterruptedException {
        PackageQeueu packageQeueu = pendingPackage.getOrDefault(channel, new PackageQeueu(100));
        packageQeueu.putPackage(pkg);
        pendingPackage.putIfAbsent(channel, packageQeueu);
    }

    /**
     * 连接建立之后提取所有排队Package
     *
     * @param channel
     * @return
     */
    public List<Package> drainPendingPackages(NioSocketChannel channel) {
        PackageQeueu packageQeueu = pendingPackage.getOrDefault(channel, new PackageQeueu(100));
        return packageQeueu.drainPackage();
    }

    /**
     * 连接通道建立，将状态置为ture
     *
     * @param channel
     */
    public void channelConnected(NioSocketChannel channel) {
        channelConnectMap.putIfAbsent(channel, Boolean.TRUE);
    }

    /**
     * 查询连接状态
     *
     * @param channel
     * @return
     */
    public boolean isChannelConnected(NioSocketChannel channel) {
        return channelConnectMap.getOrDefault(channel, Boolean.FALSE);
    }

    /**
     * 返回通道对应的connId
     *
     * @param channel
     * @return
     */
    public Integer getConnId(NioSocketChannel channel) {
        return connIdMap.get(channel);
    }

    /**
     * 添加新建的SocketChannel, 并返回分配的connId
     *
     * @param channel
     * @return
     */
    public Integer addConnection(NioSocketChannel channel) {
        Integer connId = generateConnId();
        synchronized (updateLock) {
            channelMap.put(connId, channel);
            connIdMap.put(channel, connId);
            lastTouchMap.putIfAbsent(connId, Long.valueOf(System.currentTimeMillis()));
        }
        return connId;
    }

    /**
     * 保存connId对应关系
     *
     * @param connId
     * @param connId2
     */
    public void makeConnectionMap(int connId, int connId2) {
        connectionMap.put(Integer.valueOf(connId), Integer.valueOf(connId2));
    }

    /**
     * 根据一方connId，查找对应的connId
     *
     * @param connId
     * @return
     */
    public int getConnectionMap(int connId) {
        return connectionMap.getOrDefault(connId, -1);
    }

    /**
     * 自增生成一个唯一connId，该Id只能在该Jvm维持唯一（数值越界不考虑）
     *
     * @return
     */
    public Integer generateConnId() {
        return idCount.getAndIncrement() & 0x7fffffff;
    }

    /**
     * 根据connId找到对应的Channel
     *
     * @param connId
     * @return
     */
    public NioSocketChannel getChannel(int connId) {
        return channelMap.get(Integer.valueOf(connId));
    }

    /**
     * 更新数据收发时间，表示该连接还存活，如果长时间没有更新，会被回收线程关闭连接通道
     *
     * @param connId
     */
    public void touch(Integer connId) {
        lastTouchMap.put(connId, System.currentTimeMillis());
    }

    public void closeConnection(NioSocketChannel channel) {
        synchronized (updateLock) {
            Integer connId = connIdMap.remove(channel);
            channelMap.remove(connId);
            connectionMap.remove(Integer.valueOf(connId));
            pendingPackage.remove(channel);
            channelConnectMap.remove(channel);

            if (channel != null && !channel.isOpen()) {
                //channel.write(Unpooled.EMPTY_BUFFER);
                try {
                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && running) {
            try {
                Thread.sleep(idleScanInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long time = System.currentTimeMillis();
            synchronized (updateLock) {
                Iterator<Map.Entry<Integer, Long>> it = lastTouchMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Long> entry = it.next();
                    Integer connId = entry.getKey();
                    Long lastTouch = entry.getValue();

                    if (System.currentTimeMillis() - lastTouch > 1.5 * idleScanInterval) {
                        LOG.debug("connection {} shoud being closed ...", connId);
                        it.remove();
                        NioSocketChannel channel = channelMap.remove(connId);
                        if(channel != null) {
                            connIdMap.remove(channel);
                            connectionMap.remove(Integer.valueOf(connId));
                            pendingPackage.remove(channel);
                            channelConnectMap.remove(channel);

                            if (channel != null && !channel.isOpen()) {
                                //channel.write(Unpooled.EMPTY_BUFFER);
                                try {
                                    channel.closeFuture().sync();
                                } catch (InterruptedException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            LOG.debug("scan idle connection finished ... use time [{}].", time);
        }
    }
}
