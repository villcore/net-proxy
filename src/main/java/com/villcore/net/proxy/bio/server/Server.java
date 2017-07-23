package com.villcore.net.proxy.bio.server;

import com.villcore.net.proxy.bio.common.Connection;
import com.villcore.net.proxy.bio.util.SocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/17.
 */
public class Server {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        List<Connection> connections = new LinkedList<>();

        int listenPort = 20080;
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 3128);

        ServerSocket serverSocket = null;
        final ServerSocket finalServerSocket = serverSocket;

        try {
            serverSocket = new ServerSocket(listenPort);

            while (true) {
                Socket localSocket = serverSocket.accept();
                Socket remoteSocket = SocketUtil.connect(remoteAddress);
                if(remoteSocket == null) {
                    LOG.info("can not connect {}...", remoteAddress);
                    localSocket.close();
                    continue;
                }
                SocketUtil.configSocket(localSocket);
                SocketUtil.configSocket(remoteSocket);
                Connection connection = new ServerConnection(localSocket, remoteSocket);
                connection.start();
                LOG.info("server build connection [{} -> {}]...", localSocket.getRemoteSocketAddress().toString(), remoteSocket.getRemoteSocketAddress().toString());
                connections.add(connection);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

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
    }
}
