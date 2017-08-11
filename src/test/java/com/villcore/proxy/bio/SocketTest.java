package com.villcore.proxy.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketTest {
    public static void main(String[] args) throws IOException {
        SocketAddress socketAddress = new InetSocketAddress("box.bdimg.com", 80);
        Socket socket = new Socket();
        socket.connect(socketAddress);
    }
}
