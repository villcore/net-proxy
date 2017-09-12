package com.villcore.net.proxy.v2.client;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import com.villcore.net.proxy.v2.Html404;
import com.villcore.net.proxy.v2.pkg.Package;
import com.villcore.net.proxy.v2.pkg.PackageUtils;
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
        this.packageGatherHandler = new PackageGatherHandler(connectionManager, this.pkgQueue);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        LOG.debug("init channel [{}]...", socketChannel.remoteAddress().toString());
        socketChannel.pipeline().addLast(new ByteBufPackgeHandler(connectionManager));
        socketChannel.pipeline().addLast(this.packageGatherHandler);
        //socketChannel.pipeline().addLast(new TestReader());
    }

    private static class TestReader extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println(PackageUtils.toString(byteBuf));
        }
    };
}
