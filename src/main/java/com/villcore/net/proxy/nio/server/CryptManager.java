package com.villcore.net.proxy.nio.server;

import com.villcore.net.proxy.nio.Crypt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by villcore on 2017/7/14.
 */
public class CryptManager {
    private Map<Long, Crypt> crypts = new ConcurrentHashMap<>();

    public synchronized void addCrypt(long userFlag, Crypt crypt) {
        crypts.put(Long.valueOf(userFlag), crypt);
    }

    public synchronized void remoteCrypt(long userFlag) {
        crypts.remove(Long.valueOf(userFlag));
    }

    //需要调用段检测是否为null
    public Crypt getCrypt(long userFlag) {
        return crypts.get(userFlag);
    }
}
