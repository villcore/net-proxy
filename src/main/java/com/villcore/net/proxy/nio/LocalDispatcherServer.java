package com.villcore.net.proxy.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by villcore on 2017/7/10.
 * <p>
 * 本地ServerSocket，用来接收本地连接请求
 */
public class LocalDispatcherServer extends RunnableTask {
    private static final Logger LOG = LoggerFactory.getLogger(LocalDispatcherServer.class);

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private long userFlag = 0L;

    private AtomicInteger connectionIdCount = new AtomicInteger();

    private int port;
    private ClientDataQueue dataQueue;

    private String remoteAddress;
    private int remotePort;

    private DefaultConnectionManager connectionManager;

    private int threadNum = 1;
    private int localReaderSize = threadNum;
    private int localWriterSize = threadNum;
    private int remoteReaderSize = threadNum;
    private int remoteWriterSize = threadNum;

    private ClientReadableSelectProcessor[] localReaders;
    private ClientWriteableSelectProcessor[] localWriters;
    private RemoteReadableSelectProcessor[] remoteReaders;
    private RemoteWriteableSelectProcessor[] remoteWriters;

    public LocalDispatcherServer(long userFlag, int port, String remoteAddress, int remotePort, ClientDataQueue dataQueue) {
        this.userFlag = userFlag;
        this.port = port;
        this.dataQueue = dataQueue;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        connectionManager = new DefaultConnectionManager(dataQueue);
    }

    @Override
    public void run() {
        while (running) {
            try {
                selector = Selector.open();
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);

                //TODO add option infomation
                InetSocketAddress address = new InetSocketAddress(port);
                serverSocketChannel.socket().bind(address);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


                //init readers
                localReaders = new ClientReadableSelectProcessor[localReaderSize];
                for (int i = 0; i < localReaderSize; i++) {
                    localReaders[i] = new ClientReadableSelectProcessor(connectionManager, dataQueue);
                    localReaders[i].start();
                    localReaders[i].startThread();
                }
                //init writers
                localWriters = new ClientWriteableSelectProcessor[localWriterSize];
                for (int i = 0; i < localWriterSize; i++) {
                    localWriters[i] = new ClientWriteableSelectProcessor(connectionManager, dataQueue);
                    localWriters[i].start();
                    localWriters[i].startThread();
                }

                //init readers
                remoteReaders = new RemoteReadableSelectProcessor[remoteReaderSize];
                for (int i = 0; i < remoteReaderSize; i++) {
                    remoteReaders[i] = new RemoteReadableSelectProcessor(connectionManager, dataQueue);
                    remoteReaders[i].start();
                    remoteReaders[i].startThread();
                }
                //init writers
                remoteWriters = new RemoteWriteableSelectProcessor[localWriterSize];
                for (int i = 0; i < localWriterSize; i++) {
                    remoteWriters[i] = new RemoteWriteableSelectProcessor(connectionManager, dataQueue);
                    remoteWriters[i].start();
                    remoteWriters[i].startThread();
                }
                Thread.sleep(5000);
                LOG.info("{} start, listen port [{}]...", getClass().getSimpleName(), port);
                break;
            } catch (BindException e) {
                LOG.error(e.getMessage(), e);
                System.out.printf("port %d already in bind... sys will exit...\n", port);
                System.exit(1);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (running) {
            try {
                if (selector.select(500) <= 0) {
//                    LOG.debug("select no channel...");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        LOG.error(e.getMessage(), e);
//                    }
                    continue;
                }

                LOG.debug("select accept channel...");

                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

                while (selectionKeyIterator.hasNext()) {
                    SelectionKey key = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (!key.isValid()) {
                        LOG.debug("key invalid...");
                        key.cancel();
                        key.channel().close();
                    }

                    if (running && key.isValid() && key.isAcceptable()) {
                        try {
                            doAccept(key);
//                            if(key.channel().isOpen()) {
//                                key.channel().register(selector, SelectionKey.OP_ACCEPT);
//                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                            key.channel().close();
                            key.cancel();
                        }
                    }
                }
            } catch (IOException e) {
                close();
                LOG.error(e.getMessage(), e);
            }
        }
        close();
        LOG.info("{} stoped...", getClass().getSimpleName());
    }

    private void doAccept(SelectionKey key) throws IOException, InterruptedException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel localSocketChannel = null;
        SocketChannel remoteSocketChannel = null;
        try {
            localSocketChannel = serverSocketChannel.accept();
            localSocketChannel.configureBlocking(false);
            localSocketChannel.socket().setTcpNoDelay(true);
            localSocketChannel.socket().setKeepAlive(true);
            localSocketChannel.socket().setSendBufferSize(128 * 1024);
            localSocketChannel.socket().setReceiveBufferSize(128 * 1024);
            LOG.debug("accept channel from [{}]...", localSocketChannel.getRemoteAddress());
//            int a = 1;
//            if(a == 1) {
//                localSocketChannel.close();
//                return;
//            }
            //TODO option
            remoteSocketChannel = RemoteSocketUtils.connect(new InetSocketAddress(remoteAddress, remotePort), 5, 1000);

            if (remoteSocketChannel == null) {
                connectionManager.closeSocketChannel(localSocketChannel);
                LOG.info("connection remote socket [{}:{}] failed...close connection", remoteAddress, remotePort);
                return;
            }
            remoteSocketChannel.socket().setTcpNoDelay(true);
            remoteSocketChannel.socket().setKeepAlive(true);
            remoteSocketChannel.socket().setSendBufferSize(128 * 1024);
            remoteSocketChannel.socket().setReceiveBufferSize(128 * 1024);
            LOG.info("connection remote socket [{}:{}] success...", remoteAddress, remotePort);

            Connection connection = connectionManager.newConnection(localSocketChannel, remoteSocketChannel, userFlag);

            ClientReadableSelectProcessor localReader = selectLocalReader(connection);
            ClientWriteableSelectProcessor localWriter = selectLocalWriter(connection);

            RemoteReadableSelectProcessor remoteReader = selectRemoteReader(connection);
            RemoteWriteableSelectProcessor remoteWriter = selectRemoteWriter(connection);

            connectionManager.addConnection(connection);

            localReader.pendingConnection(connection);
            localWriter.pendingConnection(connection);
            remoteReader.pendingConnection(connection);
            remoteWriter.pendingConnection(connection);
            LOG.info("build connection [{}] from [{}] to [{}] success...", connection.getId(), localSocketChannel.getRemoteAddress(), remoteSocketChannel.getRemoteAddress());

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            connectionManager.closeSocketChannel(localSocketChannel);
            connectionManager.closeSocketChannel(remoteSocketChannel);
        }
        //localSocketChannel.register(selector, -1);
    }

    private ClientReadableSelectProcessor selectLocalReader(Connection connection) {
        return localReaders[connection.getId() % localReaders.length];
    }

    private ClientWriteableSelectProcessor selectLocalWriter(Connection connection) {
        return localWriters[connection.getId() % localWriters.length];
    }

    private RemoteReadableSelectProcessor selectRemoteReader(Connection connection) {
        return remoteReaders[connection.getId() % remoteReaders.length];
    }

    private RemoteWriteableSelectProcessor selectRemoteWriter(Connection connection) {
        return remoteWriters[connection.getId() % remoteWriters.length];
    }

    private SocketChannel createRemoteSocket(InetSocketAddress address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(address);
        socketChannel.finishConnect();
        return socketChannel;
    }

    @Override
    public void stop() {
        selector.wakeup();
        super.stop();
    }

    private void close() {
        connectionManager.closeAll();
    }
}
