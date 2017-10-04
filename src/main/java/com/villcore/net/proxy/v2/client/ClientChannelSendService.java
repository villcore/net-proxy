package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.v2.pkg.*;
import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端发送服务
 * <p>
 *
 * 主要执行以下操作
 *
 * 1.对于
 * 2.将聚合的请求package发送到远程服务器通道
 * 3.从远程服务器通道读取相应package，并根据connId分发给本地channnel
 */
public class ClientChannelSendService implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ClientChannelSendService.class);

    private static final long IDLE_SLEEP_MICROSECONDS = 50;
    private volatile boolean running;

    private ConnectionManager connectionManager;

    private PackageQeueu sendPackage;
    private PackageQeueu recvPackage;
    private PackageQeueu failedSendPackage;

    private EventLoopGroup eventExecutors;
    private NioSocketChannel remoteSocketChannel;

    private final Bootstrap bootstrap = new Bootstrap();

    private String remoteAddr;
    private int port;

    private RemoteReadHandler remoteReadHandler;

    private AtomicLong recvCnt = new AtomicLong();
    private AtomicLong sendCnt = new AtomicLong();

    public void addRecv() {
        //LOG.debug("recv pkg, total = {}", recvCnt.incrementAndGet());
    }

    public void addSend() {
        //LOG.debug("write pkg, total = {}", sendCnt.incrementAndGet());
    }

    public ClientChannelSendService(ConnectionManager connectionManager, PackageQeueu sendPackage, PackageQeueu recvPackage, PackageQeueu failedSendPackage, EventLoopGroup eventExecutors, String remoteAddr, int port) {
        this.connectionManager = connectionManager;
        this.sendPackage = sendPackage;
        this.recvPackage = recvPackage;
        this.failedSendPackage = failedSendPackage;
        this.eventExecutors = eventExecutors;
        this.remoteSocketChannel = remoteSocketChannel;
        this.remoteAddr = remoteAddr;
        this.port = port;

        remoteReadHandler = new RemoteReadHandler(connectionManager, sendPackage, recvPackage);
        bootstrap.group(this.eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ClientPackageDecoder());
                        ch.pipeline().addLast(new RemoteReadHandler(connectionManager, sendPackage, recvPackage));
                    }
                });
    }

    //远程读取handler
    @ChannelHandler.Sharable
    private static class RemoteReadHandler extends ChannelInboundHandlerAdapter {
        private ConnectionManager connectionManager;
        private PackageQeueu sendQueue;
        private PackageQeueu recvQueue;

        public RemoteReadHandler(ConnectionManager connectionManager, PackageQeueu sendQueue, PackageQeueu recvQueue) {
            this.connectionManager = connectionManager;
            this.sendQueue = sendQueue;
            this.recvQueue = recvQueue;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof Package) {
                Package pkg = (Package) msg;

                ByteBuf header = pkg.getHeader().copy();
                ByteBuf body = pkg.getBody().copy();
//                LOG.debug(">>>>>>>>>>>>>>>>client recv package = {}", PackageUtils.toString(pkg));

                short pkgType = pkg.getPkgType();
                if (pkgType == PackageType.PKG_CONNECT_RESP) {
                    ConnectRespPackage connectRespPackage = new ConnectRespPackage();
                    connectRespPackage.setHeader(header);
                    connectRespPackage.setBody(body);

                    //ConnectRespPackage connectRespPackage = (ConnectRespPackage) msg;
                    int localConnId = connectRespPackage.getLocalConnId();
                    int remoteConnId = connectRespPackage.getRemoteConnId();
                    NioSocketChannel socketChannel = connectionManager.getChannel(localConnId);

                    if(remoteConnId == -1) {
                        //socketChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
                        connectionManager.closeConnection(socketChannel);
                        return;
                    }

                    connectionManager.channelConnected(socketChannel);
                    connectionManager.makeConnectionMap(localConnId, remoteConnId);
                    for(Package dataPkg : connectionManager.drainPendingPackages(socketChannel)) {
                        DefaultDataPackage defaultDataPackage = (DefaultDataPackage) dataPkg;
                        defaultDataPackage.setRemoteConnId(remoteConnId);
                        //LOG.debug("recv resp package, drain data package to write queue...{}", PackageUtils.toString(dataPkg));
                        sendQueue.putPackage(dataPkg);
                    }
                    connectionManager.touch(localConnId);
                }

                if(pkgType == PackageType.PKG_DEFAULT_DATA) {
                    DefaultDataPackage dataPackage = new DefaultDataPackage();
                    dataPackage.setHeader(header);
                    dataPackage.setBody(body);
//                    DefaultDataPackage dataPackage = (DefaultDataPackage) msg;
                    recvQueue.putPackage(dataPackage);
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    };

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    private void connectServer(String addr, int port) throws InterruptedException {
        LOG.debug("connect to server...");
        try {
            Channel channel = bootstrap.connect(remoteAddr, port).sync().channel();
            channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    LOG.debug("remote server channel closed...");
                }
            });
            remoteSocketChannel = (NioSocketChannel) channel;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
//        Promise<Channel> promise = this.eventExecutors.next().newPromise();
//        promise.addListener(
//                new FutureListener<Channel>() {
//                    @Override
//                    public void operationComplete(final Future<Channel> future) throws Exception {
//                        final Channel outboundChannel = future.getNow();
//                        if (future.isSuccess()) {
//                            remoteSocketChannel = (NioSocketChannel) outboundChannel;
//                            outboundChannel.pipeline().addLast(remoteReadHandler);
//                            LOG.debug("connected remote server [{}] ...", addr);
//                        } else {
//                            LOG.debug("connect remote server [{}] failed...", port);
//                            remoteSocketChannel = null;
//                        }
//                    }
//                });
//        if(remoteSocketChannel == null || !remoteSocketChannel.isOpen()) {
//            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
//                    .option(ChannelOption.SO_KEEPALIVE, true)
//                    .handler(new DirectClientHandler(promise));
//
//            bootstrap.connect(addr, port).addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    if (future.isSuccess()) {
//                    } else {
//                        LOG.debug("connect remote server [{}] failed...", addr);
//                        remoteSocketChannel = null;
//                    }
//                }
//            });
    }

    private void checkRemoteServerConnectState() throws InterruptedException {
        //LOG.debug("======================{}, {}", remoteSocketChannel == null, remoteSocketChannel == null ? true : !remoteSocketChannel.isOpen());
        if (remoteSocketChannel == null || !remoteSocketChannel.isOpen()) {
            if(remoteSocketChannel != null) {
                remoteSocketChannel.close().sync();
                remoteSocketChannel = null;
            }
            LOG.debug("remote server is not connected ...");
            connectServer(remoteAddr, port);
        }
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted() && running) {

            try {
                checkRemoteServerConnectState();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }

            List<Package> failPackages = failedSendPackage.drainPackage();
            List<Package> sendPackages = sendPackage.drainPackage();
            List<Package> recvPackages = recvPackage.drainPackage();

            if (failPackages.isEmpty() && sendPackages.isEmpty() && recvPackages.isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(IDLE_SLEEP_MICROSECONDS);
                    continue;
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }

            //write failed
            sendRemote(failPackages);
            //write remote
            sendRemote(sendPackages);
            //write local
            sendLocal(recvPackages);
        }
    }

    public void sendRemote(List<Package> packages) {
        try {
            packages = processSendPackages(packages);
            writeSendPackages(packages);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void sendLocal(List<Package> packages) {
        try {
            packages = processRecvPackages(packages);
            writeRecvPackages(packages);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void writeSendPackages(List<Package> packages) throws Exception {
        for (Package pkg : packages) {
            int localConnId = -1;

            if (pkg instanceof ConnectReqPackage) {
                ConnectReqPackage connectReqPackage = (ConnectReqPackage) pkg;
                localConnId = connectReqPackage.getConnId();
            }

            if (pkg instanceof DefaultDataPackage) {
                DefaultDataPackage defaultDataPackage = (DefaultDataPackage) pkg;
                localConnId = defaultDataPackage.getLocalConnId();
                int remoteConnId = connectionManager.getConnectionMap(localConnId);
                defaultDataPackage.setRemoteConnId(remoteConnId);
            }

            //TODO 需要处理
            if (remoteSocketChannel == null || !remoteSocketChannel.isOpen()) {
                LOG.debug("remote server channel does not connected...");
                failedSendPackage.putPackage(pkg);
                continue;
            }

            remoteSocketChannel.write(pkg.toByteBuf());
            addSend();
        }
        if (remoteSocketChannel == null || !remoteSocketChannel.isOpen()) {
        } else {
            //remoteSocketChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
        }
    }

    private void writeRecvPackages(List<Package> packages) throws Exception {
        for (Package pkg : packages) {
            //LOG.debug("channel write service write recv package ... {}", pkg);
            int localConnId = -1;
            if (pkg instanceof ConnectReqPackage) {
                ConnectReqPackage connectReqPackage = (ConnectReqPackage) pkg;
                localConnId = connectReqPackage.getConnId();
            }

            if (pkg instanceof DefaultDataPackage) {
                DefaultDataPackage defaultDataPackage = (DefaultDataPackage) pkg;
                localConnId = defaultDataPackage.getLocalConnId();
//                try {
//                    LOG.debug("\n=======================resp[local{}, remote{}]====================\n{}\n", defaultDataPackage.getLocalConnId(), defaultDataPackage.getRemoteConnId(), /**PackageUtils.toString(defaultDataPackage.getBody())**/"");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }

            if(connectionManager.getConnectionMap(localConnId) == -1) {
                localConnId = -1;
            }

            NioSocketChannel socketChannel = connectionManager.getChannel(localConnId);
            if (socketChannel == null || !socketChannel.isOpen()) {
                //failedSendPackage.putPackage(pkg);
                pkg.toByteBuf().release();
                continue;
            }

            socketChannel.writeAndFlush(Unpooled.wrappedBuffer(pkg.getBody()));
            addRecv();
        }
    }

    private List<Package> processSendPackages(List<Package> packages) throws Exception {
        return packages;
    }

    private List<Package> processRecvPackages(List<Package> packages) throws Exception {
        return packages;
    }
}
