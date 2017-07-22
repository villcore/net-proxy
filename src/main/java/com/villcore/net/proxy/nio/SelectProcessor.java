package com.villcore.net.proxy.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by villcore on 2017/7/13.
 */
public abstract class SelectProcessor extends RunnableTask {
    protected Logger LOG;

    {
        LOG = LoggerFactory.getLogger(this.getClass().getSimpleName());
    }

    protected int intrestOp;
    protected Selector selector;
    protected BlockingQueue<Connection> pendingConnection = new LinkedBlockingQueue<>();
    //protected DataQueue dataQueue;
    protected DefaultConnectionManager connectionManager;

    public SelectProcessor(int intrestOp, DefaultConnectionManager connectionManager) {
        this.intrestOp = intrestOp;
        this.connectionManager = connectionManager;
    }

    public void pendingConnection(Connection connection) throws InterruptedException {
        pendingConnection.put(connection);
        selector.wakeup();
    }

    @Override
    public void run() {
        LOG.info("[{}] select processor  starting...", this.getClass().getSimpleName());

        while (running) {
            try {
                selector = Selector.open();
                LOG.info("[{}] init selector processor success...", this.getClass().getSimpleName());
                break;
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }

        while (running) {
            try {
                //pending connection
                List<Connection> newConnections = new LinkedList<>();
                if (pendingConnection.drainTo(newConnections) > 0) {
                    addPendingConnection(newConnections);
                }
                //select
                int selectChannels = selector.select();
                LOG.debug("select channels = {}", selectChannels);

                if (selectChannels <= 0) {
                    LOG.debug("select no channels...");
                    continue;
                }
                //LOG.debug("select intrest channels...");

                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey key = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (!key.isValid()) {
                        Connection connection = getConnectionForKey(key);
                        closeConnection(connection);
                        key.cancel();
                        //LOG.debug("[{}] connection [{}] invalid...", this.getClass().getSimpleName(), connection.getId());
                    }

                    try {
                        if (key.isValid() && running && (intrestOp == SelectionKey.OP_ACCEPT) && key.isAcceptable()) {
                            doAccept(key);
                        }

                        if (key.isValid() && running && (intrestOp == SelectionKey.OP_CONNECT) && key.isConnectable()) {
                            doConnect(key);
                        }

                        if (key.isValid() && running && (intrestOp == SelectionKey.OP_READ) && key.isReadable()) {
                            //process
                            doRead(key);
                            LOG.debug("do read...");
                        }

                        if (key.isValid() && running && (intrestOp == SelectionKey.OP_WRITE) && key.isWritable()) {
                            //process
                            doWrite(key);
                            LOG.debug("do write...");
                        }
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                        Connection connection = getConnectionForKey(key);
                        closeConnection(connection);
                        key.cancel();
                    }
                }
            } catch (IOException e) {
                //stop();
                LOG.error(e.getMessage(), e);
            }
        }

        try {
            stop();
            close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("[{}] select processor stoped...", this.getClass().getSimpleName());
    }

    public abstract void doAccept(SelectionKey key) throws IOException;

    public abstract void doConnect(SelectionKey key) throws IOException;

    public abstract void doRead(SelectionKey key) throws IOException;

    public abstract void doWrite(SelectionKey key) throws IOException;


    private void addPendingConnection(List<Connection> newConnections) {
        for (Connection connection : newConnections) {
            try {
                getRegisterSocketChannel(connection).register(this.selector, intrestOp);
                connection.addSelector(selector);
            } catch (ClosedChannelException e) {
                LOG.error(e.getMessage(), e);
                LOG.info("connection [{}] already closed...", connection.getId());
                closeConnection(connection);
            }
        }
    }

    public void closeConnection(Connection connection) {
        connectionManager.closeConnection(connection);
    }

    public Connection getConnectionForKey(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        LOG.debug("socket channel == {}", socketChannel);
        Connection connection = connectionManager.getConnectionByLocalSocket(socketChannel) == null ? connectionManager.getConnectionByRemoteSocket(socketChannel) : connectionManager.getConnectionByLocalSocket(socketChannel);
        return connection;
    }

    public void close() throws IOException {
        selector.wakeup();
        selector.close();
    }

    public abstract Connection getConnection(SelectionKey key);

    public abstract SocketChannel getRegisterSocketChannel(Connection connection);

    public Connection getConnectionByLocalSocket(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        if (connection == null) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            connection = connectionManager.getConnectionByLocalSocket(socketChannel);
            key.attach(connection);
        }
        return connection;
    }

    public Connection getConnectionByRemoteSocket(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        if (connection == null) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            connection = connectionManager.getConnectionByRemoteSocket(socketChannel);
            key.attach(connection);
        }
        return connection;
    }
}
