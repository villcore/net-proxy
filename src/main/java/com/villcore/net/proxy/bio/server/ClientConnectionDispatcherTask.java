package com.villcore.net.proxy.bio.server;

import com.villcore.net.proxy.bio.client.ClientConnection;
import com.villcore.net.proxy.bio.common.Connection;
import com.villcore.net.proxy.bio.handler.Handler;
import com.villcore.net.proxy.bio.pkg2.Package;
import com.villcore.net.proxy.bio.util.HttpParser;
import com.villcore.net.proxy.bio.util.SocketUtil;
import com.villcore.net.proxy.nio.RunnableTask;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class read first package from client, and parse the dst connection, then build a socket to dst
 */
public class ClientConnectionDispatcherTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ClientConnection.class);

    private Socket clientSocket;
    private Map<String, Handler> handlerChain;
    private List<Connection> connections;

    public ClientConnectionDispatcherTask(Socket clientSocket, Map<String, Handler> handlerChain, List<Connection> connections) {
        this.clientSocket = clientSocket;
        this.handlerChain = handlerChain;
        this.connections = connections;
    }

    @Override
    public void run() {
        try {
            Package pkg = new Package();
            pkg.readPackageWithHeader(clientSocket.getInputStream());

            LOG.debug("header len = {}, body len = {}", pkg.getHeaderLen(), pkg.getBodyLen());
            for (Map.Entry<String, Handler> entry : handlerChain.entrySet()) {
                pkg = entry.getValue().handle(pkg);
            }

            byte[] httpRequest = pkg.getBody();
            InetSocketAddress address = HttpParser.parseAddress2(httpRequest);
            if (address == null) {
                throw new IllegalArgumentException("parse address error...");
            }

            LOG.debug("need connect remote server {}", address.toString());

            Socket remoteSocket = null;
            if(address.getPort() == 443) {
                //https
                remoteSocket = SocketUtil.connectSSL(address);
                LOG.debug("connect https server {}", address);
            } else {
                remoteSocket = SocketUtil.connect(address);
            }

            if (remoteSocket == null) {
                LOG.info("can not connect {}...", address);
                clientSocket.close();
                return;
            }

            LOG.debug("first pkg content = \n{}\n", new String(pkg.getBody()));
            //pkg.writePackageWithoutHeader(remoteSocket.getOutputStream());
            remoteSocket.getOutputStream().write(pkg.getBody());
            remoteSocket.getOutputStream().flush();

            SocketUtil.configSocket(clientSocket);
            SocketUtil.configSocket(remoteSocket);
            Connection connection = new ServerConnection(clientSocket, remoteSocket);
            connection.start();

            LOG.info("server build connection [{} -> {}]...", clientSocket.getRemoteSocketAddress().toString(), remoteSocket.getRemoteSocketAddress().toString());
            connections.add(connection);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            try {
                clientSocket.close();
            } catch (IOException e1) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
