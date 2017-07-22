package com.villcore.net.proxy.nio;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by villcore on 2017/7/13.
 */
public class TestSocket {
    public static void main(String[] args) throws IOException, InterruptedException {
        String test = String.format("%08d,%05d", 10, 12);
        System.out.println(test);

        Socket socket = new Socket();
        System.out.println("connect...");
        socket.connect(new InetSocketAddress("127.0.0.1", 3128));
        System.out.println("connected...");

        String http = "" +
                "CONNECT go.microsoft.com:443 HTTP/1.0\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063\r\n" +
                "Content-Length: 0\r\n" +
                "Host: go.microsoft.com\r\n" +
                "Proxy-Connection: Keep-Alive\r\n" +
                "Pragma: no-cache\r\n" +
                "\r\n";

        OutputStream os = socket.getOutputStream();
        os.write(http.getBytes());
        os.flush();

        System.out.println("write complete...");

        InputStream is = socket.getInputStream();
        byte[] bytes = new byte[is.available()];
        int read = is.read(bytes);
        System.out.println(new String(bytes, 0, read));
        System.out.println("read complete...");

        Thread.sleep(100000);
    }
}
