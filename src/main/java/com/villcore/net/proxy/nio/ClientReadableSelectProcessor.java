package com.villcore.net.proxy.nio;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by villcore on 2017/7/13.
 */
public class ClientReadableSelectProcessor extends DefaultSelectProcessor {

    {
        LOG = LoggerFactory.getLogger(ClientReadableSelectProcessor.class);
    }

    private ClientDataQueue dataQueue;

    public  ClientReadableSelectProcessor(DefaultConnectionManager connectionManager, ClientDataQueue dataQueue) {
        super(SelectionKey.OP_READ, connectionManager);
        this.dataQueue = dataQueue;
    }

    @Override
    public void doRead(SelectionKey key) throws IOException {
        Connection connection = getConnection(key);
        if(connection == null) {
            key.cancel();
            key.channel().close();
            return;
        }
        //不需要为Connection绑定readerBuffer，该读取是一次完成的
        //connection.setReadBuf(null);

        //从本地客户端读取未加密的字节
        int headerLen = 4 + 8 + 4 + 4;

        //申请 1M byteBuffer
        ByteBuffer byteBuffer = ByteBufPool.acquire(1 * 1024 * 1024); //1M

        //构建 header
        buildHeader(connection, byteBuffer);

        //读取操作
        while(true) {
            int hasRead = read(connection, byteBuffer);
            LOG.debug("read local request {} bytes...", hasRead);
            if(hasRead == 0) {
                break;
            }
            if (hasRead == -1) {
                closeConnection(connection);
                key.cancel();
                return;
            }
        }

        connection.setLocalReadBuf(null);
        readComplete(connection, byteBuffer);
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
//        int size = byteBuffer.position();
        byteBuffer.flip();
        byteBuffer.putInt(0, byteBuffer.limit());
        byteBuffer.rewind();
        //send to data queue
        try {
            dataQueue.putRequest(Bundle.valueOf(byteBuffer));
            LOG.debug("local recv request  = \n{}", ByteBufferUtil.getContent(byteBuffer));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
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


}
