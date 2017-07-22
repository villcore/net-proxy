package com.villcore.net.proxy.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/7/10.
 */
public class ClientCryptRunner extends RunnableTask {
    private static final Logger LOG = LoggerFactory.getLogger(ClientCryptRunner.class);

    private Crypt crypt;
    private ClientDataQueue dataQueue;

    public Crypt getCrypt() {
        return crypt;
    }

    public void setCrypt(Crypt crypt, ClientDataQueue dataQueue) {
        this.crypt = crypt;
        this.dataQueue = dataQueue;
    }

    @Override
    public void run() {
        while(running) {
            //request 加密
            if (running) {
                try {
                    Bundle request = dataQueue.getRequest();
                    if(request != null) {
                        ByteBuffer buffer = request.getByteBuffer();
                        buffer.rewind();
                        LOG.debug("encrypt request = \n{}", ByteBufferUtil.getContent(buffer));
                        ByteBuffer buffer2 = crypt.encrypt(buffer);
                        buffer2.rewind();
                        Bundle encryptRequest = Bundle.valueOf(buffer2);
                        //LOG.debug("put ...1");
                        dataQueue.putEncryptRequest(encryptRequest.getConnectionId(), encryptRequest);
                        //LOG.debug("put ...2");
                    }
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            //resp 解密
            if (running) {
                try {
                    Bundle encryptResp = dataQueue.getEncryptResp();
                    if(encryptResp != null) {
                        ByteBuffer buffer = encryptResp.getByteBuffer();
                        buffer.rewind();
                        Bundle resp = Bundle.valueOf(crypt.decrypt(buffer));
                        dataQueue.putResp(resp.getConnectionId(), resp);
                    }
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            if (running) {

            }

            if (running) {

            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        Thread.currentThread().interrupt();
    }

    //encrypt
    //decrypt
    //unencrypt queue
    //encrypt queue
}
