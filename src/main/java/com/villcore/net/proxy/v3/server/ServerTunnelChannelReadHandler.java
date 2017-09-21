package com.villcore.net.proxy.v3.server;

import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server side tunnel channel read handler
 *
 * 将服务端连接到目的地址的ByteBuf打包成 DefaultDataPackage
 */
public class ServerTunnelChannelReadHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ServerTunnelChannelReadHandler.class);

    public static final String dummyReq = "GET http://speedtest.cn/ HTTP/1.1\n\n" +
            "Accept: text/html, application/xhtml+xml, image/jxr, */*\n\n" +
            "Accept-Language: zh-Hans-CN,zh-Hans;q=0.5\n\n" +
            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063\n\n" +
            "Accept-Encoding: gzip, deflate\n\n" +
            "Host: speedtest.cn\r\n" +
            "Proxy-Connection: Keep-Alive\r\n" +
            "\r\n\r\n";

    private TunnelManager tunnelManager;

    public ServerTunnelChannelReadHandler(TunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOG.debug("|||||server tunnel channel read success ...");
        Channel channel = ctx.channel();
        if(msg instanceof ByteBuf) {
            ByteBuf data = (ByteBuf) msg;
            Tunnel tunnel = tunnelManager.tunnelFor(channel);
            tunnel.touch(null);
            int connId = tunnel.getConnId();
            int corrspondConnId = tunnel.getCorrespondConnId();

            DefaultDataPackage defaultDataPackage = PackageUtils.buildDataPackage(connId, corrspondConnId, 1L, data);
            LOG.debug("add send pkg [{}] -> [{}]", connId, corrspondConnId);
//            ctx.fireChannelRead(defaultDataPackage);
            tunnel.addSendPackage(defaultDataPackage);
//            LOG.debug("read content >>>\n [{}]\n>>>>>>>", PackageUtils.toString(data));
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
