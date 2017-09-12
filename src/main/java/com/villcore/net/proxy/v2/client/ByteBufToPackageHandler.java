//package com.villcore.net.proxy.v2.client;
//
//import com.villcore.net.proxy.v2.Html404;
//import com.villcore.net.proxy.v2.pkg.Package;
//import com.villcore.net.proxy.v2.pkg.PackageUtils;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.nio.charset.Charset;
//import java.util.concurrent.atomic.AtomicLong;
//
//public class ByteBufToPackageHandler extends ChannelInboundHandlerAdapter{
//    private static final Logger LOG = LoggerFactory.getLogger(ByteBufToPackageHandler.class);
//
//    public static final String HANDLER_NAME = "bytebuf-pkg";
//
//    private ConnectionManager connectionManager;
//    public static AtomicLong count = new AtomicLong();
//
//    public ByteBufToPackageHandler(ConnectionManager connectionManager) {
//        this.connectionManager = connectionManager;
//    }
//
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if(msg instanceof ByteBuf) {
//            LOG.debug("get byteBuf, and convert it to a pkg... total = {}", count.incrementAndGet());
//            ByteBuf byteBuf = (ByteBuf) msg;
//            //LOG.debug("msg = {}", ((ByteBuf) msg).toString(Charset.forName("utf-8")));
//            NioSocketChannel socketChannel = (NioSocketChannel) ctx.pipeline().channel();
//            int connId = connectionManager.getConnId(socketChannel);
//
//            ByteBuf data = (ByteBuf) msg;
//            Package dataPackage = PackageUtils.buildDataPackage(connId, -1, 1L, data);
//            //ctx.writeAndFlush(dataPackage);
//            //ctx.writeAndFlush(Unpooled.wrappedBuffer(Html404.RESP.getBytes()));
//            ctx.fireChannelRead(dataPackage);
//            return;
//        } else {
//            ctx.fireChannelRead(msg);
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        LOG.error(cause.getMessage(), cause);
//        ctx.close();
//    }
//}
