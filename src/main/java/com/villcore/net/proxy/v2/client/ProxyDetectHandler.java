package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.bio.util.HttpParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 根据请求的内容
 *
 */
//@ChannelHandler.Sharable
public class ProxyDetectHandler extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(ProxyDetectHandler.class);

    private ConnectionManager connectionManager;

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
        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex();
        LOG.debug("\n=======================\n{}\n==========================", byteBuf.toString(Charset.forName("utf-8")));
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

                LOG.debug("last four char = {}, {}, {}, {}", new String[]{last + "", lastOne + "", lastTwo + "", lastThree + ""});
                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
                    LOG.debug("read first http req finish...");
                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());
                    LOG.debug("connect address = {}", address.toString());
                    channelHandlerContext.channel().pipeline().remove(this);
                    channelHandlerContext.close();
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
                //LOG.debug("detect protocal = {}", procotol);
                int lastIndex = writerIndex;
                int last = byteBuf.getByte(lastIndex - 1);
                int lastOne = byteBuf.getByte(lastIndex - 2);
                int lastTwo = byteBuf.getByte(
                        lastIndex - 3);
                int lastThree = byteBuf.getByte(lastIndex - 4);

                //LOG.debug("last four char = {}, {}, {}, {}", new String[]{last + "", lastOne + "", lastTwo + "", lastThree + ""});
                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
                    LOG.debug("read first https req finish...");
                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());
                    LOG.debug("connect address = {}", address.toString());
                    String connectResponse = "HTTP/1.0 200 Connection Established\r\n\r\n";
                    channelHandlerContext.channel().writeAndFlush(connectResponse.getBytes("utf-8"));
                    channelHandlerContext.channel().pipeline().remove(this);
                    channelHandlerContext.close();
                }
            }
        }
    }
}
