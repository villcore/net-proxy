package com.villcore.net.proxy.nio;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/13.
 */
public class ClientWriteableSelectProcessor extends DefaultSelectProcessor {

    {
        LOG = LoggerFactory.getLogger(ClientWriteableSelectProcessor.class);
    }

    private ClientDataQueue dataQueue;

    public ClientWriteableSelectProcessor(DefaultConnectionManager connectionManager, ClientDataQueue dataQueue) {
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
        //从DataQueue读取对应的未加密数据写入本地channel
        ByteBuffer byteBuffer = connection.getLocalWriteBuf();

        if (byteBuffer == null) {
            //从dataQueue中获取数据
            Bundle bundle = getBundle(connection);
            if (bundle == null) {
                connection.setLocalWriteBuf(null);
                return;
            }
            byteBuffer = bundle.getByteBuffer();
            byteBuffer.rewind();
            byteBuffer.position(headerLen);
        }

        while(true) {
            int hasWrite = write(connection, byteBuffer);
            LOG.debug("write {} bytes to local...", hasWrite);
            if(hasWrite == 0) {
                break;
            }
        }

        if (!byteBuffer.hasRemaining()) {
            //complete
            connection.setLocalReadBuf(null);
        } else {
            connection.setLocalReadBuf(byteBuffer);
        }
    }

    @Override
    public Connection getConnection(SelectionKey key) {
        SocketChannel localSocketChannel = (SocketChannel) key.channel();
        return connectionManager.getConnectionByLocalSocket(localSocketChannel);
    }

    @Override
    public SocketChannel getRegisterSocketChannel(Connection connection) {
        return connection.getLocalSocket();
    }

    public int write(Connection connection, ByteBuffer byteBuffer) throws IOException {
        return getRegisterSocketChannel(connection).write(byteBuffer);
    }

    public Bundle getBundle(Connection connection) {
        try {
            return dataQueue.getResponse(connection.getId(), 100);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
