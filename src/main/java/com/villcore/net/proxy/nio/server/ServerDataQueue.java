package com.villcore.net.proxy.nio.server;


import com.villcore.net.proxy.nio.Bundle;
import com.villcore.net.proxy.nio.Connection;
import com.villcore.net.proxy.nio.DataQueue;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by villcore on 2017/7/14.
 */
public class ServerDataQueue implements DataQueue {
    public final int DEFAULT_QUEUE_SIZE = 1000;

    //加密request
    private final BlockingQueue<Bundle> REQ_BUNDLE_ENCRYPT_QUEUE = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);

    //未加密request
    private final Map<Integer, BlockingQueue<Bundle>> REQ_BUNDLE_QUEUE = new ConcurrentHashMap<>();

    //未加密resp
    private final BlockingQueue<Bundle> RESP_BUNDLE_QUEUE = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);

    //加密resp
    private final Map<Integer, BlockingQueue<Bundle>> RESP_BUNDLE_ENCRYPT_QUEUE = new ConcurrentHashMap<>();

    //放入加密的client request
    public void putEncryptRequest(Bundle bundle) throws InterruptedException {
        REQ_BUNDLE_ENCRYPT_QUEUE.put(bundle);
    }

    //取出加密的 client requset用来解密
    public Bundle getEncryptRequest() throws InterruptedException {
        return REQ_BUNDLE_ENCRYPT_QUEUE.poll(100L, TimeUnit.MILLISECONDS);
    }

    //放入解密的client request
    public void putRequest(int connectionId, Bundle bundle) throws InterruptedException {
        if (REQ_BUNDLE_QUEUE.containsKey(Integer.valueOf(connectionId))) {
        } else {
            BlockingQueue<Bundle> blockingQueue = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
            REQ_BUNDLE_QUEUE.put(Integer.valueOf(connectionId), blockingQueue);
        }
        REQ_BUNDLE_QUEUE.get(Integer.valueOf(connectionId)).put(bundle);
    }

    //取出解密的client request用来发送给Proxy
    public Bundle getRequest(int connectionId) throws InterruptedException {
        if (REQ_BUNDLE_QUEUE.containsKey(Integer.valueOf(connectionId))) {
        } else {
            BlockingQueue<Bundle> blockingQueue = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
            REQ_BUNDLE_QUEUE.put(Integer.valueOf(connectionId), blockingQueue);
        }
        return REQ_BUNDLE_QUEUE.get(Integer.valueOf(connectionId)).poll(100, TimeUnit.MILLISECONDS);
    }

    //放入未加密的resp
    public void putResp(Bundle bundle) throws InterruptedException {
        RESP_BUNDLE_QUEUE.put(bundle);
    }

    //取出未加密的resp用来加密
    public Bundle getResp() throws InterruptedException {
        return RESP_BUNDLE_QUEUE.poll(100L, TimeUnit.MILLISECONDS);
    }

    //放入加密的resp
    public void putEncryptResp(int connectionId, Bundle bundle) throws InterruptedException {
        //TODO null判断
        if (RESP_BUNDLE_ENCRYPT_QUEUE.containsKey(Integer.valueOf(connectionId))) {
        } else {
            BlockingQueue<Bundle> blockingQueue = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
            RESP_BUNDLE_ENCRYPT_QUEUE.put(Integer.valueOf(connectionId), blockingQueue);
        }
        RESP_BUNDLE_ENCRYPT_QUEUE.get(Integer.valueOf(bundle.getConnectionId())).put(bundle);
    }

    //取出加密的resp 发送给client, 需要null判断
    public Bundle getEncryptResp(int connectionId, long time) throws InterruptedException {
        if (RESP_BUNDLE_ENCRYPT_QUEUE.containsKey(Integer.valueOf(connectionId))) {
        } else {
            BlockingQueue<Bundle> blockingQueue = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
            RESP_BUNDLE_ENCRYPT_QUEUE.put(Integer.valueOf(connectionId), blockingQueue);
        }
        return RESP_BUNDLE_ENCRYPT_QUEUE.get(Integer.valueOf(connectionId)).poll(time, TimeUnit.MILLISECONDS);
    }

    public void removeResponseQueue(int connectionId) {
        if (RESP_BUNDLE_ENCRYPT_QUEUE.get(Integer.valueOf(connectionId)) == null) {
            return;
        }
        RESP_BUNDLE_ENCRYPT_QUEUE.remove(Integer.valueOf(connectionId)).clear();
    }

    @Override
    public void removeConnection(Connection connection) {
        if (REQ_BUNDLE_QUEUE.containsKey(Integer.valueOf(connection.getId()))) {
            REQ_BUNDLE_QUEUE.remove(Integer.valueOf(connection.getId()));
        }
        if (RESP_BUNDLE_ENCRYPT_QUEUE.containsKey(Integer.valueOf(connection.getId()))) {
            RESP_BUNDLE_ENCRYPT_QUEUE.remove(Integer.valueOf(connection.getId()));
        }
    }
}
