package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.bio.util.HttpParser;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.v1.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.v1.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.v1.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Optional;

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
        curTunnel.touch(-1);
        channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess()) {
                    if(curTunnel != null) {
                        curTunnel.needClose();
                        curTunnel.close();
                    }
                }
            }
        });

        int connId = curTunnel.getConnId();

        if(!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf byteBuf = (ByteBuf) msg;

        LOG.debug("tunnel [{}] -> [{}] need send {} bytes ...", curTunnel.getConnId(), curTunnel.getCorrespondConnId(), byteBuf.readableBytes());

        if(detectedProxy) {
            if(curTunnel.shouldClose()) {
                curTunnel.getChannel().config().setAutoRead(false);
                return;
            }

            if(!curTunnel.readWaterMarkerSafe()) {
                curTunnel.getChannel().config().setAutoRead(false);
            }

            DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, curTunnel.getCorrespondConnId(), userFlag, byteBuf);
            curTunnel.addSendPackage(dataPackage);
            return;
        }

        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex();

        if(writerIndex > 4) {
            ByteBuf httpProtocol = byteBuf.slice(0, 4);
            String procotol = httpProtocol.toString(Charset.forName("utf-8"));
//            LOG.debug("tunnel [{}] HTTP first lien = {}", connId, procotol);
            if(procotol.contains(POST) || procotol.contains(GET) || procotol.contains(HEAD)) {

                InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());

                if(address == null) {
                    channel.config().setAutoRead(false);
                    curTunnel.shouldClose();
                    curTunnel.close();
                    PackageUtils.release(byteBuf);
                    return;
                }
                String hostName = address.getHostName();
                short port = (short) address.getPort();

                if(hostName == null) {
                    channel.config().setAutoRead(false);
                    curTunnel.shouldClose();
                    curTunnel.close();
                    PackageUtils.release(byteBuf);
                    return;
                }
                ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
                DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, -1, userFlag, byteBuf);

                curTunnel.setConnectPackage(connectReqPackage);
                curTunnel.addSendPackage(dataPackage);
                curTunnel.waitTunnelConnect();

                detectedProxy = true;
                return;
            }
        }

        if(writerIndex > CONNECT.length()) {
            ByteBuf httpsProcotol = byteBuf.slice(0, CONNECT.length());
            String procotol = httpsProcotol.toString(Charset.forName("utf-8"));
            if(procotol.contains(CONNECT)) {
                channel.closeFuture();

                int lastIndex = writerIndex;
                int last = byteBuf.getByte(lastIndex - 1);
                int lastOne = byteBuf.getByte(lastIndex - 2);
                int lastTwo = byteBuf.getByte(
                        lastIndex - 3);
                int lastThree = byteBuf.getByte(lastIndex - 4);

                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());

                    if(address == null) {
                        channel.config().setAutoRead(false);
                        curTunnel.shouldClose();
                        curTunnel.close();
                        PackageUtils.release(byteBuf);
                        return;
                    }
                    String hostName = address.getHostName();
                    short port = (short) address.getPort();

                    if(hostName == null) {
                        channel.config().setAutoRead(false);
                        curTunnel.shouldClose();
                        curTunnel.close();
                        PackageUtils.release(byteBuf);
                        return;
                    }

                    ReferenceCountUtil.release(byteBuf);
                    ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
                    curTunnel.setConnectPackage(connectReqPackage);
                    curTunnel.waitTunnelConnect();
                    curTunnel.setHttps(true);
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(HTTPS_CONNECTED_RESP.getBytes()));

                    detectedProxy = true;
                    return;
                }
            }
        }

        curTunnel.stopRead();
        curTunnel.shouldClose();
        curTunnel.drainSendPackages().forEach(pkg -> PackageUtils.release(Optional.of(pkg)));
        curTunnel.drainRecvPackages().forEach(pkg -> PackageUtils.release(Optional.of(pkg)));

        curTunnel.close();
        channel.close();
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! tunnel [{}] protocal detect error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n{}\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", connId, "");
    }
}
