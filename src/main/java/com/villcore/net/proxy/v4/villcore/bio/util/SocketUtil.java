package com.villcore.net.proxy.v4.villcore.bio.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/**
 * Created by villcre on 2017/7/17.
 */
public class SocketUtil {
    private static final Logger LOG = LoggerFactory.getLogger(Socket.class);

    private static final int TIME_OUT = 5 * 60 * 1000;

    public static Socket connect(InetSocketAddress address) {
        try {
            Socket socket = new Socket();
            configSocket(socket);
            socket.connect(address, TIME_OUT);
            return socket;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static Socket connectSSL(InetSocketAddress address) {
        try {
//            System.setProperty("javax.net.ssl.trustStore", "clienttrust");
//            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
//            Socket socket = ssf.createSocket(address.getAddress(), address.getPort());
            SSLSocketFactory factory =
                    (SSLSocketFactory)SSLSocketFactory.getDefault();
            SSLSocket socket =
                    (SSLSocket)factory.createSocket(address.getAddress(), address.getPort());
            //Socket socket = new Socket();
//            if(!socket.isConnected()) {
//                socket.connect(address);
//            }
            return socket;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static void configSocket(Socket socket) throws SocketException {
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        socket.setReuseAddress(true);
        // socket.setSoLinger();
        // socket.setOOBInline();
        // .setPerformancePreferences();
        // socket.setTrafficClass();
        socket.setSendBufferSize(128 * 1024);
        socket.setReceiveBufferSize(128 * 1024);
        socket.setSoTimeout(TIME_OUT);
    }
}
