package com.villcore.net.proxy.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/15.
 */
public class RemoteSocketUtils {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteSocketUtils.class);

    public static SocketChannel connect(InetSocketAddress address, int maxRetry, long retryInterval) throws IOException {
        int curRetry = 0;

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        //TODO option
        socketChannel.connect(address);

        //LOG.info("start connecting to [{}:{}]...", address.getAddress().toString(), address.getPort());

        try {
            while (!socketChannel.finishConnect() && curRetry++ < maxRetry) {
                try {
                    //LOG.info("connecting to [{}:{}]...", address.getAddress().toString(), address.getPort());
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            //LOG.info("connect [{}:{}] failed...", address.getAddress().toString(), address.getPort());
            socketChannel.close();
            return null;
        }

        if (socketChannel.finishConnect()) {
            LOG.info("connect [{}:{}] success...", address.getAddress().toString(), address.getPort());
            return socketChannel;
        } else {
            LOG.info("connect [{}:{}] failed...", address.getAddress().toString(), address.getPort());
            socketChannel.close();
            return null;
        }
    }
}
