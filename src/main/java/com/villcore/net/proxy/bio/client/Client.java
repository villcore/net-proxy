package com.villcore.net.proxy.bio.client;

import com.villcore.net.proxy.bio.common.Connection;
import com.villcore.net.proxy.bio.util.SocketUtil;
import com.villcore.net.proxy.conf.ClientConfig;
import com.villcore.net.proxy.conf.Config;
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
 *
 */
public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    public static void start(ClientConfig config) throws IOException {
        List<Connection> connections = new LinkedList<>();

        int listenPort = Integer.valueOf(config.getProxyPort());
        String remoteAddr = config.getRemoteAddr();
        int remotePort = Integer.valueOf(config.getRemotePort());

        InetSocketAddress remoteAddress = new InetSocketAddress(remoteAddr, remotePort);

        ServerSocket serverSocket = null;

        ServerSocket finalServerSocket = serverSocket;

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
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

        try {
            serverSocket = new ServerSocket(listenPort);
            while (true) {
                Socket localSocket = serverSocket.accept();
                Socket remoteSocket = SocketUtil.connect(remoteAddress);
                if(remoteSocket == null) {
                    LOG.info("can not connect remote server [{}:{}] ...", remoteAddr, remotePort);
                    localSocket.close();
                    continue;
                }

                SocketUtil.configSocket(localSocket);
                SocketUtil.configSocket(remoteSocket);

                Connection connection = new ClientConnection(localSocket, remoteSocket);
                connection.start();
                connections.add(connection);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
