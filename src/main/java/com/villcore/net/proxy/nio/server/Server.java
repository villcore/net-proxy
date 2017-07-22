package com.villcore.net.proxy.nio.server;

import com.villcore.net.proxy.nio.Connection;
import com.villcore.net.proxy.nio.DefaultConnectionManager;
import com.villcore.net.proxy.nio.RemoteSocketUtils;
import com.villcore.net.proxy.nio.RunnableTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by villcore on 2017/7/11.
 */
public class Server extends RunnableTask {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private Map<Integer, Connection> localConnections = new ConcurrentHashMap<>();

    private AtomicInteger connectionIdCount = new AtomicInteger();

    private int threadNum = 1;
    private int serverReaderSize = threadNum;
    private int serverWriterSize = threadNum;
    private int proxyReaderSize = threadNum;
    private int proxyWriterSize = threadNum;

    private ServerReadableSelectProcessor[] serverReaders;
    private ServerWriteableSelectProcessor[] serverWriters;
    private ProxyReadableSelectProcessor[] proxyReaders;
    private ProxyWriteableSelectProcessor[] proxyWriters;

    private int port;
    private ServerDataQueue dataQueue;

    private String proxyAddress;
    private int proxyPort;

    private DefaultConnectionManager connectionManager;

    public Server(int port, String proxyAddress, int proxyPort, ServerDataQueue dataQueue) {
        this.port = port;
        this.dataQueue = dataQueue;
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
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
                serverReaders = new ServerReadableSelectProcessor[serverReaderSize];
                for (int i = 0; i < serverReaderSize; i++) {
                    serverReaders[i] = new ServerReadableSelectProcessor(connectionManager, dataQueue);
                    serverReaders[i].start();
                    serverReaders[i].startThread();
                }
                //init writers
                serverWriters = new ServerWriteableSelectProcessor[serverWriterSize];
                for (int i = 0; i < serverWriterSize; i++) {
                    serverWriters[i] = new ServerWriteableSelectProcessor(connectionManager, dataQueue);
                    serverWriters[i].start();
                    serverWriters[i].startThread();
                }

                //init readers
                proxyReaders = new ProxyReadableSelectProcessor[proxyReaderSize];
                for (int i = 0; i < proxyReaderSize; i++) {
                    proxyReaders[i] = new ProxyReadableSelectProcessor(connectionManager, dataQueue);
                    proxyReaders[i].start();
                    proxyReaders[i].startThread();
                }
                //init writers
                proxyWriters = new ProxyWriteableSelectProcessor[proxyReaderSize];
                for (int i = 0; i < proxyReaderSize; i++) {
                    proxyWriters[i] = new ProxyWriteableSelectProcessor(connectionManager, dataQueue);
                    proxyWriters[i].start();
                    proxyWriters[i].startThread();
                }

                Thread.sleep(5000);
                LOG.info("{} start, listen port [{}]...", getClass().getSimpleName(), port);
                break;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (running) {
            try {
                if (selector.select() <= 0) {
                    LOG.debug("select no channel...");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        LOG.error(e.getMessage(), e);
//                    }
                    continue;
                }

                LOG.debug("select instrist channel...");

                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey key = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    if (!key.isValid()) {
                        LOG.debug("key invalid...");
//                        key.cancel();
//                        key.channel().close();
                    }

                    if (running && key.isValid() && key.isAcceptable()) {
                        try {
                            doAccept(key);
                        } catch (Exception e) {
                            key.channel().close();
                            LOG.error(e.getMessage(), e);
                            key.cancel();
                            break;
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

    private void doAccept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel clientSocketChannel = null;
        SocketChannel proxySocketChannel = null;
        try {
            clientSocketChannel = serverSocketChannel.accept();
            clientSocketChannel.configureBlocking(false);
            clientSocketChannel.socket().setTcpNoDelay(true);
            clientSocketChannel.socket().setKeepAlive(true);
            clientSocketChannel.socket().setSendBufferSize(128 * 1024);
            clientSocketChannel.socket().setReceiveBufferSize(128 * 1024);

            //TODO option
            proxySocketChannel = RemoteSocketUtils.connect(new InetSocketAddress(proxyAddress, proxyPort), 5, 1000);

            if (proxySocketChannel == null) {
                connectionManager.closeSocketChannel(proxySocketChannel);
                connectionManager.closeSocketChannel(clientSocketChannel);
                return;
            }
            proxySocketChannel.socket().setTcpNoDelay(true);
            proxySocketChannel.socket().setKeepAlive(true);
            proxySocketChannel.socket().setSendBufferSize(128 * 1024);
            proxySocketChannel.socket().setReceiveBufferSize(128 * 1024);

            Connection connection = connectionManager.newConnection(clientSocketChannel, proxySocketChannel);

            ServerReadableSelectProcessor serverReader = selectServerReader(connection);
            serverReader.pendingConnection(connection);
            //TODO localWriter
            ServerWriteableSelectProcessor serverWriter = selectServerWriter(connection);
            serverWriter.pendingConnection(connection);

            ProxyReadableSelectProcessor proxyReader = selectProxyReader(connection);
            proxyReader.pendingConnection(connection);
            //TODO localWriter
            ProxyWriteableSelectProcessor proxyWriter = selectProxyWriter(connection);
            proxyWriter.pendingConnection(connection);

            connectionManager.addConnection(connection);
            //LOG.info("server accept socket [{}] to [{}]...", clientSocketChannel.getRemoteAddress(), proxySocketChannel.getRemoteAddress());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            connectionManager.closeSocketChannel(clientSocketChannel);
            connectionManager.closeSocketChannel(proxySocketChannel);
        }

    }

    private ServerReadableSelectProcessor selectServerReader(Connection connection) {
        return serverReaders[connection.getId() % serverReaders.length];
    }

    private ServerWriteableSelectProcessor selectServerWriter(Connection connection) {
        return serverWriters[connection.getId() % serverWriters.length];
    }

    private ProxyReadableSelectProcessor selectProxyReader(Connection connection) {
        return proxyReaders[connection.getId() % proxyReaders.length];
    }

    private ProxyWriteableSelectProcessor selectProxyWriter(Connection connection) {
        return proxyWriters[connection.getId() % proxyWriters.length];
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
