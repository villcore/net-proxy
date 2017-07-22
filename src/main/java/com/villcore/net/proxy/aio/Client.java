package com.villcore.net.proxy.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by Administrator on 2017/7/18.
 */
public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        AsynchronousServerSocketChannel asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
        asynchronousServerSocketChannel.bind(new InetSocketAddress("127.0.0.1", 10080));

        {
            asynchronousServerSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel result, Object attachment) {
                    asynchronousServerSocketChannel.accept(null, this);
                    LOG.debug("connect complete...");
                    result.read(ByteBuffer.allocate(1 * 1024 * 1024), null, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result2, Object attachment) {
                            result.read(ByteBuffer.allocate(1 * 1024 * 1024), null ,this);
                            if(result2.byteValue() == -1){
                                try {
                                    result.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            LOG.debug("read complete ... {}", result2.intValue());
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            LOG.debug("read failed ... {}", exc);
                        }
                    });

                    result.write(ByteBuffer.wrap("test".getBytes()), null, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            LOG.debug("write complete ... {}", result.intValue());
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            LOG.debug("read failed ... {}", exc);
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    LOG.debug("connect failed...");
                }
            });
        }

        Thread.sleep(1000 * 1000);
    }
}
