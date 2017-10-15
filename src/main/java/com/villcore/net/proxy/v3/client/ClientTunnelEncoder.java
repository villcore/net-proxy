package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.bio.util.HttpParser;
import com.villcore.net.proxy.v3.Html404;
import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.v2.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.v2.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.v2.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

public class ClientTunnelEncoder extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(ClientTunnelEncoder.class);

    //TODO 优化的地方解析请求地址,只需要将http地址解析到即可。

    /**
     * http
     **/
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";

    /**
     * https
     **/
    private static final String CONNECT = "CONNECT";
    private static final String HTTPS_CONNECTED_RESP = "HTTP/1.0 200 Connection Established\r\n\r\n";

    private TunnelManager tunnelManager;
    private Connection connection;
    private boolean detectedProxy = false;

    private long userFlag = 1L;

    public ClientTunnelEncoder(TunnelManager tunnelManager, Connection connection) {
        this.tunnelManager = tunnelManager;
        this.connection = connection;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        LOG.debug("read ...........");
        ChannelPipeline pipeline = ctx.pipeline();
        Channel channel = pipeline.channel();

        Tunnel curTunnel = tunnelManager.tunnelFor(channel);
        curTunnel.touch(-1);
//        channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
//            @Override
//            public void operationComplete(Future<? super Void> future) throws Exception {
//                if (future.isSuccess()) {
//                    if (curTunnel != null) {
//                        curTunnel.needClose();
//                        curTunnel.close();
//                    }
//                }
//            }
//        });

        int connId = curTunnel.getConnId();

        ByteBuf byteBuf = in;
        byteBuf.markReaderIndex();
        byteBuf.markWriterIndex();


//        byte[] bytes2 = new byte[byteBuf.readableBytes()];
//        byteBuf.readBytes(bytes2);
//        LOG.debug("tunnel [{}] -> [{}] need send {} bytes ...", curTunnel.getConnId(), curTunnel.getCorrespondConnId(), bytes2.length);
//        LOG.debug("content = {}", new String(bytes2));

        byteBuf.resetReaderIndex();
        byteBuf.resetWriterIndex();

        if (detectedProxy) {
            if (curTunnel.shouldClose()) {
                curTunnel.getChannel().config().setAutoRead(false);
                return;
            }

            if (!curTunnel.readWaterMarkerSafe()) {
                curTunnel.getChannel().config().setAutoRead(false);
            }

            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            LOG.debug("tunnel [{}] -> [{}] need send {} bytes ...", curTunnel.getConnId(), curTunnel.getCorrespondConnId(), bytes.length);
            DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, curTunnel.getCorrespondConnId(), userFlag, bytes);
            curTunnel.addSendPackage(dataPackage);
            ctx.fireChannelRead(Unpooled.EMPTY_BUFFER);
            return;
        }

        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex();

        int avaliableRead = byteBuf.readableBytes();
        LOG.debug("avaliable read = {}", avaliableRead);

        if (avaliableRead > 4) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);

            String procotol = new String(bytes, "utf-8");

            //LOG.debug("http procotal = {}", procotol);
            if (procotol.startsWith(POST) || procotol.startsWith(GET) || procotol.startsWith(HEAD)) {
                String[] hostAndPort = HttpParser.parseAddress4(procotol.getBytes("utf-8"));

                if (hostAndPort.length != 2) {
                    channel.config().setAutoRead(false);
                    curTunnel.shouldClose();
                    curTunnel.close();
                    return;
                }

                String hostName = hostAndPort[0];
                short port = Short.valueOf(hostAndPort[1]);

                if (hostName == null) {
                    channel.config().setAutoRead(false);
                    curTunnel.shouldClose();
                    curTunnel.close();
                    return;
                }
                //System.out.println(5);

                LOG.debug("need connect to https [{}]", hostName + ":" + port);

                ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
                DefaultDataPackage dataPackage = PackageUtils.buildDataPackage(connId, -1, userFlag, bytes);

                curTunnel.setConnectPackage(connectReqPackage);
                curTunnel.addSendPackage(dataPackage);
                curTunnel.waitTunnelConnect();

                curTunnel.getChannel().writeAndFlush(Unpooled.wrappedBuffer(Html404.RESP.getBytes()));
                detectedProxy = true;
                ctx.fireChannelRead(Unpooled.EMPTY_BUFFER);
                return;
            } else {
                LOG.debug("not a http req ...");
                byteBuf.resetReaderIndex();
                byteBuf.resetWriterIndex();
            }
        }

        if (avaliableRead > CONNECT.length()) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);

            String procotol = new String(bytes, "utf-8");
            //LOG.debug("https procotal = {}", procotol);

            if (procotol.startsWith(CONNECT)) {
                int lastIndex = writerIndex;
                int last = byteBuf.getByte(lastIndex - 1);
                int lastOne = byteBuf.getByte(lastIndex - 2);
                int lastTwo = byteBuf.getByte(
                        lastIndex - 3);
                int lastThree = byteBuf.getByte(lastIndex - 4);

                //System.out.println("1");
                if ((last == lastTwo && lastTwo == 10) && (lastOne == lastThree && lastThree == 13)) {

                    String[] hostAndPort = HttpParser.parseAddress4(procotol.getBytes("utf-8"));

                    if (hostAndPort.length != 2) {
                        channel.config().setAutoRead(false);
                        curTunnel.shouldClose();
                        curTunnel.close();
                        return;
                    }

                    String hostName = hostAndPort[0];
                    short port = Short.valueOf(hostAndPort[1]);

                    if (hostName == null) {
                        channel.config().setAutoRead(false);
                        curTunnel.shouldClose();
                        curTunnel.close();
                        return;
                    }
                    //System.out.println(5);

                    LOG.debug("need connect to https [{}]", hostName + ":" + port);

                    ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage(hostName, port, connId, userFlag);
                    curTunnel.setConnectPackage(connectReqPackage);
                    curTunnel.waitTunnelConnect();
                    curTunnel.setHttps(true);
                    //ctx.writeAndFlush(Unpooled.wrappedBuffer(HTTPS_CONNECTED_RESP.getBytes()));
                    ctx.fireChannelRead(Unpooled.EMPTY_BUFFER);

                    detectedProxy = true;
                    return;
                }
            } else {
                LOG.debug("not a https req ...");
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
        ctx.fireChannelRead(Unpooled.EMPTY_BUFFER);
    }
}
