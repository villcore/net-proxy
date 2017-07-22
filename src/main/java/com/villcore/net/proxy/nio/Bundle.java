package com.villcore.net.proxy.nio;

import java.nio.ByteBuffer;

public class Bundle {

    //size connectionId userId requestId
    private int size;
    private int connectionId;
    private long userFlag;
    private int requestId;

    private ByteBuffer requestByteBuffer;

    public static Bundle valueOf(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        int size = byteBuffer.getInt();
        int connectionId = byteBuffer.getInt();
        long userFlag = byteBuffer.getLong();
        int requestId = byteBuffer.getInt();
        return new Bundle(size, connectionId, userFlag, requestId, byteBuffer);
    }

    public Bundle(int size, int connectionId, long userFlag, int requestId, ByteBuffer requestByteBuffer) {
        this.size = size;
        this.connectionId = connectionId;
        this.userFlag = userFlag;
        this.requestId = requestId;
        this.requestByteBuffer = requestByteBuffer;
    }


    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        int originPos = requestByteBuffer.position();
        requestByteBuffer.putInt(4, connectionId);
        requestByteBuffer.position(originPos);
        this.connectionId = connectionId;
    }

    public long getUserFlag() {
        return userFlag;
    }

    public void setUserFlag(long userFlag) {
        int originPos = requestByteBuffer.position();
        requestByteBuffer.putLong(8, userFlag);
        requestByteBuffer.position(originPos);
        this.userFlag = userFlag;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        int originPos = requestByteBuffer.position();
        requestByteBuffer.putInt(16, requestId);
        requestByteBuffer.position(originPos);
        this.requestId = requestId;
    }

    public ByteBuffer getByteBuffer() {
        return requestByteBuffer;
    }

    public void setRequestByteBuffer(ByteBuffer requestByteBuffer) {
        this.requestByteBuffer = requestByteBuffer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bundle bundle = (Bundle) o;

        if (connectionId != bundle.connectionId) return false;
        if (userFlag != bundle.userFlag) return false;
        if (requestId != bundle.requestId) return false;
        return requestByteBuffer != null ? requestByteBuffer.equals(bundle.requestByteBuffer) : bundle.requestByteBuffer == null;
    }

    @Override
    public int hashCode() {
        int result = connectionId;
        result = 31 * result + (int) (userFlag ^ (userFlag >>> 32));
        result = 31 * result + requestId;
        result = 31 * result + (requestByteBuffer != null ? requestByteBuffer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Bundle{" +
                "connectionId=" + connectionId +
                ", size=" + size +
                ", userFlag=" + userFlag +
                ", requestId=" + requestId +
                ", requestByteBuffer=" + requestByteBuffer +
                '}';
    }
}
