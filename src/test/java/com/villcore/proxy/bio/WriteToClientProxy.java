package com.villcore.proxy.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class WriteToClientProxy {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("127.0.0.1", 10080);
        OutputStream os = socket.getOutputStream();
        os.write("hello this is a msg...".getBytes());
        os.flush();

        Thread.sleep(100 * 1000);
    }
}
