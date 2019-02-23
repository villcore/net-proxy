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
        remoteAddress = "207.246.108.224";
        remotePort = 20081;
        password = "villcore2";

        socketServer = new SocketServer(listenPort, remoteAddress, remotePort, password);
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
