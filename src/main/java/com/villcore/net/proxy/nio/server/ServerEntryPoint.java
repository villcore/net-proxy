package com.villcore.net.proxy.nio.server;


import com.villcore.net.proxy.nio.Crypt;
import com.villcore.net.proxy.nio.NoCrypt;
import com.villcore.net.proxy.nio.RunnableTask;

/**
 * Created by villcore on 2017/7/15.
 */
public class ServerEntryPoint {
    public static void main(String[] args) throws InterruptedException {
        ServerDataQueue dataQueue = new ServerDataQueue();
        RunnableTask server = new Server(20080, "127.0.0.1", 3128, dataQueue);
        server.start();
        server.startThread();

        //cypher running
        Crypt crypt = new NoCrypt();
        ServerCryptRunner cryptRunner = new ServerCryptRunner();
        cryptRunner.setCrypt(crypt, dataQueue);
        cryptRunner.start();
        cryptRunner.startThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop();
                cryptRunner.stop();
            }
        });
    }
}
