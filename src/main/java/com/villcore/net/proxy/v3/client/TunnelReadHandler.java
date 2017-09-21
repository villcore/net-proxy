package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.bio.util.HttpParser;

import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Channel 读取处理Handler
 */
public class TunnelReadHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(TunnelReadHandler.class);

    /** http **/
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";

    /** https **/
    private static final String CONNECT = "CONNECT";
    private static final String HTTPS_CONNECTED_RESP = "HTTP/1.0 200 Connection Established\r\n\r\n";

    private TunnelManager tunnelManager;
    private boolean detectedProxy = false;

    private long userFlag = 1L;

    public TunnelReadHandler(TunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    //TODO 此处需要考虑和重新设计
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        Channel channel = pipeline.channel();
        Tunnel curTunnel = tunnelManager.tunnelFor(channel);
        int connId = curTunnel.getConnId();

        if(!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf byteBuf = (ByteBuf) msg;
        LOG.debug("read ================================{}", byteBuf.readableBytes());

        if(detectedProxy) {
            LOG.debug("tunnel read ready ...{}", tunnelReady(curTunnel));
            LOG.debug("connId = {}, correspondConnId = {}", curTunnel.getConnId(), curTunnel.getCorrespondConnId());
            if (!tunnelReady(curTunnel)) {
                DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, curTunnel.getCorrespondConnId(), userFlag, byteBuf);
                curTunnel.addSendPackage(dataPackage);
                return;
            } else {
                ByteBuf data = byteBuf;
                DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, curTunnel.getCorrespondConnId(), userFlag, data);
                //ctx.fireChannelRead(dataPackage);
                curTunnel.addSendPackage(dataPackage);
                return;
            }
        }

        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex();

        if(writerIndex > 4) {
            ByteBuf httpProtocol = byteBuf.slice(0, 4);
            String procotol = httpProtocol.toString(Charset.forName("utf-8"));

            if(procotol.contains(POST) || procotol.contains(GET) || procotol.contains(HEAD)) {
                int last = byteBuf.getByte(writerIndex - 1);
                int lastOne = byteBuf.getByte(writerIndex - 2);
                int lastTwo = byteBuf.getByte(writerIndex - 3);
                int lastThree = byteBuf.getByte(writerIndex - 4);

                if((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {
                    InetSocketAddress address = HttpParser.parseAddress2(byteBuf.toString(Charset.forName("utf-8")).getBytes());
                    String hostName = address.getHostName();
                    short port = (short) address.getPort();

                    ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
                    DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, -1, userFlag, byteBuf);

//                    ctx.fireChannelRead(connectReqPackage);
//                    ctx.fireChannelRead(dataPackage);
                    curTunnel.setConnectPackage(connectReqPackage);
                    curTunnel.addSendPackage(dataPackage);
                    channel.config().setAutoRead(false);
                    if(connectReqPackage == null) {
                        LOG.debug("!!!!connect pkg == null {}", "");
                    }
                    detectedProxy = true;
                    return;
                } else {

                }
            }
        }

        if(writerIndex > CONNECT.length()) {
            ByteBuf httpsProcotol = byteBuf.slice(0, CONNECT.length());
            String procotol = httpsProcotol.toString(Charset.forName("utf-8"));
            if(procotol.contains(CONNECT)) {

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

                    //ctx.fireChannelRead(connectReqPackage);
                    curTunnel.setConnectPackage(connectReqPackage);
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(HTTPS_CONNECTED_RESP.getBytes()));
                    channel.config().setAutoRead(false);
                    if(connectReqPackage == null) {
                        LOG.debug("!!!!connect pkg == null {}", "");
                    }
                    detectedProxy = true;
                    return;
                }
            }
        }
    }

    /**
     *
     * 当前 Tunnel是否可以继续读取数据
     *
     * @param curTunnel
     * @return
     */
    private boolean tunnelReady(Tunnel curTunnel) {

        //1.tunnel should close
        //2.tunnel write queue full
        //3.connect false

        boolean shouldClose = curTunnel.shouldClose();
        boolean sendQueueFull = curTunnel.sendQueueIsFull();
        boolean connected = curTunnel.getConnected();

        return !shouldClose && !sendQueueFull && connected;
        //return true;
    }
}
