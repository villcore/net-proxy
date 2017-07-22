package com.villcore.net.proxy.nio.server;//package com.villcore.proxy2.proxy.server;
//
//import com.villcore.proxy2.proxy.Connection;
//import com.villcore.proxy2.proxy.ConnectionManager;
//import com.villcore.proxy2.proxy.RunnableTask;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.SocketChannel;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Created by villcore on 2017/7/11.
// *
// * 负责创建远程socket相关
// */
//public class ServerProxyConnector extends RunnableTask implements ConnectionManager {
//    private static final Logger LOG = LoggerFactory.getLogger(ServerProxyConnector.class);
//
//    private String address;
//    private int port;
//    private int proxyConnectionNum = 1;
//
//    private Selector selector;
//
//    private Map<Integer, Connection> proxyConnections = new ConcurrentHashMap<>();
//    private AtomicInteger connectionIdCount = new AtomicInteger();
//
//    private ServerDataQueue dataQueue;
//
//    private int proxyReaderSize = 1;
//    private int proxyWriterSize = 1;
//
//    private ProxyReadableSelectProcessor[] proxyReaders;
//    private ProxyWriteableSelectProcessor[] proxyWriters;
//
//    public ServerProxyConnector(String address, int port, ServerDataQueue dataQueue) {
//        this.address = address;
//        this.port = port;
//        this.dataQueue = dataQueue;
//    }
//
//    public ServerProxyConnector(String address, int port, ServerDataQueue dataQueue, int remoteConnectionNum) {
//        this.address = address;
//        this.port = port;
//        this.dataQueue = dataQueue;
//        this.proxyConnectionNum = proxyConnectionNum;
//    }
//
//    @Override
//    public void run() {
//        while (running) {
//            try {
//                selector = Selector.open();
//                //addToRemote reader
//                //addToRemote reader
//                //init readers
//                proxyReaders = new ProxyReadableSelectProcessor[proxyReaderSize];
//                for(int i = 0; i < proxyReaderSize; i++) {
//                    proxyReaders[i] = new ProxyReadableSelectProcessor(this, dataQueue);
//                    proxyReaders[i].start();
//                    proxyReaders[i].startThread();
//                }
//                //init writers
//                proxyWriters = new ProxyWriteableSelectProcessor[proxyWriterSize];
//                for(int i = 0; i < proxyWriterSize; i++) {
//                    proxyWriters[i] = new ProxyWriteableSelectProcessor(this, dataQueue);
//                    proxyWriters[i].start();
//                    proxyWriters[i].startThread();
//                }
//
//                Thread.sleep(5000);
//                break;
//            } catch (IOException e) {
//                LOG.error(e.getMessage(), e);
//            } catch (InterruptedException e) {
//                LOG.error(e.getMessage(), e);
//            }
//        }
//
//        while(running) {
//            if(needBuildConnection()) {
//                try {
//                    SocketChannel socketChannel = SocketChannel.open();
//                    socketChannel.configureBlocking(false);
////                    socketChannel.register(selector, SelectionKey.OP_CONNECT);
//                    socketChannel.socket().setKeepAlive(true);
//                    socketChannel.socket().setTcpNoDelay(true);
//                    socketChannel.socket().setSoTimeout(100 * 1000);
//                    socketChannel.connect(new InetSocketAddress(address, Integer.valueOf(port)));
//
//                    while(!socketChannel.finishConnect() ){
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            LOG.error(e.getMessage(), e);
//                        }
//                    }
//                    doConnect(socketChannel);
//                } catch (IOException e) {
//                    LOG.error(e.getMessage(), e);
//                }
//            }
//
////            try {
////                if (selector.select() > 0) {
////                    for(SelectionKey key : selector.selectedKeys()) {
////                        if(!key.isValid()) {
////                            closeKey(key);
////                        }
////
////                        if(running && key.isConnectable()) {
////                            doConnect(key);
////                        }
////                    }
////                }
////            } catch (IOException e) {
////                LOG.error(e.getMessage(), e);
////            }
//        }
//    }
//
//    private void closeKey(SelectionKey key) throws IOException {
//        key.cancel();
//        key.channel().close();
//    }
//
//
//    public boolean needBuildConnection() {
//        return proxyConnectionNum > proxyConnections.size();
//    }
//
////    public void doConnect(SelectionKey key) throws IOException {
////        SocketChannel socketChannel = (SocketChannel) key.channel();
////        LOG.info("connect to remote server...");
////        Connection remoteConnection = newConnection(socketChannel);
////        remoteConnections.put(remoteConnection.getId(), remoteConnection);
////
////        //addToRemote reader
////        //addToRemote reader
////        //init readers
////        remoteReaders = new ProxyReadableSelectProcessor[remoteReaderSize];
////        for(int i = 0; i < remoteReaderSize; i++) {
////            remoteReaders[i] = new ProxyReadableSelectProcessor(this, dataQueue);
////            remoteReaders[i].start();
////            remoteReaders[i].startThread();
////        }
////        //init writers
////        remoteWriters = new ProxyWriteableSelectProcessor[remoteWriterSize];
////        for(int i = 0; i < remoteWriterSize; i++) {
////            remoteWriters[i] = new ProxyWriteableSelectProcessor(this, dataQueue);
////            remoteWriters[i].start();
////            remoteWriters[i].startThread();
////        }
////    }
//
//    public void doConnect(SocketChannel socketChannel) throws IOException {
//        LOG.info("connect to proxy server...");
//        Connection proxyConnection = newConnection(socketChannel);
//        proxyConnections.put(proxyConnection.getId(), proxyConnection);
//
//        try {
//            selectProxyReader(proxyConnection).pendingConnection(proxyConnection);
//            selectProxyWriter(proxyConnection).pendingConnection(proxyConnection);
//        } catch (InterruptedException e) {
//            LOG.error(e.getMessage(), e);
//        }
//    }
//
//    private ProxyReadableSelectProcessor selectProxyReader(Connection connection) {
//        return proxyReaders[connection.getId() % proxyReaders.length];
//    }
//
//    private ProxyWriteableSelectProcessor selectProxyWriter(Connection connection) {
//        return proxyWriters[connection.getId() % proxyWriters.length];
//    }
//
//    @Override
//    public void stop() {
//        selector.wakeup();
//        super.stop();
//    }
//
//    public void closeConnection(int id) throws IOException {
//        Connection connection = proxyConnections.remove(Integer.valueOf(id));
//        if(connection != null) {
//            SocketChannel channel = connection.getSocketChannel();
//            channel.close();
//        }
//    }
//
//    public Connection newConnection(SocketChannel socketChannel) throws IOException {
//        while (connectionIdCount.get() == Integer.MAX_VALUE) {
//            connectionIdCount.set(0);
//        }
//
//        int connectionId =  connectionIdCount.getAndIncrement();
//        Connection connection = new Connection(connectionId, socketChannel, 0L);
//        proxyConnections.put(Integer.valueOf(connectionId), connection);
//        return connection;
//    }
//
//}
