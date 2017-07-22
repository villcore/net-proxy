package com.villcore.net.proxy.nio.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2017/7/15.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(10080);

        while(true)  {
            Socket localSocket = serverSocket.accept();
            System.out.println("connect from " + localSocket.getRemoteSocketAddress().toString());
            //localSocket.close();
//            Socket remoteSocket = new Socket();
//            remoteSocket.connect(new InetSocketAddress("127.0.0.1", 3128));

//            OutputStream los = localSocket.getOutputStream();
//            InputStream lis = localSocket.getInputStream();
//
//            OutputStream ros = remoteSocket.getOutputStream();
//            InputStream ris = remoteSocket.getInputStream();
//
//            new Transfer(lis, ros).start();
//            new Transfer(ris, los).start();

        }
    }

    static class Transfer extends Thread{
        InputStream is;
        OutputStream os;

        public Transfer(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }
        @Override
        public void run() {
            byte[] bytes = new byte[1024];
            int pos = -1;

            while(true) {
                try {
                    while((pos = is.read(bytes)) > 0) {
                        os.write(bytes, 0, pos);
                        os.flush();
                    }
                } catch (IOException e) {

                }
            }
        }
    }
}
