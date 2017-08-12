package com.villcore.proxy.bio;



import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketAddress socketAddress = new InetSocketAddress("www.baidu.com", 443);
        Socket socket = new Socket();
        socket.connect(socketAddress);

        String msg = "CONNECT www.baidu.com:443 HTTP/1.0\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063\n" +
                "Content-Length: 0\r\n" +
                "Host: www.baidu.com\r\n" +
                "Proxy-Connection: Keep-Alive\r\n" +
                "Pragma: no-cache\r\n" +
                "\r\n";

        socket.getOutputStream().write(msg.getBytes());
        socket.getOutputStream().flush();

        Thread.sleep(5 * 1000);
        InputStream is = socket.getInputStream();

        byte[] bytes = new byte[1024];
        int pos = -1;

        while(true) {
            pos = is.read(bytes);
            if(pos < 0) {
                continue;
            }
            System.out.println(new String(bytes, 0, pos));
            Thread.sleep(5 * 100);
        }
        //System.out.println(new String(bytes));
    }
}
