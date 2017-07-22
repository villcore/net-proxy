package com.villcore.net.proxy.nio.server;

import com.villcore.net.proxy.nio.Bundle;
import com.villcore.net.proxy.nio.Connection;
import com.villcore.net.proxy.nio.DefaultConnectionManager;
import com.villcore.net.proxy.nio.DefaultSelectProcessor;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/13.
 */
public class ServerWriteableSelectProcessor extends DefaultSelectProcessor {

    {
        LOG = LoggerFactory.getLogger(ServerWriteableSelectProcessor.class);
    }

    private ServerDataQueue dataQueue;

    public ServerWriteableSelectProcessor(DefaultConnectionManager connectionManager, ServerDataQueue dataQueue) {
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
            if(bundle == null) {
                connection.setLocalWriteBuf(null);
                return;
            }
            byteBuffer = bundle.getByteBuffer();
            byteBuffer.rewind();
            LOG.debug("server write bundle size = [{}]", byteBuffer.getInt(0));
            byteBuffer.rewind();

        }

        while(true) {
            int hasWrite = write(connection, byteBuffer);
            LOG.debug("write {} bytes to client...", hasWrite);
            if(hasWrite == 0) {
                break;
            }
        }

        if (!byteBuffer.hasRemaining()) {
            //complete
            completeWrite(byteBuffer);
            connection.setLocalWriteBuf(null);
        } else {
            connection.setLocalWriteBuf(byteBuffer);
        }
    }

    private void completeWrite(ByteBuffer byteBuffer) {
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

    public int write(Connection connection, ByteBuffer byteBuffer) throws IOException {
        return getRegisterSocketChannel(connection).write(byteBuffer);
    }

    public Bundle getBundle(Connection connection) {
        try {
            return dataQueue.getEncryptResp(connection.getId(), 100);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
