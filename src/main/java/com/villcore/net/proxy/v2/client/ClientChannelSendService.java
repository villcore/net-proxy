package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.v2.pkg.ConnectPackage;
import com.villcore.net.proxy.v2.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 客户端发送服务
 *
 * 主要执行一下操作
 */
public class ClientChannelSendService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ClientChannelSendService.class);

    private volatile boolean running;
    protected PackageQeueu packageQeueu;

    public ClientChannelSendService(PackageQeueu packageQeueu) {
        this.packageQeueu = packageQeueu;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && running) {
            List<Package> packages = packageQeueu.drainPackage();
            if (packages.isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100L);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }

            try {
                packages = processPackages(packages);
                sendPackages(packages);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
    public ClientChannelSendService(PackageQeueu packageQeueu) {
        super(packageQeueu);
    }


    @Override
    protected void sendPackages(List<Package> packages) throws Exception {
        for(Package pkg : packages) {
            //取得localConnId
            //找到相应channel
            //发送
            int localConnId = -1;
            if(pkg instanceof ConnectPackage) {
                ConnectPackage connectPackage = (ConnectPackage) pkg;
                localConnId = connectPackage.getLocalConnectionId();
            }

            if(pkg instanceof DefaultDataPackage) {
                DefaultDataPackage defaultDataPackage = (DefaultDataPackage) pkg;
                localConnId = defaultDataPackage.getLocalConnectionId();
            }

            NioSocketChannel channel = connectionManager.getChannel(localConnId);
            if(channel == null) {
                continue;
            }
            channel.writeAndFlush(pkg.toByteBuf());
        }
    }

    @Override
    protected List<Package> processPackages(List<Package> packages) throws Exception {
        return packages;
    }
}
