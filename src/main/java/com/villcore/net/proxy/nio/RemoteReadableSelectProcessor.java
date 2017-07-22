package com.villcore.net.proxy.nio;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/13.
 */
public class RemoteReadableSelectProcessor extends DefaultSelectProcessor {
    private ClientDataQueue dataQueue;

    {
        LOG = LoggerFactory.getLogger(RemoteReadableSelectProcessor.class);
    }

    public RemoteReadableSelectProcessor(DefaultConnectionManager connectionManager, ClientDataQueue dataQueue) {
        super(SelectionKey.OP_READ, connectionManager);
        this.dataQueue = dataQueue;
    }

    @Override
    public void doRead(SelectionKey key) throws IOException {
        LOG.debug("remote do read...");
        Connection connection = getConnection(key);
        if(connection == null) {
            LOG.debug("connecton is null...");
            key.cancel();
            key.channel().close();
            return;
        }
        //从远程服务端读取加密的Resp并放入DataQueue
        ByteBuffer byteBuffer = connection.getRemoteReadBuf();
        //判断
        int headerLen = 4 + 4 + 8 + 4;

        if(byteBuffer == null) {
            //新建header 读取size信息
            byteBuffer = ByteBufPool.acquire(headerLen);
        }

        while(true) {
            int hasRead = read(connection, byteBuffer);
            LOG.debug("read remote {} bytes...", hasRead);
            if(hasRead == 0) {
                break;
            }
            if (hasRead == -1) {
                connection.setRemoteReadBuf(null);
                closeConnection(connection);
                key.cancel();
                return;
            }
        }

        if(!byteBuffer.hasRemaining()) {
            if (byteBuffer.capacity() == headerLen) {
                //新建header + body byteBuffer
                byteBuffer.flip();
                ByteBuffer newByteBuffer = ByteBufPool.acquire(byteBuffer.getInt(0));
                byteBuffer.rewind();
                newByteBuffer.put(byteBuffer);
                byteBuffer = newByteBuffer;
            } else {
                //complete
                readComplete(connection, byteBuffer);
                connection.setRemoteReadBuf(null);
                return;
            }
        }

        while(true) {
            int hasRead = read(connection, byteBuffer);
            LOG.debug("read remote {} bytes...", hasRead);
            if(hasRead == 0) {
                break;
            }
            if (hasRead == -1) {
                connection.setRemoteReadBuf(null);
                closeConnection(connection);
                key.cancel();
                return;
            }
        }

        if(!byteBuffer.hasRemaining()) {
            if (byteBuffer.capacity() == headerLen) {
                //新建header + body byteBuffer
                byteBuffer.flip();
                ByteBuffer newByteBuffer = ByteBufPool.acquire(byteBuffer.getInt(0));
                byteBuffer.rewind();
                newByteBuffer.put(byteBuffer);
                byteBuffer = newByteBuffer;
            } else {
                //complete
                readComplete(connection, byteBuffer);
                connection.setRemoteReadBuf(null);
                return;
            }
        }

        connection.setRemoteReadBuf(byteBuffer);
    }

    @Override
    public Connection getConnection(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        return connectionManager.getConnectionByRemoteSocket(socketChannel);
    }

    @Override
    public SocketChannel getRegisterSocketChannel(Connection connection) {
        return connection.getRemoteSocket();
    }

    private void readComplete(Connection connection, ByteBuffer byteBuffer) {
        byteBuffer.flip();
        Bundle encryptResp = Bundle.valueOf(byteBuffer);
        encryptResp.setConnectionId(connection.getId());
        try {
            dataQueue.putEncryptResp(encryptResp);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public int read(Connection connection, ByteBuffer byteBuffer) throws IOException {
        return connection.getRemoteSocket().read(byteBuffer);
    }

    public void buildHeader(Connection connection, ByteBuffer byteBuffer) {
        byteBuffer.putInt(0);
        byteBuffer.putInt(connection.getId());
        byteBuffer.putLong(connection.getUserFlag());
        byteBuffer.putInt(-1);
    }
}
