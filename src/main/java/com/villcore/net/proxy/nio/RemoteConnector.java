package com.villcore.net.proxy.nio;//package com.villcore.proxy2.proxy;
//
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
//public class RemoteConnector extends RunnableTask implements ConnectionManager {
//    private static final Logger LOG = LoggerFactory.getLogger(RemoteConnector.class);
//
//    private String address;
//    private int port;
//    private int remoteConnectionNum = 1;
//
//    private Selector selector;
//
//    private Map<Integer, Connection> remoteConnections = new ConcurrentHashMap<>();
//    private AtomicInteger connectionIdCount = new AtomicInteger();
//
//    private DataQueue dataQueue;
//
//    private int remoteReaderSize = 1;
//    private int remoteWriterSize = 1;
//
//    private RemoteReadableSelectProcessor[] remoteReaders;
//    private RemoteWriteableSelectProcessor[] remoteWriters;
//
//    public RemoteConnector(String address, int port, DataQueue dataQueue) {
//        this.address = address;
//        this.port = port;
//        this.dataQueue = dataQueue;
//    }
//
//    public RemoteConnector(String address, int port, DataQueue dataQueue, int remoteConnectionNum) {
//        this.address = address;
//        this.port = port;
//        this.dataQueue = dataQueue;
//        this.remoteConnectionNum = remoteConnectionNum;
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
//                remoteReaders = new RemoteReadableSelectProcessor[remoteReaderSize];
//                for(int i = 0; i < remoteReaderSize; i++) {
//                    remoteReaders[i] = new RemoteReadableSelectProcessor(this, dataQueue);
//                    remoteReaders[i].start();
//                    remoteReaders[i].startThread();
//                }
//                //init writers
//                remoteWriters = new RemoteWriteableSelectProcessor[remoteWriterSize];
//                for(int i = 0; i < remoteWriterSize; i++) {
//                    remoteWriters[i] = new RemoteWriteableSelectProcessor(this, dataQueue);
//                    remoteWriters[i].start();
//                    remoteWriters[i].startThread();
//                }
//
//                Thread.sleep(5000);
//                break;
//            } catch (IOException e) {
//                LOG.error(e.getMessage(), e);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        while(running) {
//            if(needBuildConnection()) {
//                try {
//                    SocketChannel socketChannel = SocketChannel.open();
//                    socketChannel.configureBlocking(false);
//                    //socketChannel.register(selector, SelectionKey.OP_CONNECT);
//                    socketChannel.connect(new InetSocketAddress(address, Integer.valueOf(port)));
//                    while(!socketChannel.finishConnect() ){
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            LOG.error(e.getMessage(), e);
//                        }
//                    }
//                    doConnect(socketChannel);
//                    LOG.debug("connect server success...");
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
//        return remoteConnectionNum > remoteConnections.size();
//    }
//
////    public void doConnect(SelectionKey key) throws IOException {
////        SocketChannel socketChannel = (SocketChannel) key.channel();
////        LOG.info("connect to remote server [{}]...", socketChannel.getRemoteAddress());
////        Connection remoteConnection = newConnection(socketChannel);
////        remoteConnections.put(remoteConnection.getId(), remoteConnection);
////
////        //addToRemote reader
////        //addToRemote reader
////        //init readers
////        remoteReaders = new RemoteReadableSelectProcessor[remoteReaderSize];
////        for(int i = 0; i < remoteReaderSize; i++) {
////            remoteReaders[i] = new RemoteReadableSelectProcessor(this, dataQueue);
////            remoteReaders[i].start();
////            remoteReaders[i].startThread();
////        }
////        //init writers
////        remoteWriters = new RemoteWriteableSelectProcessor[remoteWriterSize];
////        for(int i = 0; i < remoteWriterSize; i++) {
////            remoteWriters[i] = new RemoteWriteableSelectProcessor(this, dataQueue);
////            remoteWriters[i].start();
////            remoteWriters[i].startThread();
////        }
////    }
//
//    public void doConnect(SocketChannel socketChannel) throws IOException {
//        LOG.info("connect to remote server [{}]...", socketChannel.getRemoteAddress());
//        Connection remoteConnection = newConnection(socketChannel);
//        remoteConnections.put(remoteConnection.getId(), remoteConnection);
//
//        try {
//            selectRemoteReader(remoteConnection).pendingConnection(remoteConnection);
//            selectRemoteWriter(remoteConnection).pendingConnection(remoteConnection);
//        } catch (InterruptedException e) {
//            LOG.error(e.getMessage(), e);
//        }
//    }
//
//    private RemoteReadableSelectProcessor selectRemoteReader(Connection connection) {
//        return remoteReaders[connection.getId() % remoteReaders.length];
//    }
//
//    private RemoteWriteableSelectProcessor selectRemoteWriter(Connection connection) {
//        return remoteWriters[connection.getId() % remoteWriters.length];
//    }
//
//    @Override
//    public void stop() {
//        selector.wakeup();
//        super.stop();
//    }
//
//    public void closeConnection(int id) throws IOException {
//        Connection connection = remoteConnections.remove(Integer.valueOf(id));
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
//        remoteConnections.put(Integer.valueOf(connectionId), connection);
//        return connection;
//    }
//
//}
