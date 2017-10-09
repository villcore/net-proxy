package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.bio.util.HttpParser;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.ChannelClosePackage;
import com.villcore.net.proxy.v3.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Channel 读取处理Handler
 */
public class ClientTunnelChannelReadHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ClientTunnelChannelReadHandler.class);

    /** http **/
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";

    /** https **/
    private static final String CONNECT = "CONNECT";
    private static final String HTTPS_CONNECTED_RESP = "HTTP/1.0 200 Connection Established\r\n\r\n";

    private TunnelManager tunnelManager;
    private Connection connection;
    private boolean detectedProxy = false;

    private long userFlag = 1L;

    public ClientTunnelChannelReadHandler(TunnelManager tunnelManager, Connection connection) {
        this.tunnelManager = tunnelManager;
        this.connection = connection;
    }

    //TODO 此处需要考虑和重新设计
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        Channel channel = pipeline.channel();
        Tunnel curTunnel = tunnelManager.tunnelFor(channel);
        channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess()) {
//                    ChannelClosePackage channelClosePackage =  , curTunnel.getCorrespondConnId(), 1L);
                }
            }
        });
        int connId = curTunnel.getConnId();

        if(!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf byteBuf = (ByteBuf) msg;
        //LOG.debug("client [{}] read {} bytes ...", curTunnel.getConnId(), byteBuf.readableBytes());
//        LOG.debug("tunnel [{}] read content ===================\n {}=======================", connId, PackageUtils.toString(byteBuf.copy()));

        LOG.debug("tunnel [{}] -> [{}] need send {} bytes ...", curTunnel.getConnId(), curTunnel.getCorrespondConnId(), byteBuf.readableBytes());

        if(detectedProxy) {
            if(curTunnel.shouldClose()) {
//                LOG.debug("tunnel [{}] should close ...", connId);
                curTunnel.getChannel().config().setAutoRead(false);
                return;
            }

            if(!curTunnel.readWaterMarkerSafe()) {
//                LOG.debug("tunnel [{}] waterMark too high ...", connId);
                curTunnel.getChannel().config().setAutoRead(false);
            }

            DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, curTunnel.getCorrespondConnId(), userFlag, byteBuf);
            curTunnel.addSendPackage(dataPackage);
//            LOG.debug("connId = {}, correspondConnId = {}", curTunnel.getConnId(), curTunnel.getCorrespondConnId());
            return;
        }

        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex();

        if(writerIndex > 4) {
            ByteBuf httpProtocol = byteBuf.slice(0, 4);
            String procotol = httpProtocol.toString(Charset.forName("utf-8"));
//            LOG.debug("tunnel [{}] HTTP first lien = {}", connId, procotol);
            if(procotol.contains(POST) || procotol.contains(GET) || procotol.contains(HEAD)) {
//                LOG.debug("tunnel [{}] detect procotol = http", connId);

                InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());
                String hostName = address.getHostName();
                short port = (short) address.getPort();

                ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
                DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, -1, userFlag, byteBuf);

                curTunnel.setConnectPackage(connectReqPackage);
                curTunnel.addSendPackage(dataPackage);
                //channel.config().setAutoRead(false);
                curTunnel.waitTunnelConnect();

                detectedProxy = true;
                return;

//                int last = byteBuf.getByte(writerIndex - 1);
//                int lastOne = byteBuf.getByte(writerIndex - 2);
//                int lastTwo = byteBuf.getByte(writerIndex - 3);
//                int lastThree = byteBuf.getByte(writerIndex - 4);
//
//                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
//                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());
//                    String hostName = address.getHostName();
//                    short port = (short) address.getPort();
//
//                    ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
//                    DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, -1, userFlag, byteBuf);
//
//                    curTunnel.setConnectPackage(connectReqPackage);
//                    curTunnel.addSendPackage(dataPackage);
//                    //channel.config().setAutoRead(false);
//                    curTunnel.waitTunnelConnect();
//
//                    detectedProxy = true;
//                    return;
//                } else {
//                    LOG.debug("");
//                }
            }
        }

        if(writerIndex > CONNECT.length()) {
            ByteBuf httpsProcotol = byteBuf.slice(0, CONNECT.length());
            String procotol = httpsProcotol.toString(Charset.forName("utf-8"));
            if(procotol.contains(CONNECT)) {
//                LOG.debug("tunnel [{}] detect procotol = https", connId);
                channel.closeFuture();
//                int a = 0;
//                if(a == 0) {
//                    return;
//                }

                int lastIndex = writerIndex;
                int last = byteBuf.getByte(lastIndex - 1);
                int lastOne = byteBuf.getByte(lastIndex - 2);
                int lastTwo = byteBuf.getByte(
                        lastIndex - 3);
                int lastThree = byteBuf.getByte(lastIndex - 4);

                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());

                    String hostName = address.getHostName();
                    short port = (short) address.getPort();

                    ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
                    curTunnel.setConnectPackage(connectReqPackage);
                    curTunnel.waitTunnelConnect();
                    curTunnel.setHttps(true);
                    String connectResponse = "HTTP/1.0 200 Connection Established\r\n\r\n";
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(connectResponse.getBytes()));
                    //ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);


//                    ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
//                    DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, -1, userFlag, byteBuf);
//                    curTunnel.setConnectPackage(connectReqPackage);
//                    curTunnel.addSendPackage(dataPackage);
//                    curTunnel.waitTunnelConnect();

//                    if(connectReqPackage == null) {
//                        LOG.debug("!!!!connect pkg == null {}", "");
//                    }
                    detectedProxy = true;
                    return;
                }
            }
        }

        curTunnel.stopRead();
        curTunnel.shouldClose();
        curTunnel.drainSendPackages().forEach(pkg -> pkg.toByteBuf().release());
        curTunnel.drainRecvPackages().forEach(pkg -> pkg.toByteBuf().release());

        curTunnel.close();
        channel.close();
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! tunnel [{}] protocal detect error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n{}\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", connId, PackageUtils.toString(byteBuf.copy()));
//        LOG.debug("tunnel [{}] read content = {}", connId, PackageUtils.toString(byteBuf.copy()));

    }
}
