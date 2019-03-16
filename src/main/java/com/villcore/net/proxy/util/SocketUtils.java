package com.villcore.net.proxy.util;

import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import io.netty.channel.socket.nio.NioSocketChannel;

public class SocketUtils {

    private static Method getJavaSocketChannelMethod;

    static {
        try {
            getJavaSocketChannelMethod = NioSocketChannel.class.getDeclaredMethod("javaChannel");
            getJavaSocketChannelMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Socket getSocketFormChannel(NioSocketChannel nioSocketChannel) {
        try {
            SocketChannel javaSocketChannel = (SocketChannel) getJavaSocketChannelMethod.invoke(nioSocketChannel);
            return javaSocketChannel.socket();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
