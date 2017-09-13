package com.villcore.net.proxy.v2.server;

import com.villcore.net.proxy.v2.client.ConnectionManager;
import com.villcore.net.proxy.v2.client.PackageQeueu;
import com.villcore.net.proxy.v2.pkg.*;
import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务端发送服务
 * <p>
 *
 * 服务端接收到 Package 会做以下处理
 *
 * 1.处理ConnectPackage，根据连接地址新建SocketChannel，如果成功，分配connId，并将接收到的localConnId与远程connId返回，如果失败，返回远程的远程Id为-1
 * 2.处理 DataPackage， 根据接收到的 Package中的connId进行链接的读写
 */
public class ServerChannelSendService implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ServerChannelSendService.class);

    private static final long IDLE_SLEEP_MICROSECONDS = 50;
    private volatile boolean running;

    private ConnectionManager connectionManager;

    private PackageQeueu sendPackage;
    private PackageQeueu recvPackage;
    private PackageQeueu failedSendPackage;

    private EventLoopGroup eventExecutors;

    private final Bootstrap bootstrap = new Bootstrap();

    private NioSocketChannel clientChannel;

    private ExecutorService connectExecutor = Executors.newCachedThreadPool();

    private AtomicLong recvCnt = new AtomicLong();
    private AtomicLong sendCnt = new AtomicLong();

    public void addRecv() {
        //LOG.debug("recv pkg, total = {}", recvCnt.incrementAndGet());
    }

    public void addSend() {
        //LOG.debug("send pkg, total = {}", sendCnt.incrementAndGet());
    }
    public ServerChannelSendService(ConnectionManager connectionManager, PackageQeueu sendPackage, PackageQeueu recvPackage, PackageQeueu failedSendPackage, EventLoopGroup eventExecutors) {
        this.connectionManager = connectionManager;
        this.sendPackage = sendPackage;
        this.recvPackage = recvPackage;
        this.failedSendPackage = failedSendPackage;
        this.eventExecutors = eventExecutors;

        bootstrap.group(this.eventExecutors)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new DstSocketReadHandler(connectionManager, sendPackage));
    }

    //远程读取handler
    @ChannelHandler.Sharable
    private static class DstSocketReadHandler extends ChannelInboundHandlerAdapter {
        private ConnectionManager connectionManager;
        private PackageQeueu sendPackage;

        public DstSocketReadHandler(ConnectionManager connectionManager, PackageQeueu sendPackage) {
            this.connectionManager = connectionManager;
            this.sendPackage = sendPackage;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LOG.debug("get dst resp...");
            NioSocketChannel socketChannel = (NioSocketChannel) ctx.channel();
            int dstConnId = connectionManager.getConnId(socketChannel);
            int localConnId = connectionManager.getConnectionMap(dstConnId);

            connectionManager.touch(dstConnId);
            if(msg instanceof ByteBuf) {
                ByteBuf data = (ByteBuf) msg;
                DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(localConnId, dstConnId, 1L, data);
                try {
                    LOG.debug("\n=======================resp[local{}, remote{}]====================\n{}\n", dataPackage.getLocalConnId(), dataPackage.getRemoteConnId(), /**PackageUtils.toString(dataPackage.getBody())**/"");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendPackage.putPackage(dataPackage);
            }
        }
    };

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && running) {
            List<Package> failPackages = failedSendPackage.drainPackage();
            List<Package> recvPackages = recvPackage.drainPackage();
            List<Package> sendPackages = sendPackage.drainPackage();

            if (sendPackages.isEmpty() && recvPackages.isEmpty() && failPackages.isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(IDLE_SLEEP_MICROSECONDS);
                    continue;
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }

            buildDstChannelOrSend(recvPackages);
            sendClient(failPackages);
            sendClient(sendPackages);
        }
    }

    private NioSocketChannel connectServer(int localConnId, String addr, int port) {
        LOG.debug("ready to connect to server [{}:{}]...", addr, port);
        Channel channel = null;
        try {
            channel = bootstrap.connect(addr, port).sync().channel();
            channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    LOG.debug("remote server channel closed...");
                }
            });
            NioSocketChannel dstSocketChannel = (NioSocketChannel) channel;
            LOG.debug("connect to server [{}] success ...", dstSocketChannel.remoteAddress().toString());
            return dstSocketChannel;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            //channel.closeFuture();
        }
        return null;
    }

    public void setClientChannel(NioSocketChannel socketChannel) {
        clientChannel = socketChannel;
    }

    public void sendClient(List<Package> packages) {
        try {
            packages = processSendPackages(packages);
            writeSendPackages(packages);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void writeSendPackages(List<Package> packages) throws InterruptedException {
        //LOG.debug("send package size = {}", packages.size());
        int count = 0;
        for (Package pkg : packages) {
            //LOG.debug("channel send service write send package ... {}", pkg);
            if(clientChannel == null || !clientChannel.isOpen()) {
                failedSendPackage.putPackage(pkg);
                continue;
            }
            clientChannel.write(pkg.toByteBuf());
            addSend();
        }
        if(clientChannel == null || !clientChannel.isOpen()) {

        } else {
            clientChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
        }
    }

    public void buildDstChannelOrSend(List<Package> packages) {
        try {
            packages = processRecvPackages(packages);
            handleRecvPackages(packages);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void handleRecvPackages(List<Package> packages) throws Exception {
        for (Package pkg : packages) {
            int localConnId = -1;
            if (pkg instanceof ConnectPackage) {
                ConnectPackage connectPackage = (ConnectPackage) pkg;

                LOG.debug("handle recv package, connect pkg totalLen = {}, headerLen = {}, bodyLen = {}", connectPackage.getTotalLen(), connectPackage.getHeaderLen(), connectPackage.getBodyLen());
                //connectPackage.getHeader().retain();
                localConnId = connectPackage.getConnId();
                buildDstChannel(localConnId, connectPackage);
            }

            if (pkg instanceof DefaultDataPackage) {
                DefaultDataPackage defaultDataPackage = (DefaultDataPackage) pkg;
                localConnId = defaultDataPackage.getLocalConnId();

                sendDst(defaultDataPackage);
            }
            addRecv();
        }
    }

    private void sendDst(DefaultDataPackage defaultDataPackage) {
        int dstConnId = defaultDataPackage.getRemoteConnId();
        NioSocketChannel socketChannel = connectionManager.getChannel(dstConnId);
        if(socketChannel != null) {
            try {
                LOG.debug("\n=======================req[local{}, remote{}]====================\n{}\n",defaultDataPackage.getLocalConnId(), defaultDataPackage.getRemoteConnId(), /**PackageUtils.toString(defaultDataPackage.getBody())**/"");
            } catch (Exception e) {
                e.printStackTrace();
            }
            socketChannel.writeAndFlush(defaultDataPackage.getBody());
            connectionManager.touch(dstConnId);
        } else {
            defaultDataPackage.toByteBuf().release();
        }
    }

    private void buildDstChannel(int localConnId, ConnectPackage connectPackage) throws UnsupportedEncodingException, InterruptedException {
        String hostname = connectPackage.getHostname();
        short port = connectPackage.getPort();

        connectExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    NioSocketChannel dstSocketChannel = connectServer(localConnId, hostname, port);
                    ConnectRespPackage respPackage = null;
                    if (dstSocketChannel != null) {
                        int connId = connectionManager.addConnection(dstSocketChannel);
                        connectionManager.makeConnectionMap(connId, localConnId);
                        connectionManager.touch(connId);
                        respPackage = PackageUtils.buildConnectRespPackage(localConnId, connId, 1L);
                    } else {
                        respPackage = PackageUtils.buildConnectRespPackage(localConnId, -1, 1L);
                    }
                    //LOG.debug("build connect resp package, localConnId = {}, remoteId = {}", respPackage.getLocalConnId(), respPackage.getRemoteConnId());
                    sendPackage.putPackage(respPackage);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
    }

    private List<Package> processSendPackages(List<Package> packages) throws Exception {
        return packages;
    }

    private List<Package> processRecvPackages(List<Package> packages) throws Exception {
        return packages;
    }
}
