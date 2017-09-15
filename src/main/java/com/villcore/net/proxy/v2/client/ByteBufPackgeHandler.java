package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.bio.util.HttpParser;
import com.villcore.net.proxy.v2.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v2.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v2.pkg.Package;
import com.villcore.net.proxy.v2.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 根据请求的内容
 *
 */
public class ByteBufPackgeHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ByteBufPackgeHandler.class);

    public static final String HANDLER_NAME = "proxy-detect";
    private static final String HTTPS_CONNECTED_RESP = "HTTP/1.0 200 Connection Established\r\n\r\n";

    private boolean detectedProxy = false;
    private ConnectionManager connectionManager;

    private static AtomicLong count = new AtomicLong();

    public ByteBufPackgeHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        NioSocketChannel socketChannel = (NioSocketChannel) ctx.channel();
        connectionManager.addConnection(socketChannel);
    }


    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ChannelPipeline pipeline = ctx.pipeline();
        ByteBuf byteBuf = (ByteBuf) msg;

        if(detectedProxy) {
            //LOG.debug("get byteBuf, and convert it to a pkg... total = {}", count.incrementAndGet());
            //LOG.debug("msg = {}", ((ByteBuf) msg).toString(Charset.forName("utf-8")));
            NioSocketChannel socketChannel = (NioSocketChannel) pipeline.channel();
            int connId = connectionManager.getConnId(socketChannel);
            ByteBuf data = byteBuf;
            Package dataPackage = PackageUtils.buildDataPackage(connId, -1, 1L, data);
            ctx.fireChannelRead(dataPackage);
            return;
        }

        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex();
        //LOG.debug("read index = {}, write index = {}", readerIndex, writerIndex);
//        LOG.debug("channel local addr = {}", pipeline.channel().remoteAddress().toString());
//        LOG.debug("\n=======================\n{}\n==========================", byteBuf.copy().toString(Charset.forName("utf-8")));
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

                    Integer localConnId = connectionManager.addConnection((NioSocketChannel) ctx.channel());
                    Long userFlag = 1L;

                    ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, localConnId, userFlag);

                    //LOG.debug("proxy wirte a connect package, total = {}", count.incrementAndGet());


                    //data package
                    DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(localConnId, -1, userFlag, byteBuf);
                    //LOG.debug("connect package type = {}, data package type = {}", connectReqPackage.getPkgType(), dataPackage.getPkgType());

//                    LOG.debug("connect pkg len = {}, header len = {}, body len = {}", connectReqPackage.getTotalLen(), connectReqPackage.getHeaderLen(), connectReqPackage.getBodyLen());
//                    LOG.debug("data pkg len = {}, header len = {}, body len = {}", dataPackage.getTotalLen(), dataPackage.getHeaderLen(), dataPackage.getBodyLen());

                    ctx.fireChannelRead(connectReqPackage);
                    ctx.fireChannelRead(dataPackage);

//                    LOG.debug("connect package = {}", connectReqPackage.toByteBuf().toString(Charset.forName("utf-8")));
//                    LOG.debug("data package = {}", dataPackage.getBody().toString(Charset.forName("utf-8")));
//
//                    LOG.debug("write pkg time = {}", System.currentTimeMillis());
//                    LOG.debug("proxy wirte a data package, total = {}", count.incrementAndGet());
                    //pipeline.addAfter(channelHandlerContext.name(), null, new ByteBufToPackageHandler(connectionManager));
                    //pipeline.remove(this);
                    detectedProxy = true;
                    return;
                } else {

                }
            }
        }

        final String httpsFirst = "CONNECT";
        if(writerIndex > httpsFirst.length()) {
            ByteBuf httpsProcotol = byteBuf.slice(0, httpsFirst.length());
            String procotol = httpsProcotol.toString(Charset.forName("utf-8"));
            if(procotol.contains(httpsFirst)) {
//                LOG.debug("detect protocal = {}", procotol);
                int lastIndex = writerIndex;
                int last = byteBuf.getByte(lastIndex - 1);
                int lastOne = byteBuf.getByte(lastIndex - 2);
                int lastTwo = byteBuf.getByte(
                        lastIndex - 3);
                int lastThree = byteBuf.getByte(lastIndex - 4);

                //LOG.debug("last four char = {}, {}, {}, {}", new String[]{last + "", lastOne + "", lastTwo + "", lastThree + ""});
                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
                    //LOG.debug("read first https req finish...");
                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());

                    String hostName = address.getHostName();
                    short port = (short) address.getPort();

                    Integer localConnId = connectionManager.addConnection((NioSocketChannel) ctx.channel());
                    Long userFlag = 1L;

                    ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, localConnId, userFlag);
//                    LOG.debug("proxy wirte a connect package, total = {}", count.incrementAndGet());
//                    LOG.debug("connect pkg len = {}, header len = {}, body len = {}", connectReqPackage.getTotalLen(), connectReqPackage.getHeaderLen(), connectReqPackage.getBodyLen());
                    ctx.fireChannelRead(connectReqPackage);
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(HTTPS_CONNECTED_RESP.getBytes()));
                    detectedProxy = true;
                    return;
                }
            }
        }
    }
}
