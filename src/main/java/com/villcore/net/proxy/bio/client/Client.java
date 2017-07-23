package com.villcore.net.proxy.bio.client;

import com.villcore.net.proxy.bio.common.Connection;
import com.villcore.net.proxy.bio.util.SocketUtil;
import com.villcore.net.proxy.sysproxy.WinSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by villcore on 2017/7/17.
 */
public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException {
        List<Connection> connections = new LinkedList<>();

        int listenPort = 10080;
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 20080);

        ServerSocket serverSocket = null;

        WinSystemProxy proxy = null;
        try {
           proxy = new WinSystemProxy("win_utils");
        } catch (Exception e) {
            e.printStackTrace();
        }

        WinSystemProxy finalProxy = proxy;
        ServerSocket finalServerSocket = serverSocket;

        try {
            serverSocket = new ServerSocket(listenPort);
            try {
                String proxyAddress = "http://127.0.0.1:10080";
                proxy.setGlobalProxy(proxyAddress);
                System.out.println("set proxy address to : [" + proxyAddress + "]");
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (true) {
                Socket localSocket = serverSocket.accept();
                Socket remoteSocket = SocketUtil.connect(remoteAddress);
                if(remoteSocket == null) {
                    LOG.debug("can not connect {}...", remoteAddress);
                    localSocket.close();
                    continue;
                }

                SocketUtil.configSocket(localSocket);
                SocketUtil.configSocket(localSocket);
                Connection connection = new ClientConnection(localSocket, remoteSocket);
                connection.start();
                LOG.debug("client build connection [{} -> {}]...", localSocket.getRemoteSocketAddress().toString(), remoteSocket.getRemoteSocketAddress().toString());
                connections.add(connection);
            }
        } catch (IOException e) {
            proxy.clearProxy();
            LOG.error(e.getMessage(), e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    System.out.println("close client, reset proxy address...");
                    finalProxy.clearProxy();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }

                if(finalServerSocket != null) {
                    try {
                        finalServerSocket.close();
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                for(Connection connection : connections) {
                    connection.stop();
                }

            }
        });
    }
}
