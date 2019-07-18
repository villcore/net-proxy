package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.crypt.Crypt;
import com.villcore.net.proxy.packet.Package;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger LOG = LoggerFactory.getLogger(ClientChannelInitializer.class);

    private final String remoteAddress;
    private final int remotePort;
    private final String password;

    private final RemotePackageForwarder remotePackageForwarder;
    private final Crypt crypt;

    public ClientChannelInitializer(String remoteAddress, int remotePort, String password) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.password = password;

        this.remotePackageForwarder = new RemotePackageForwarder(remoteAddress, remotePort);
        this.crypt = createCrypt(this.password);
    }

    private Crypt createCrypt(String password) {
        Crypt crypt = new Crypt();
        byte[] key = crypt.generateKey(password);
        byte[] iv = crypt.generateIv();
        crypt.setIv(iv);
        crypt.setKey(key);
        crypt.initEncrypt();
        crypt.initDecrypt();
        return crypt;
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        LOG.info("Init local conn {}", ch.remoteAddress());
        ch.attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT)).set(createCrypt(password));
        ChannelPipeline channelPipeline = ch.pipeline();
        channelPipeline.addLast(new LocalHttpDecoder(4096));
//        channelPipeline.addLast(new LocalPackageDecoder());
        channelPipeline.addLast(new PackageEncipher());
        channelPipeline.addLast(remotePackageForwarder);
        channelPipeline.addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                LOG.error("{}", cause);
                ctx.close();
            }
        });
    }
}
