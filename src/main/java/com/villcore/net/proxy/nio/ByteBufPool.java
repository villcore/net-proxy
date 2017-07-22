package com.villcore.net.proxy.nio;

import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/7/10.
 */
public class ByteBufPool {
    //TODO default impl, need to rewrite
    public static ByteBuffer acquire(int size) {
        if(size > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("too much buffer for size = " + size);
        }
        return ByteBuffer.allocate(size);
    }

    public static void release(ByteBuffer byteBuffer) {
        byteBuffer = null;
    }
}
