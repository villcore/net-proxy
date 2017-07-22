package com.villcore.net.proxy.nio.server;

import com.villcore.net.proxy.nio.Bundle;
import com.villcore.net.proxy.nio.ByteBufferUtil;
import com.villcore.net.proxy.nio.Crypt;
import com.villcore.net.proxy.nio.RunnableTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/7/13.
 */
public class ServerCryptRunner extends RunnableTask {
        private static final Logger LOG = LoggerFactory.getLogger(ServerCryptRunner.class);

        private Crypt crypt;
        private ServerDataQueue dataQueue;

        public Crypt getCrypt() {
            return crypt;
        }

        public void setCrypt(Crypt crypt, ServerDataQueue dataQueue) {
            this.crypt = crypt;
            this.dataQueue = dataQueue;
        }

        @Override
        public void run() {
            while(running) {
                //从dataQueue获取加密的request解密并将解密的request放入dataqueue
                //request 加密
                if (running) {
                    try {
                        Bundle request = dataQueue.getEncryptRequest();
                        if(request != null) {
                            ByteBuffer buffer = request.getByteBuffer();
                            buffer.rewind();
                            Bundle request2 = Bundle.valueOf(crypt.decrypt(buffer));
                            LOG.debug("decrypt client request = \n{}", ByteBufferUtil.getContent(buffer));
                            dataQueue.putRequest(request.getConnectionId(), request2);
                        }
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                //从resp中获取未加密的resp，加密之后放入加密resp，根据connectionId
                //resp 解密
                if (running) {
                    try {
                        Bundle resp = dataQueue.getResp();
                        if(resp != null) {
                            ByteBuffer buffer = resp.getByteBuffer();
                            buffer.rewind();
                            Bundle encryptResp = Bundle.valueOf(crypt.encrypt(buffer));
                            dataQueue.putEncryptResp(encryptResp.getConnectionId(), encryptResp);
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
