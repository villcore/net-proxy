package com.villcore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by villcore on 2017/9/30.
 */
public class SocketReconnectTest {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        try {
            socket.bind(new InetSocketAddress("127.0.0.1", 60070));
            socket.connect(new InetSocketAddress("127.0.0.1", 20081));
            socket.getInputStream().read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
