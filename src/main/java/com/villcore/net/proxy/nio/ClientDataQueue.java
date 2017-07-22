package com.villcore.net.proxy.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by villcore on 2017/7/10.
 */
//TODO 对于已经关闭的Connection要有清理方法，不然会有内存泄露
//TODO 在加密解密的handler里面进行判断，如果已经关闭的request resp都需要清理，具体查找dataQueue与ConnectionManager
public class ClientDataQueue implements DataQueue {
    private static final Logger LOG = LoggerFactory.getLogger(ClientDataQueue.class);

    public final int DEFAULT_QUEUE_SIZE = 1000;

    private final BlockingQueue<Bundle> REQ_BUNDLE_QUEUE = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
    private final Map<Integer, BlockingQueue<Bundle>> REQ_BUNDLE_ENCRYPT_QUEUE = new ConcurrentHashMap<>();

    private final BlockingQueue<Bundle> RESP_BUNDLE_ENCRYPT_QUEUE = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
    private final Map<Integer, BlockingQueue<Bundle>> RESP_BUNDLE_QUEUE = new ConcurrentHashMap<>();

    //放入未加密本地request
    public void putRequest(Bundle bundle) throws InterruptedException {
        printSize();
        REQ_BUNDLE_QUEUE.put(bundle);
    }

    //取出未加密本地request用来加密
    public Bundle getRequest() throws InterruptedException {
        printSize();
        return REQ_BUNDLE_QUEUE.poll(100L, TimeUnit.MILLISECONDS);
    }

    //放入已经加密的本地request
    public void putEncryptRequest(int connectionId, Bundle bundle) throws InterruptedException {
        printSize();
        if(REQ_BUNDLE_ENCRYPT_QUEUE.containsKey(Integer.valueOf(connectionId))) {

        } else {
            BlockingQueue<Bundle> blockingQueue = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
            REQ_BUNDLE_ENCRYPT_QUEUE.put(Integer.valueOf(connectionId), blockingQueue);
        }
        REQ_BUNDLE_ENCRYPT_QUEUE.get(Integer.valueOf(connectionId)).put(bundle);

    }

    //取出已经加密的本地requset用来发送
    public Bundle getEncryptRequest(int connectionId) throws InterruptedException {
        printSize();
        if(REQ_BUNDLE_ENCRYPT_QUEUE.containsKey(Integer.valueOf(connectionId))) {
        } else {
            BlockingQueue<Bundle> blockingQueue = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
            REQ_BUNDLE_ENCRYPT_QUEUE.put(Integer.valueOf(connectionId), blockingQueue);
        }
        return REQ_BUNDLE_ENCRYPT_QUEUE.get(Integer.valueOf(connectionId)).poll(100L, TimeUnit.MILLISECONDS);
    }

    //放入加密的resp
    public void putEncryptResp(Bundle bundle) throws InterruptedException {
        printSize();
        RESP_BUNDLE_ENCRYPT_QUEUE.put(bundle);
    }

    //取出加密的resp用来解密
    public Bundle getEncryptResp() throws InterruptedException {
        printSize();
        return RESP_BUNDLE_ENCRYPT_QUEUE.poll(100L, TimeUnit.MILLISECONDS);
    }

    //放入解密的resp
    public void putResp(int connectionId, Bundle bundle) throws InterruptedException {
        printSize();
        if(RESP_BUNDLE_QUEUE.containsKey(Integer.valueOf(connectionId))) {
        } else {
            BlockingQueue<Bundle> blockingQueue = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
            RESP_BUNDLE_QUEUE.put(Integer.valueOf(connectionId), blockingQueue);
        }
        RESP_BUNDLE_QUEUE.get(Integer.valueOf(connectionId)).put(bundle);
    }
    //取出解密的resp

    //非阻塞访问,可以返回null
    public Bundle getResponse(int connectionId, long time) throws InterruptedException {
        printSize();
        if(RESP_BUNDLE_QUEUE.containsKey(Integer.valueOf(connectionId))) {
        } else {
            BlockingQueue<Bundle> blockingQueue = new ArrayBlockingQueue<Bundle>(DEFAULT_QUEUE_SIZE);
            RESP_BUNDLE_QUEUE.put(Integer.valueOf(connectionId), blockingQueue);
        }
        return RESP_BUNDLE_QUEUE.get(Integer.valueOf(connectionId)).poll(100, TimeUnit.MILLISECONDS);
    }

    public void removeResponseQueue(int connectionId) {
        printSize();
        if(RESP_BUNDLE_QUEUE.get(Integer.valueOf(connectionId)) == null) {
            return;
        }
        RESP_BUNDLE_QUEUE.remove(Integer.valueOf(connectionId)).clear();
    }

    public void removeConnection(Connection connection) {
        if(REQ_BUNDLE_ENCRYPT_QUEUE.containsKey(Integer.valueOf(connection.getId()))) {
            REQ_BUNDLE_ENCRYPT_QUEUE.remove(Integer.valueOf(connection.getId()));
        }
        if(RESP_BUNDLE_QUEUE.containsKey(Integer.valueOf(connection.getId()))) {
            RESP_BUNDLE_QUEUE.remove(Integer.valueOf(connection.getId()));
        }
    }

    public void printSize() {
//        StringBuilder sb = new StringBuilder();
//        LOG.debug("{}", sb.toString());
    }

}
