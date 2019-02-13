package com.villcore.net.proxy.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create by WangTao on 2019/1/25
 */
public class NetProxyClient {

    private static final Logger LOG = LoggerFactory.getLogger(NetProxyClient.class);

    private final int listenPort;
    private final String remoteAddress;
    private final int remotePort;
    private final String password;

    private final SocketServer socketServer;

    public NetProxyClient(Config config) {
        // TODO parser argument
        listenPort = 50082;
        remoteAddress = "localhost";
        remotePort = 60082;
        password = "villcore";

        Crypt crypt = new Crypt();
        byte[] key = crypt.generateKey(password);
        byte[] iv = crypt.generateIv();
        crypt.setIv(iv);
        crypt.setKey(key);
        crypt.initDecrypt();
        crypt.initEncrypt();

        socketServer = new SocketServer(listenPort, remoteAddress, remotePort, crypt);
    }

    public void startup() {
        LOG.info("Starting NetProxyClient, listen port {}, remote address {}, remote port {}", listenPort, remoteAddress, remotePort);
        LOG.info("Start NetProxyClient completed");
        // TODO start socket server listen
        socketServer.startup();
    }

    public void shutdown() {
        LOG.info("Shutdowning NetProxyClient");
        socketServer.shutdown();
        LOG.info("Shutdown NetProxyClient completed");
    }

    private void startSocketServer(int listenPort, String remoteAddress, int remotePort, String password) {

    }

    public static void main(String[] args) {
        Config clientConfig = null;
        NetProxyClient client = new NetProxyClient(clientConfig);
        client.startup();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                client.shutdown();
            }
        }));
    }
}
