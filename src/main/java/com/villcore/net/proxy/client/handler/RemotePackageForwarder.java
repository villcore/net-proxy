package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.client.Crypt;
import com.villcore.net.proxy.client.Package;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ChannelHandler.Sharable
public class RemotePackageForwarder extends SimpleChannelInboundHandler<Package> {

    private static final Logger LOG = LoggerFactory.getLogger(RemotePackageForwarder.class);

    private static final String SOURCE_CHANNEL_KEY = "source";

    private final String proxyServerAddress;
    private final int proxyServerPort;
    private final Crypt crypt;

    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;

    private final ConcurrentMap<Channel, Channel> channelMap = new ConcurrentHashMap<>();
    private final LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

    public RemotePackageForwarder(String proxyServerAddress, int proxyServerPort, Crypt crypt) {
        this.proxyServerAddress = proxyServerAddress;
        this.proxyServerPort = proxyServerPort;
        this.crypt = crypt;

        this.eventLoopGroup = new NioEventLoopGroup();
        initBoostrap(this.proxyServerAddress, this.proxyServerPort);
    }

    private void initBoostrap(String proxyServerAddress, int proxyServerPort) {
        this.eventLoopGroup = new NioEventLoopGroup(1);
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 1 * 1024 * 1024)
                .option(ChannelOption.SO_SNDBUF, 1 * 1024 * 1024)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(loggingHandler);
                        pipeline.addLast(new RemotePackageDecoder());
                        pipeline.addLast(new PackageDecipher(crypt));
                        pipeline.addLast(new SimpleChannelInboundHandler<Package>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
                                Channel channel = ctx.channel();
                                Attribute<Channel> sourceChannelAttr = channel.attr(AttributeKey.<Channel>valueOf(SOURCE_CHANNEL_KEY));
                                Channel localChannel = sourceChannelAttr.get();
                                localChannel.writeAndFlush(pkg.getBody());
                            }
                        });
                    }
                });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Channel localChannel = ctx.channel();
        Channel remoteChannel = this.bootstrap.connect(proxyServerAddress, proxyServerPort).sync().channel();
        Attribute<Channel> sourceChannelAttr = remoteChannel.attr(AttributeKey.<Channel>valueOf(SOURCE_CHANNEL_KEY));
        sourceChannelAttr.set(localChannel);
        channelMap.put(localChannel, remoteChannel);
        LOG.info("Connect remote channel {} complete", remoteChannel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel localChannel = ctx.channel();
        Channel remoteChannel = channelMap.remove(localChannel);
        remoteChannel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                LOG.info("Close remote channel {} close complete", remoteChannel.remoteAddress());
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
        LOG.info("forward");
        Channel localChannel = ctx.channel();
        Channel remoteChannel = channelMap.get(localChannel);
        if (remoteChannel != null) {
            byte[] bytes = Package.toBytes(pkg);
            LOG.info("forward bytes size {}", bytes.length);
            remoteChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
            LOG.info("Forward pkg {}", new String(pkg.getBody()));
            LOG.info("Forward {} bytes from local channel {} to remote channel {} complete", bytes.length, localChannel.remoteAddress(), remoteChannel.remoteAddress());
        } else {
            LOG.warn("Forward local channel {} to remote channel error", localChannel.remoteAddress());
        }
        ReferenceCountUtil.release(pkg);
        ctx.fireChannelReadComplete();
    }
}
