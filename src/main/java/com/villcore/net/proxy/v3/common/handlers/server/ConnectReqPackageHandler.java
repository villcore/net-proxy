package com.villcore.net.proxy.v3.common.handlers.server;

import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.pkg.v2.*;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import com.villcore.net.proxy.v3.server.DNS;
import com.villcore.net.proxy.v3.server.ServerTunnelChannelReadHandler;
import com.villcore.net.proxy.v3.server.ServerTunnelMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * server side handler
 */
public class ConnectReqPackageHandler implements PackageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectReqPackage.class);

    private EventLoopGroup eventLoopGroup;

    private WriteService writeService;
    private TunnelManager tunnelManager;
    private ConnectionManager connectionManager;

    private static final short MAX_CONNECT_RETRY = 3;

    private Bootstrap bootstrap;

    public ConnectReqPackageHandler(EventLoopGroup eventLoopGroup, WriteService writeService, TunnelManager tunnelManager, ConnectionManager connectionManager) {
        this.eventLoopGroup = eventLoopGroup;
        this.writeService = writeService;
        this.tunnelManager = tunnelManager;
        this.connectionManager = connectionManager;

        bootstrap = initBoostrap();
    }

    private Bootstrap initBoostrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1 * 60 * 60 * 1000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                //.option(ChannelOption.AUTO_READ, false)

//                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
//                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)

                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ServerTunnelChannelReadHandler(tunnelManager));
//                        ch.pipeline().addLast(new ServerTunnelMessageEncoder(tunnelManager));
//                        ch.pipeline().addLast(new TunnelMessageEncoder());
                        ch.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void read(ChannelHandlerContext ctx) throws Exception {
//                                Attribute<Boolean> pause = ctx.channel().attr(AttributeKey.<Boolean>valueOf("pause"));
//                                if(pause.get() == null) {
//                                    pause.set(false);
//                                }
//                                if(!pause.get()){
//                                    super.read(ctx);
//                                }
                                Tunnel curTunnel = tunnelManager.tunnelFor(ctx.channel());
                                if(curTunnel == null) {
                                    return;
                                }

                                if(curTunnel.isPause()) {
                                    LOG.debug("pause ...");
                                    return;
                                }
                                super.read(ctx);
                                LOG.debug("read ...");
                            }

                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                Package pkg = (Package) msg;
                                ctx.writeAndFlush(Unpooled.wrappedBuffer(pkg.getBody()));
                            }
                        });


                    }
                });
        return bootstrap;
    }

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        List<Package> connectReqPackage = packages.stream()
                .filter(pkg -> pkg.getPkgType() == PackageType.PKG_CONNECT_REQ)
                .collect(Collectors.toList());

        List<Package> otherPackage = packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECT_REQ).collect(Collectors.toList());

//        LOG.debug("handle connect req package ...{}, {}", packages.size(), connectReqPackage.size());
        connectReqPackage.stream().map(pkg -> ConnectReqPackage.class.cast(pkg)).collect(Collectors.toList())
                .forEach(pkg -> {
                    Integer correspondConnId = Integer.valueOf(pkg.getConnId());
                    String hostname = new String(pkg.getHostname());
                    int port = pkg.getPort();

                    PackageUtils.release(Optional.of(pkg));

                    LOG.debug("handle connect pkg, req address -> [{}:{}] ...", hostname, port);
                    connectToDst(hostname, port, correspondConnId, connection, 0);
//                    hostname = "127.0.0.1";
//                    port = 3128;
//                    connectToDst(hostname, port, correspondConnId, connection, 0);
                });

        return otherPackage;
    }
    /***
     * 方法2实现, 从效率角度来说, 方法2更好一些
     */
    private void connectToDst(String hostname, int port, int correspondConnId, Connection connection, int retry) {
        String[] addrInfo = DNS.parseIp(hostname, port);
        String ip = addrInfo[0] == null || addrInfo[0].isEmpty() ? hostname : addrInfo[0];
        //ip = hostname;


        Channel[] channels = new Channel[1];
        final int curRetry = retry;

        channels[0] = bootstrap.connect(ip, port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                Channel channel = channels[0];
                if (future.isSuccess()) {
                    //connect success...
                    //channel success, build tunnel
                    if (channel != null && channel.isOpen()) {
                        Tunnel tunnel = tunnelManager.newTunnel(channel);
                        tunnel.setBindConnection(connection);
                        tunnel.setCorrespondConnId(correspondConnId);
                        tunnelManager.bindConnection(connection, tunnel);
                        tunnel.setConnect(true);
                        ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(tunnel.getConnId(), correspondConnId, 1L);
                        //LOG.debug("connect resp [CID{}:CCID{}]", tunnel.getConnId(), correspondConnId);
                        tunnel.addSendPackage(connectRespPackage);
                        writeService.addWrite(tunnel);
                        LOG.debug("connect [{}:{}] success for tunnels [CID{}:CCID{}] ...", hostname, port, tunnel.getConnId(), correspondConnId);

                        channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                            @Override
                            public void operationComplete(Future<? super Void> future) throws Exception {
                                if (future.isSuccess()) {
                                    tunnel.setConnect(false);
                                    //TODO build channel close package and send
                                    List<Package> packages = tunnel.drainSendPackages();
                                    if (!packages.isEmpty()) {
                                        connection.addSendPackages(packages);
                                    }
                                    LOG.debug("server tunnel [{}] close ...", tunnel.getConnId());
                                }
                            }
                        });
                    }
                } else {
                    int newRetry = curRetry + 1;
                    if (newRetry > MAX_CONNECT_RETRY) {
                        if (channel == null || !channel.isOpen()) {
                            //LOG.debug("===== connect [{}:{}] failed  ...", hostname, port);
                            ConnectRespPackage connectRespPackage = PackageUtils.buildConnectRespPackage(-1, correspondConnId, 1L);
                            connection.addSendPackages(Collections.singletonList(connectRespPackage));
                            //LOG.debug("connect resp [CID{}:CCID{}]", -1, correspondConnId);
                            LOG.debug("connect [{}:{}] failed for tunnels [CID{}:CCID{}] ...", hostname, port, -1, correspondConnId);
                            if (channel != null) {
                                channel.close();
                            }
                            return;
                        }
                    } else {
                        eventLoopGroup.schedule(new Runnable() {
                            @Override
                            public void run() {
                                connectToDst(hostname, port, correspondConnId, connection, newRetry);
                            }
                        }, 500, TimeUnit.MILLISECONDS);
                        //retry connect
                    }
                }
            }
        }).channel();
    }
}
