package com.villcore.net.proxy.v2.client;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 *
 */
public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    //协议发现解析端在client
    //client解析到完整的 addr, host，包装成第一个请求包（connect）发送给 client，此时应该持有一个本地生成的connId
    //server端接收到connect 请求，根据接收到的地址建立链接，如果成功，分配一个唯一connId，并将本地和新生成的connId共同返回，否则新的connId = -1
    //client与server发送与接收在少部分链路，该链路如果因为超时断开，能重建链接，并将发送失败的数据重发
    //链接管理，每次根据connId接收与发送数据成功，会将conn lastTouch时间标记，有一个定时任务定期关闭超时链接（主要发生在双发关闭链接不同步）
    //包管理，包统一使用格式，[total_len](4) + [header_len](4) + [body_len](4) + [header_flag](2)(包区分压缩，加密等) + [header] + [body]

    //客户端拿到需要链接的地址端口信息，打包成pkg,
    public static void main(String[] args) {
        //load configuration
        //TODO load form conf file
        String proxyPort = "10081";

        String remoteAddress = "127.0.0.1";
        String remotePort = "20080";

        //
        ConnectionManager connectionManager = new ConnectionManager();
        new Thread(connectionManager, "connection-manager").start();

        PackageQeueu pkgQueue = new PackageQeueu(1 * 10000);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        NioSocketChannel remoteChannel = null;
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new RemoteChannelInitlizer(remoteChannel, bossGroup, new InetSocketAddress(remoteAddress, Integer.valueOf(remotePort))))
                    .childHandler(new ChildHandlerInitlizer(connectionManager, pkgQueue));
            serverBootstrap.bind(Integer.valueOf(proxyPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
