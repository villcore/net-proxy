package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.client.HostPort;
import com.villcore.net.proxy.crypt.Crypt;
import com.villcore.net.proxy.dns.DNS;
import com.villcore.net.proxy.metric.ClientMetrics;
import com.villcore.net.proxy.packet.Package;
import com.villcore.net.proxy.util.NamedThreadFactory;
import com.villcore.net.proxy.util.SocketUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class RemotePackageForwarder extends SimpleChannelInboundHandler<Package> {

    private static final Logger LOG = LoggerFactory.getLogger(RemotePackageForwarder.class);

    private final String proxyServerAddress;
    private final int proxyServerPort;

    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;

    private EventLoopGroup remoteEventLoopGroup;
    private Bootstrap remoteBootstrap;

    private final ConcurrentMap<Channel, Channel> channelMap = new ConcurrentHashMap<>();

    public RemotePackageForwarder(String proxyServerAddress, int proxyServerPort) {
        this.proxyServerAddress = proxyServerAddress;
        this.proxyServerPort = proxyServerPort;
        this.eventLoopGroup = new NioEventLoopGroup();
        initBoostrap(this.proxyServerAddress, this.proxyServerPort);
    }

    private void initBoostrap(String proxyServerAddress, int proxyServerPort) {
        // TODO just debug
        // LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

        this.eventLoopGroup = new NioEventLoopGroup(4, new NamedThreadFactory("client-local-forward-worker"));
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 1 * 1024 * 1024)
                .option(ChannelOption.SO_SNDBUF, 1 * 1024 * 1024)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000)
                .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(false))
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                        pipeline.addLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                        pipeline.addLast(new RemotePackageDecoder());
                        pipeline.addLast(new PackageDecipher());
                        pipeline.addLast(new SimpleChannelInboundHandler<Package>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
                                Channel channel = ctx.channel();
                                Attribute<Channel> sourceChannelAttr = channel.attr(AttributeKey.valueOf(ChannelAttrKeys.SOURCE_CHANNEL));
                                Channel localChannel = sourceChannelAttr.get();
                                ClientMetrics.incRx (pkg.getBody().length);
                                localChannel.writeAndFlush(Unpooled.wrappedBuffer(pkg.getBody()));
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Write to local channel content \n {}", new String(pkg.getBody(), StandardCharsets.UTF_8));
                                }
                            }
                        });
                        pipeline.addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//                                LOG.error("{}", cause);
                                ctx.close();
                            }
                        });
                    }
                });

        this.remoteEventLoopGroup = new NioEventLoopGroup(4, new NamedThreadFactory("client-remote-forward-worker"));
        this.remoteBootstrap = new Bootstrap();
        this.remoteBootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 1 * 1024 * 1024)
                .option(ChannelOption.SO_SNDBUF, 1 * 1024 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(false))
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                        pipeline.addLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                        pipeline.addLast(new RemotePackageDecoder());
                        pipeline.addLast(new PackageDecipher());
                        pipeline.addLast(new SimpleChannelInboundHandler<Package>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
                                Channel channel = ctx.channel();
                                Attribute<Channel> sourceChannelAttr = channel.attr(AttributeKey.valueOf(ChannelAttrKeys.SOURCE_CHANNEL));
                                Channel localChannel = sourceChannelAttr.get();
                                ClientMetrics.incRx (pkg.getBody().length);
                                localChannel.writeAndFlush(Unpooled.wrappedBuffer(pkg.getBody()));
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Write to local channel content \n {}", new String(pkg.getBody(), StandardCharsets.UTF_8));
                                }
                            }
                        });
                        pipeline.addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//                                LOG.error("{}", cause);
                                ctx.close();
                            }
                        });
                    }
                });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
