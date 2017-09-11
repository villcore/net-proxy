package com.villcore.net.proxy.v2.client;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import com.villcore.net.proxy.bio.util.HttpParser;
import com.villcore.net.proxy.v2.pkg.*;
import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 根据请求的内容
 *
 */
public class ProxyDetectHandler extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(ProxyDetectHandler.class);

    public static final String HANDLER_NAME = "proxy-detect";
    private static final String HTTPS_CONNECTED_RESP = "HTTP/1.0 200 Connection Established\\r\\n\\r\\n\";\n";

    private boolean detectedProxy = false;
    private ConnectionManager connectionManager;

    private static AtomicLong count = new AtomicLong();

    public ProxyDetectHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        NioSocketChannel socketChannel = (NioSocketChannel) ctx.channel();
        connectionManager.addConnection(socketChannel);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
//        if(detectedProxy) {
//            LOG.debug("get byteBuf, and convert it to a pkg... total = {}", count.incrementAndGet());
//            //LOG.debug("msg = {}", ((ByteBuf) msg).toString(Charset.forName("utf-8")));
//            NioSocketChannel socketChannel = (NioSocketChannel) channelHandlerContext.pipeline().channel();
//            int connId = connectionManager.getConnId(socketChannel);
//
//            ByteBuf data = byteBuf;
//            Package dataPackage = PackageUtils.buildDataPackage(connId, -1, 1L, data);
//            //ctx.writeAndFlush(dataPackage);
//            //ctx.writeAndFlush(Unpooled.wrappedBuffer(Html404.RESP.getBytes()));
//            list.add(dataPackage);
//            return;
//        }
//        list.add(Unpooled.wrappedBuffer("test".getBytes()));
//        int a = 1;
//        if(1 == a) {
//            return;
//        }
        ChannelPipeline pipeline = channelHandlerContext.pipeline();

        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex();
        LOG.debug("\n=======================\n{}\n==========================", byteBuf.copy().toString(Charset.forName("utf-8")));
//        LOG.debug("readerIndex = {}", readerIndex);
//        LOG.debug("writerIndex = {}", writerIndex);

//        while(byteBuf.readableBytes() > 0) {
//            byte val = byteBuf.readByte();
//            LOG.debug("{} -> {}", new Character((char)val), val);
//        }
        //http
        //https
        //socks5
        final String post = "POST";
        final String get = "GET";

        if(writerIndex > 4) {
            //LOG.debug("http check...");
            ByteBuf httpProtocol = byteBuf.slice(0, 4);
            String procotol = httpProtocol.toString(Charset.forName("utf-8"));
            //LOG.debug("http check...{}", procotol);

            if(procotol.contains(post) || procotol.contains(get)) {
                //LOG.debug("detect protocal = {}", procotol);
                int lastIndex = writerIndex;
                int last = byteBuf.getByte(lastIndex - 1);
                int lastOne = byteBuf.getByte(lastIndex - 2);
                int lastTwo = byteBuf.getByte(lastIndex - 3);
                int lastThree = byteBuf.getByte(lastIndex - 4);

                //LOG.debug("last four char = {}, {}, {}, {}", new String[]{last + "", lastOne + "", lastTwo + "", lastThree + ""});
                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
                    //LOG.debug("read first http req finish...");
                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());
                    //LOG.debug("connect address = {}", address.toString());

                    String hostName = address.getHostName();
                    short port = (short) address.getPort();

                    Integer localConnId = connectionManager.getConnId((NioSocketChannel) channelHandlerContext.channel());
                    Long userFlag = 1L;

                    Package connectPackage = PackageUtils.buildConnectPackage(hostName, port, localConnId, userFlag);
                    //channelHandlerContext.writeAndFlush(connectPackage);

                    LOG.debug("proxy wirte a connect package, total = {}", count.incrementAndGet());


                    //data package
                    Package dataPackage = PackageUtils.buildDataPackage(localConnId, -1, userFlag, byteBuf);
                    //channelHandlerContext.writeAndFlush(dataPackage);

                    list.add(connectPackage);
                    list.add(dataPackage);

                    LOG.debug("connect package type = {}, data package type = {}", connectPackage.getPkgType(), dataPackage.getPkgType());

//                    LOG.debug("connect package = {}", connectPackage.toByteBuf().toString(Charset.forName("utf-8")));
                    LOG.debug("data package = {}", dataPackage.getBody().toString(Charset.forName("utf-8")));

                    LOG.debug("write pkg time = {}", System.currentTimeMillis());
                    LOG.debug("proxy wirte a data package, total = {}", count.incrementAndGet());
                    //pipeline.addAfter(channelHandlerContext.name(), null, new ByteBufToPackageHandler(connectionManager));
                    //pipeline.remove(this);
                    //detectedProxy = true;
                    return;
                } else {

                }
            }
        }

//        final String httpsFirst = "CONNECT";
//        if(writerIndex > httpsFirst.length()) {
//            ByteBuf httpsProcotol = byteBuf.slice(0, httpsFirst.length());
//            String procotol = httpsProcotol.toString(Charset.forName("utf-8"));
//            if(procotol.contains(httpsFirst)) {
//                //LOG.debug("detect protocal = {}", procotol);
//                int lastIndex = writerIndex;
//                int last = byteBuf.getByte(lastIndex - 1);
//                int lastOne = byteBuf.getByte(lastIndex - 2);
//                int lastTwo = byteBuf.getByte(
//                        lastIndex - 3);
//                int lastThree = byteBuf.getByte(lastIndex - 4);
//
//                //LOG.debug("last four char = {}, {}, {}, {}", new String[]{last + "", lastOne + "", lastTwo + "", lastThree + ""});
//                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
//                    //LOG.debug("read first https req finish...");
//                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());
//                    //LOG.debug("connect address = {}", address.toString());
//
//                    channelHandlerContext.channel().writeAndFlush(Unpooled.wrappedBuffer(HTTPS_CONNECTED_RESP.getBytes("utf-8")));
//
//                    String hostName = address.getHostName();
//                    short port = (short) address.getPort();
//                    Integer localConnId = connectionManager.getConnId((NioSocketChannel) channelHandlerContext.channel());
//                    Long userFlag = 1L;
//
//                    Package connectPackage = PackageUtils.buildConnectPackage(hostName, port, localConnId, userFlag);
//                    channelHandlerContext.writeAndFlush(connectPackage.toByteBuf());
//
//                    LOG.debug("proxy wirte a data package, total = {}", count.incrementAndGet());
//
//                    channelHandlerContext.pipeline().addAfter(channelHandlerContext.executor(), HANDLER_NAME, ByteBufToPackageHandler.HANDLER_NAME, new ByteBufToPackageHandler(connectionManager));
//                    channelHandlerContext.pipeline().remove(HANDLER_NAME);
//                }
//            }
//        }
    }
}
