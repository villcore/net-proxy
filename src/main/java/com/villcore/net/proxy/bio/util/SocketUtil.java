package com.villcore.net.proxy.bio.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by villcre on 2017/7/17.
 */
public class SocketUtil {
    private static final Logger LOG = LoggerFactory.getLogger(Socket.class);

    public static Socket connect(InetSocketAddress address) {
        try {
            Socket socket = new Socket();
            socket.connect(address);
            return socket;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static Socket connectSSL(InetSocketAddress address) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "clienttrust");
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket socket = ssf.createSocket(address.getAddress(), address.getPort());
            //Socket socket = new Socket();
            socket.connect(address);
            return socket;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static void configSocket(Socket socket) throws SocketException {
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        socket.setSendBufferSize(128 * 1024);
        socket.setReceiveBufferSize(128 * 1024);
    }
}
