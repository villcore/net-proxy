package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.common.PackageProcessService;
import com.villcore.net.proxy.v3.common.WriteService;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端ChaildChannelHandlerInitlizer
 *
 * 该类主要在建立客户端代理channel后，将channel保存为对应Tunnel对象，加入管理
 */
public class ClientChildChannelHandlerInitlizer extends ChannelInitializer<Channel> {
    private static final Logger LOG = LoggerFactory.getLogger(ClientChildChannelHandlerInitlizer.class);

    private TunnelManager tunnelManager;

    public ClientChildChannelHandlerInitlizer(EventLoopGroup eventLoopGroup, TunnelManager tunnelManager, PackageProcessService packageProcessService, WriteService writeService) {
        this.tunnelManager = tunnelManager;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        LOG.debug("init channel [{}]...", channel.remoteAddress().toString());

        //TunnelManager#buildeTunnel()
        tunnelManager.newTunnel(channel);

        //channel add handler
        //TunnelReadHandler 解析proxy，打包Package，这里需要对不再读取的三种情形做判断
        channel.pipeline().addLast(new TunnelReadHandler(tunnelManager));
        channel.pipeline().addLast(new PackageToByteBufOutHandler());
    }

    private static class TestReader extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println(PackageUtils.toString(byteBuf));
        }
    };
}
