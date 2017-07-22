package com.villcore.net.proxy.nio.server;

import com.villcore.net.proxy.nio.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/13.
 *
 * 从client端读取加密的request并放到ServerDataQueue
 */
public class ServerReadableSelectProcessor extends DefaultSelectProcessor {

    {
        LOG = LoggerFactory.getLogger(ServerReadableSelectProcessor.class);
    }

    private ServerDataQueue dataQueue;

    public ServerReadableSelectProcessor(DefaultConnectionManager connectionManager, ServerDataQueue dataQueue) {
        super(SelectionKey.OP_READ, connectionManager);
        this.dataQueue = dataQueue;
    }

    @Override
    public void doRead(SelectionKey key) throws IOException {
        LOG.debug("server read...");
        Connection connection = getConnection(key);
        if(connection == null) {
            key.cancel();
            key.channel().close();
            return;
        }
        //从client端读取加密的Request并放入DataQueue
        ByteBuffer byteBuffer = connection.getLocalReadBuf();

        int headerLen = 4 + 4 + 8 + 4;

        if (byteBuffer == null) {
            //新建header 读取size信息
            byteBuffer = ByteBufPool.acquire(headerLen);
        }

        while(true) {
            int hasRead = read(connection, byteBuffer);
            LOG.debug("server read client {} bytes...", hasRead);
            if(hasRead == 0) {
                break;
            }
            if (hasRead == -1) {
                connection.setLocalReadBuf(null);
                closeConnection(connection);
                key.cancel();
                return;
            }
        }

        if (!byteBuffer.hasRemaining()) {
            if (byteBuffer.capacity() == headerLen) {
                //新建header + body byteBuffer
                byteBuffer.flip();
                ByteBuffer newByteBuffer = ByteBufPool.acquire(byteBuffer.getInt(0));
                LOG.debug("server read request bundle size = {}", byteBuffer.getInt(0));
                byteBuffer.rewind();
                newByteBuffer.put(byteBuffer);
                byteBuffer = newByteBuffer;
            } else {
                //complete
                readComplete(connection, byteBuffer);
                connection.setLocalReadBuf(null);
                return;
            }
        }


        while(true) {
            int hasRead = read(connection, byteBuffer);
            LOG.debug("server read client {} bytes...", hasRead);
            if(hasRead == 0) {
                break;
            }
            if (hasRead == -1) {
                connection.setLocalReadBuf(null);
                closeConnection(connection);
                key.cancel();
                return;
            }
        }

        if (!byteBuffer.hasRemaining()) {
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
                connection.setLocalReadBuf(null);
                return;
            }
        }

        connection.setLocalReadBuf(byteBuffer);
    }

    @Override
    public Connection getConnection(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        return connectionManager.getConnectionByLocalSocket(socketChannel);
    }

    @Override
    public SocketChannel getRegisterSocketChannel(Connection connection) {
        return connection.getLocalSocket();
    }

    private void readComplete(Connection connection, ByteBuffer byteBuffer) {
        byteBuffer.flip();
        Bundle encryptResp = Bundle.valueOf(byteBuffer);
        encryptResp.setConnectionId(connection.getId());
        try {

            LOG.debug("server recv client data = \n{}", ByteBufferUtil.getContent(byteBuffer));
            dataQueue.putEncryptRequest(encryptResp);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public int read(Connection connection, ByteBuffer byteBuffer) throws IOException {
        return getRegisterSocketChannel(connection).read(byteBuffer);
    }
}
