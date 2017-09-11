//package com.villcore.net.proxy.v2.client;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.*;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
//import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
//import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
//import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import io.netty.util.concurrent.Future;
//import io.netty.util.concurrent.FutureListener;
//import io.netty.util.concurrent.Promise;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.nio.channels.SocketChannel;
//
//public class RemoteChannelInitlizer extends ChannelInboundHandlerAdapter {
//    private static final Logger LOG = LoggerFactory.getLogger(RemoteChannelInitlizer.class);
//
//    private NioSocketChannel remoteSocketChannel;
//    private final Bootstrap bootstrap = new Bootstrap();
//    private EventLoopGroup eventLoopGroup;
//    private InetSocketAddress remoteAddress;
//
//    public RemoteChannelInitlizer(NioSocketChannel remoteSocketChannel, EventLoopGroup eventLoopGroup, InetSocketAddress remoteAddress) {
//        this.remoteSocketChannel = remoteSocketChannel;
//        this.eventLoopGroup = eventLoopGroup;
//        this.remoteAddress = remoteAddress;
//        bootstrap.group(this.eventLoopGroup)
//                .channel(NioSocketChannel.class);
//    }
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
//        ctx.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
//        Promise<Channel> promise = ctx.executor().newPromise();
//        promise.addListener(
//                new FutureListener<Channel>() {
//                    @Override
//                    public void operationComplete(final Future<Channel> future) throws Exception {
//                        final Channel outboundChannel = future.getNow();
//                        if (future.isSuccess()) {
//                            remoteSocketChannel = (NioSocketChannel) outboundChannel;
//                            LOG.debug("connected remote server [{}] ...", remoteAddress.toString());
//                        } else {
//                            LOG.debug("connect remote server [{}] failed...", remoteAddress.toString());
//                            remoteSocketChannel = null;
//                        }
//                    }
//                });
//        if(remoteSocketChannel == null || !remoteSocketChannel.isOpen()) {
//            bootstrap
//                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
//                    .option(ChannelOption.SO_KEEPALIVE, true)
//                    .handler(new DirectClientHandler(promise));
//
//            bootstrap.connect(remoteAddress.getHostName(), remoteAddress.getPort()).addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    if (future.isSuccess()) {
//                        // Connection established use handler provided results
//                    } else {
//                        // Close the connection if the connection attempt has failed.
//                        ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER);
//                        if (ctx.channel().isActive()) {
//                            ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//                        }
//                        LOG.debug("connect remote server [{}] failed...", remoteAddress.toString());
//                        remoteSocketChannel = null;
//                    }
//                }
//            });
//        }
//    }
//}
