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
public class ProxyReadableSelectProcessor extends DefaultSelectProcessor {
    {
        LOG = LoggerFactory.getLogger(ProxyReadableSelectProcessor.class);
    }

    private ServerDataQueue dataQueue;

    public ProxyReadableSelectProcessor(DefaultConnectionManager connectionManager, ServerDataQueue dataQueue) {
        super(SelectionKey.OP_READ, connectionManager);
        this.dataQueue = dataQueue;
    }

    @Override
    public void doRead(SelectionKey key) throws IOException {
        LOG.debug("===============================================================read from proxy...");
        Connection connection = getConnection(key);
        if (connection == null) {
            key.cancel();
            key.channel().close();
            return;
        }
        int headerLen = 4 + 8 + 4 + 4;

        ByteBuffer byteBuffer = ByteBufPool.acquire(1 * 1024 * 1024); //1M

        //构建 header
        buildHeader(connection, byteBuffer);

        //读取操作
        while (true) {
            int hasRead = read(connection, byteBuffer);
            if (hasRead == 0) {
                break;
            }
            if (hasRead == -1) {
                closeConnection(connection);
                key.cancel();
                return;
            }
//            LOG.info("pos = {}, limit = {}, cap = {}", byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity());
        }
 //       LOG.info("read from proxy == \n{}", new String(byteBuffer.array(), headerLen, byteBuffer.limit() - headerLen));
//        LOG.debug("read {} bytes from proxy...", hasRead, ByteBufferUtil.getContent(byteBuffer));
//        //LOG.debug("read resp from proxy = \n{}", ByteBufferUtil.getContent(byteBuffer));
//


        LOG.info("pos = {}, limit = {}, cap = {}", byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity());
        readComplete(connection, byteBuffer);
        connection.setRemoteReadBuf(null);
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

    public int read(Connection connection, ByteBuffer byteBuffer) throws IOException {
        return getRegisterSocketChannel(connection).read(byteBuffer);
    }

    public void buildHeader(Connection connection, ByteBuffer byteBuffer) {
        byteBuffer.putInt(0);
        byteBuffer.putInt(connection.getId());
        byteBuffer.putLong(connection.getUserFlag());
        byteBuffer.putInt(-1);
    }

    public void readComplete(Connection connection, ByteBuffer byteBuffer) {
        byteBuffer.flip();
        byteBuffer.putInt(0, byteBuffer.limit());
        byteBuffer.rewind();
        //send to data queue
        try {
            dataQueue.putResp(Bundle.valueOf(byteBuffer));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}