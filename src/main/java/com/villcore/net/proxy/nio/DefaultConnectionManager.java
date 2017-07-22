package com.villcore.net.proxy.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by villcore on 2017/7/15.
 */
public class DefaultConnectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectionManager.class);

    private final Map<Integer, Connection> connectionMap = new ConcurrentHashMap<>();
    private final Map<SocketChannel, Connection> localConnectionMap = new ConcurrentHashMap<>();
    private final Map<SocketChannel, Connection> remoteConnectionMap = new ConcurrentHashMap<>();

    private AtomicInteger connectionCount = new AtomicInteger(0);

    private DataQueue dataQueue;

    public DefaultConnectionManager(DataQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    //newConnection
    public Connection newConnection(SocketChannel localSocket, SocketChannel remoteSocket, long userFlag) {
        int connectionId = connectionCount.getAndIncrement();
        Connection connection = new Connection(localSocket, remoteSocket, userFlag);
        connection.setId(connectionId);
        return connection;
    }

    public Connection newConnection(SocketChannel localSocket, SocketChannel remoteSocket) {
        int connectionId = connectionCount.getAndIncrement();
        Connection connection = new Connection(localSocket, remoteSocket);
        connection.setId(connectionId);
        return connection;
    }

    //closeConnection
    public void closeConnection(Connection connection) {
        if(connection == null) {
            LOG.warn("connection is null...");
            return;
        }

        if(connectionMap.get(Integer.valueOf(connection.getId())) != null) {
            SocketChannel localSocketChannel = connection.getLocalSocket();
            SocketChannel remoteSocketChannel =  connection.getRemoteSocket();

            connectionMap.remove(Integer.valueOf(connection.getId()));

            if(localSocketChannel != null && localConnectionMap.containsKey(localSocketChannel)) {
                localConnectionMap.remove(localSocketChannel);
                closeSocketChannel(localSocketChannel);
            }

            if(remoteSocketChannel != null && remoteConnectionMap.containsKey(remoteSocketChannel)) {
                remoteConnectionMap.remove(remoteSocketChannel);
                closeSocketChannel(remoteSocketChannel);
            }
            //TODO dataQueue
            dataQueue.removeConnection(connection);
            connection.wakeSelectors();
            LOG.debug("close connectoin [{}], total connection = [{}]", connection.getId(), connectionMap.size());
        }
    }

    public void closeSocketChannel(SocketChannel socketChannel) {
        if(socketChannel == null) {
            return;
        }
        try {
            LOG.debug("close socket channel [{}]...", socketChannel.getRemoteAddress());
            if(socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void closeAll() {
        connectionMap.values().forEach(connection -> {
            closeConnection(connection);
        });

        connectionMap.clear();
        localConnectionMap.clear();
        remoteConnectionMap.clear();
    }
    //addConnection
    public void addConnection(Connection connection) {
        connectionMap.put(Integer.valueOf(connection.getId()), connection);
        localConnectionMap.put(connection.getLocalSocket(), connection);
        remoteConnectionMap.put(connection.getRemoteSocket(), connection);
        LOG.debug("add connectoin [{}], total connection = [{}]", connection.getId(), connectionMap.size());
    }

    public Connection getConnectionByLocalSocket(SocketChannel localSocket) {
        return localConnectionMap.get(localSocket);
    }

    public Connection getConnectionByRemoteSocket(SocketChannel remoteSocket) {
        return remoteConnectionMap.get(remoteSocket);
    }

    public Connection getConnectionForKey(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Connection connection = getConnectionByLocalSocket(socketChannel) == null ? getConnectionByRemoteSocket(socketChannel) : getConnectionByLocalSocket(socketChannel);
        return connection;
    }
}
