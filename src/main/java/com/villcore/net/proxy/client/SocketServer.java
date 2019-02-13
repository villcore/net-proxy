package com.villcore.net.proxy.client;

import com.villcore.net.proxy.client.handler.PackageEncipher;
import com.villcore.net.proxy.client.handler.LocalPackageDecoder;
import com.villcore.net.proxy.client.handler.RemotePackageForwarder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public class SocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(SocketServer.class);

    private final int listenPort;
    private final String remoteAddress;
    private final int remotePort;
    private final Crypt crypt;

    private EventLoopGroup bossEventGroup;
    private EventLoopGroup workerEventGroup;

    private ExecutorService handlerExecutor;

    public SocketServer(int listenPort, String remoteAddress, int remotePort, Crypt crypt) {
        this.listenPort = listenPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.crypt = crypt;
    }

    public void startup() {
        LOG.info("Starting SocketServer");
        try {
            bossEventGroup = new NioEventLoopGroup(1);
            workerEventGroup = new NioEventLoopGroup(2);             // defulat processor * 2
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEventGroup, workerEventGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false))
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 1 * 1024 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 1 * 1024 * 1024)
                    .childOption(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false))
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // TODO add channel handler
                            RemotePackageForwarder packageForwarder = new RemotePackageForwarder(remoteAddress, remotePort, crypt);
                            ChannelPipeline channelPipeline = ch.pipeline();

                            //channelPipeline.addLast(packageForwarder);

                            channelPipeline.addLast("package-encoder", new LocalPackageDecoder());
                            channelPipeline.addLast("package-encipher", new PackageEncipher(crypt));
                            channelPipeline.addLast(packageForwarder);
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            super.channelActive(ctx);
                            // TODO metric.
                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            super.channelInactive(ctx);
                            // TODO metric.
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(listenPort).sync().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    LOG.info("Start SocketServer completed");
                }
            });
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("Start SocketServer error", e);
        }
    }

    public void shutdown() {

    }
}