//        Channel localChannel = ctx.channel();
//        Channel remoteChannel = this.bootstrap.connect(proxyServerAddress, proxyServerPort).sync().channel();
//        Attribute<Channel> sourceChannelAttr = remoteChannel.attr(AttributeKey.<Channel>valueOf(ChannelAttrKeys.SOURCE_CHANNEL));
//        sourceChannelAttr.set(localChannel);
//        Attribute<Crypt> cryptAttribute = localChannel.attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT));
//        Crypt crypt = cryptAttribute.get();
//        remoteChannel.attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT)).set(crypt);
//        channelMap.put(localChannel, remoteChannel);
//        remoteChannel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
//            @Override
//            public void operationComplete(Future<? super Void> future) throws Exception {
//                LOG.info("Close channel {} close complete", localChannel.remoteAddress());
//                channelMap.remove(localChannel);
//                localChannel.close();
//            }
//        });
//        LOG.info("Connect remote channel {} complete", remoteChannel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel localChannel = ctx.channel();
        Channel remoteChannel = channelMap.remove(localChannel);
        if (remoteChannel == null) {
            return;
        }
        remoteChannel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                LOG.info("Close remote channel {} close complete", remoteChannel.remoteAddress());
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
        if (!channelMap.containsKey(ctx.channel())) {
            initRemoteChannel(ctx);
        }
        Channel localChannel = ctx.channel();
        Channel remoteChannel = channelMap.get(localChannel);
        boolean localForward = ctx.channel().attr(AttributeKey.<Boolean>valueOf(ChannelAttrKeys.LOCAL_FORWARD)).get();
        if (remoteChannel != null) {
            byte[] bytes = Package.toBytes(pkg);
            bytes = localForward ? pkg.getBody() : bytes;
            ClientMetrics.incTx(bytes.length);
            remoteChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Forward {} bytes from local channel {} to remote channel {} complete", bytes.length, localChannel.remoteAddress(), remoteChannel.remoteAddress());
            }
        } else {
            LOG.warn("Forward local channel {} to not exist remote channel error", localChannel.remoteAddress());
        }
        ReferenceCountUtil.release(pkg);
    }

    private void initRemoteChannel(ChannelHandlerContext ctx) throws InterruptedException {
        Channel localChannel = ctx.channel();
        boolean localForward = ctx.channel().attr(AttributeKey.<Boolean>valueOf(ChannelAttrKeys.LOCAL_FORWARD)).get();
        HostPort hostPort = ctx.channel().attr(AttributeKey.<HostPort>valueOf(ChannelAttrKeys.HOST_PORT)).get();

        Channel remoteChannel = null;
        boolean channelConnectLocal = true;
        if (localForward) {
            channelConnectLocal = true;
            remoteChannel = this.bootstrap.connect(hostPort.getHost(), hostPort.getPort()).sync().channel();
            ClientMetrics.incrOpenLocalChannelCounter(1);
        } else {
            channelConnectLocal = false;
            remoteChannel = this.remoteBootstrap.connect(proxyServerAddress, proxyServerPort).sync().channel();
            ClientMetrics.incrOpenRemoteChannelCounter(1);
        }
        DNS.connectAddr(hostPort.getHost());

        Attribute<Channel> sourceChannelAttr = remoteChannel.attr(AttributeKey.<Channel>valueOf(ChannelAttrKeys.SOURCE_CHANNEL));
        sourceChannelAttr.set(localChannel);
        Attribute<Crypt> cryptAttribute = localChannel.attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT));
        Crypt crypt = cryptAttribute.get();
        remoteChannel.attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT)).set(crypt);
        remoteChannel.attr(AttributeKey.valueOf(ChannelAttrKeys.LOCAL_FORWARD)).set(localForward);

        channelMap.put(localChannel, remoteChannel);
        boolean finalChannelConnectLocal = channelConnectLocal;
        remoteChannel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                LOG.info("Close channel {} close complete", localChannel.remoteAddress());
                channelMap.remove(localChannel);
                localChannel.close();

                if (finalChannelConnectLocal) {
                    ClientMetrics.incrOpenLocalChannelCounter(-1);
                } else {
                    ClientMetrics.incrOpenRemoteChannelCounter(-1);
                }
                DNS.disConnectAddr(hostPort.getHost());
            }
        });
        LOG.info("Connect remote channel {} complete", remoteChannel.remoteAddress());
    }
}
