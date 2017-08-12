package com.villcore.proxy.bio;

import org.junit.Test;

import javax.sound.midi.SoundbankResource;

/**
 *
 */
public class StringValuePrint {

    @Test
    public void printLineSeperator() {
        String str = "CONNECT www.baidu.com:443 HTTP/1.0\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063\n" +
                "Content-Length: 0\r\n" +
                "Host: www.baidu.com\r\n" +
                "Proxy-Connection: Keep-Alive\r\n" +
                "Pragma: no-cache\r\n" +
                "\r\n";
        for(int i = 0; i < 5; i++) {
            System.out.println(str);
        }

        for(byte byteVal : str.getBytes()) {
            System.out.printf("%s (%s)", (int)byteVal, (char)byteVal);
        }
    }
}
