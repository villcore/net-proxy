package com.villcore.net.proxy.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by villcore on 2017/7/13.
 */
public class TestNIOSocket {
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.connect(new InetSocketAddress("127.0.0.1", 20080));

        while(!channel.finishConnect()) {
            Thread.sleep(1000);
        }

        channel.register(selector, SelectionKey.OP_WRITE);
        //channel.register(selector, SelectionKey.OP_READ);


        while(true) {
            if(selector.select(100) > 0) {
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while(selectionKeyIterator.hasNext()) {
                    SelectionKey key = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if(key.isConnectable()) {
                        System.out.println("connect...");
                    }

                    if(key.isWritable()) {
                        System.out.println("write...");
                    }

                    if(key.isReadable()) {
                        System.out.println("read...");
                    }
                }
            }
        }
    }
}
