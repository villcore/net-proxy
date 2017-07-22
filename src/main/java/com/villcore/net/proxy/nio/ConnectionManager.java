package com.villcore.net.proxy.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/13.
 */
public interface ConnectionManager {
    //
    void closeConnection(int id) throws IOException;
    Connection newConnection(SocketChannel socketChannel) throws IOException;
}
