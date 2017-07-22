package com.villcore.net.proxy.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/13.
 */
public abstract class DefaultSelectProcessor extends SelectProcessor {
    public DefaultSelectProcessor(int intrestOp, DefaultConnectionManager connectionManager) {
        super(intrestOp, connectionManager);
    }

    @Override
    public void doAccept(SelectionKey key) throws IOException {

    }

    @Override
    public void doConnect(SelectionKey key) throws IOException {

    }

    @Override
    public void doRead(SelectionKey key) throws IOException {

    }

    @Override
    public void doWrite(SelectionKey key) throws IOException {

    }

    @Override
    public abstract Connection getConnection(SelectionKey key);

    @Override
    public abstract SocketChannel getRegisterSocketChannel(Connection connection);
}
