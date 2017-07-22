package com.villcore.net.proxy.nio;

import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/7/11.
 */
public interface Crypt {
    void setPassword(String password);
    ByteBuffer encrypt(ByteBuffer byteBuffer);
    ByteBuffer decrypt(ByteBuffer byteBuffer);
}
