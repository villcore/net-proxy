package com.villcore.net.proxy.nio;

import com.villcore.net.proxy.sys.WinSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by villcore on 2017/7/10.
 */
public class ClientEntrypoint {
    private static final Logger LOG = LoggerFactory.getLogger(ClientEntrypoint.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        WinSystemProxy proxy = new WinSystemProxy("win_utils");
        proxy.setGlobalProxy("http://127.0.0.1:10080");

        ClientDataQueue dataQueue = new ClientDataQueue();
        //RunnableTask server = new LocalDispatcherServer(1L, 10080, "45.63.120.186", 20080, dataQueue);
        RunnableTask server = new LocalDispatcherServer(1L, 10080, "127.0.0.1", 20080, dataQueue);

        server.start();
        server.startThread();

        //cypher running
        Crypt crypt = new NoCrypt();
        ClientCryptRunner cryptRunner = new ClientCryptRunner();
        cryptRunner.setCrypt(crypt, dataQueue);
        cryptRunner.start();
        cryptRunner.startThread();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                server.stop();
                try {
                    proxy.clearProxy();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cryptRunner.stop();
            }
        });

//        RemoteConnector remoteConnector = new RemoteConnector("127.0.0.1", 20080, dataQueue);
//        remoteConnector.start();
//        remoteConnector.startThread();
        //server.startThread().join();
    }
}
