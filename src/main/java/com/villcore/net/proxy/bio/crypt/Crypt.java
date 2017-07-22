package com.villcore.net.proxy.bio.crypt;

/**
 * Created by villcore on 2017/7/17.
 */
public interface Crypt {
    void setPassword(String password);
    byte[] encrypt(byte[] bytes);
    byte[] decrypt(byte[] bytes);

    byte[] encryptHeader(byte[] randomBytes, byte[] header);
    byte[] encryptBody(byte[] body);
}
