package com.villcore.net.proxy.nio;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by villcore on 2017/7/10.
 */
public class Connection {
    private int id;

    private SocketChannel localSocket;
    private SocketChannel remoteSocket;
    private List<Selector> selectorList = new LinkedList<>();

    private ByteBuffer localReadBuf;
    private ByteBuffer localWriteBuf;

    private ByteBuffer remoteReadBuf;
    private ByteBuffer remoteWriteBuf;

    private long userFlag;

    public Connection(SocketChannel localSocket, SocketChannel remoteSocket, long userFlag) {
        this.localSocket = localSocket;
        this.remoteSocket = remoteSocket;
        this.userFlag = userFlag;
    }

    public Connection(SocketChannel localSocket, SocketChannel remoteSocket) {
        this.localSocket = localSocket;
        this.remoteSocket = remoteSocket;
    }

    public void addSelector(Selector selector) {
        selectorList.add(selector);
    }

    public void wakeSelectors() {
//        for(Selector selector : selectorList) {
//            selector.wakeup();
//        }
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SocketChannel getLocalSocket() {
        return localSocket;
    }

    public void setLocalSocket(SocketChannel localSocket) {
        this.localSocket = localSocket;
    }

    public SocketChannel getRemoteSocket() {
        return remoteSocket;
    }

    public void setRemoteSocket(SocketChannel remoteSocket) {
        this.remoteSocket = remoteSocket;
    }

    public ByteBuffer getLocalReadBuf() {
        return localReadBuf;
    }

    public void setLocalReadBuf(ByteBuffer localReadBuf) {
        this.localReadBuf = localReadBuf;
    }

    public ByteBuffer getLocalWriteBuf() {
        return localWriteBuf;
    }

    public void setLocalWriteBuf(ByteBuffer localWriteBuf) {
        this.localWriteBuf = localWriteBuf;
    }

    public ByteBuffer getRemoteReadBuf() {
        return remoteReadBuf;
    }

    public void setRemoteReadBuf(ByteBuffer remoteReadBuf) {
        this.remoteReadBuf = remoteReadBuf;
    }

    public ByteBuffer getRemoteWriteBuf() {
        return remoteWriteBuf;
    }

    public void setRemoteWriteBuf(ByteBuffer remoteWriteBuf) {
        this.remoteWriteBuf = remoteWriteBuf;
    }

    public long getUserFlag() {
        return userFlag;
    }

    public void setUserFlag(long userFlag) {
        this.userFlag = userFlag;
    }
}
