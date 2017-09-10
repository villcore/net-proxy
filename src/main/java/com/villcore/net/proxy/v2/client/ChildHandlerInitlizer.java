package com.villcore.net.proxy.v2.client;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import com.villcore.net.proxy.v2.Html404;
import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.GatheringByteChannel;
import java.nio.charset.Charset;

public class ChildHandlerInitlizer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(ChildHandlerInitlizer.class);

    private ConnectionManager connectionManager;
    private PackageQeueu pkgQueue;

    private ChannelInboundHandler packageGatherHandler;

    public ChildHandlerInitlizer(ConnectionManager connectionManager, PackageQeueu pkgQueue) {
        this.connectionManager = connectionManager;
        this.pkgQueue = pkgQueue;
        this.packageGatherHandler = new PackageGatherHandler(this.pkgQueue);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        LOG.debug("init channel [{}]...", socketChannel.remoteAddress().toString());

        socketChannel.pipeline().addLast(ProxyDetectHandler.HANDLER_NAME, new ProxyDetectHandler(connectionManager));
        socketChannel.pipeline().addLast(PackageGatherHandler.HANDLER_NAME, this.packageGatherHandler);
//                new ChannelOutboundHandlerAdapter(){
//                    @Override
//                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//                        ByteBuf byteBuf = null;
//                        if(msg instanceof Package) {
//                            Package pkg = (Package) msg;
//                            byteBuf = pkg.toByteBuf();
//                        }
//
//                        if(msg instanceof ByteBuf) {
//                            byteBuf = (ByteBuf) msg;
//                        }
//                        System.out.println("write1..." + byteBuf.toString(Charset.forName("utf-8")));
//                        ctx.writeAndFlush(msg);
//                    }
//                },

//                new ChannelInboundHandlerAdapter(){
//                    @Override
//                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                        ByteBuf byteBuf = null;
//                        if(msg instanceof Package) {
//                            Package pkg = (Package) msg;
//                            byteBuf = pkg.toByteBuf();
//                        }
//
//                        if(msg instanceof ByteBuf) {
//                            byteBuf = (ByteBuf) msg;
//                        }
//                        System.out.println("read2..." + byteBuf.toString(Charset.forName("utf-8")));
//                        super.channelRead(ctx, msg);
//                        System.out.println("read2...");
//                    }
//                },
//                new ChannelInboundHandlerAdapter(){
//                    @Override
//                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                        ByteBuf byteBuf = null;
//                        if(msg instanceof Package) {
//                            Package pkg = (Package) msg;
//                            byteBuf = pkg.toByteBuf();
//                        }
//
//                        if(msg instanceof ByteBuf) {
//                            byteBuf = (ByteBuf) msg;
//                        }
//                        System.out.println("read3..." + byteBuf.toString(Charset.forName("utf-8")));
//                        super.channelRead(ctx, msg);
//                        ctx.writeAndFlush(Unpooled.wrappedBuffer(Html404.RESP.getBytes()));
//                    }
//                }
//                new ChannelOutboundHandlerAdapter(){
//                    @Override
//                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//                        ByteBuf byteBuf = null;
//                        if(msg instanceof Package) {
//                            Package pkg = (Package) msg;
//                            byteBuf = pkg.toByteBuf();
//                        }
//
//                        if(msg instanceof ByteBuf) {
//                            byteBuf = (ByteBuf) msg;
//                        }
//                        System.out.println("write1..." + byteBuf.toString(Charset.forName("utf-8")));
//                        ctx.writeAndFlush(msg);
//                    }
//                }
    }
}
