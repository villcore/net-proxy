package com.villcore.net.proxy.nio.server;

import com.villcore.net.proxy.nio.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/13.
 */
public class ProxyWriteableSelectProcessor extends DefaultSelectProcessor {

    {
        LOG = LoggerFactory.getLogger(ProxyWriteableSelectProcessor.class);
    }

    private ServerDataQueue dataQueue;


    public ProxyWriteableSelectProcessor(DefaultConnectionManager connectionManager, ServerDataQueue dataQueue) {
        super(SelectionKey.OP_WRITE, connectionManager);
        this.dataQueue = dataQueue;
    }

    @Override
    public void doWrite(SelectionKey key) throws IOException {
        Connection connection = getConnection(key);
        if(connection == null) {
            key.cancel();
            key.channel().close();
            return;
        }
        int headerLen = 4 + 4 + 8 + 4;

        ByteBuffer byteBuffer = connection.getRemoteWriteBuf();

        if (byteBuffer == null) {
            Bundle bundle = getBundle(connection);
            if(bundle == null) {
                connection.setRemoteWriteBuf(null);
                return;
            } else {
                byteBuffer = bundle.getByteBuffer();
                byteBuffer.rewind();
                byteBuffer.position(headerLen);
            }
        }

        while (true) {
            int hasWrite = write(connection, byteBuffer);
            LOG.debug("write {} bytes to proxy...", hasWrite);
            if(hasWrite == 0) {
                break;
            }
            LOG.debug("write to proxy...\n{}", ByteBufferUtil.getContent(byteBuffer));
        }


        if (!byteBuffer.hasRemaining()) {
            completeWrite(connection, byteBuffer);
            connection.setRemoteWriteBuf(null);
            return;
        }
        connection.setRemoteWriteBuf(byteBuffer);

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

    public Bundle getBundle(Connection connection) {
        try {
            return dataQueue.getRequest(connection.getId());
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
    public int write(Connection connection, ByteBuffer byteBuffer) throws IOException {
        return getRegisterSocketChannel(connection).write(byteBuffer);
    }

    public void completeWrite(Connection connection, ByteBuffer byteBuffer) {
    }
}
