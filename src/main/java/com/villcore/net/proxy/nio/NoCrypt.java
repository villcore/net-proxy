package com.villcore.net.proxy.nio;


import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/7/11.
 */
public class NoCrypt implements Crypt {
    private String password;

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public ByteBuffer encrypt(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        return byteBuffer;
    }

    @Override
    public ByteBuffer decrypt(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        return byteBuffer;
    }
}
