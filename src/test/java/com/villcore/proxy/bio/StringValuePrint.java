package com.villcore.proxy.bio;

import org.junit.Test;

import javax.sound.midi.SoundbankResource;

/**
 *
 */
public class StringValuePrint {

    @Test
    public void printLineSeperator() {
        String str = "GET http://speedtest.com/ HTTP/1.1\n" +
                "Host: speedtest.com\n" +
                "Proxy-Connection: keep-alive\n" +
                "Cache-Control: max-age=0\n" +
                "Upgrade-Insecure-Requests: 1\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" +
                "DNT: 1\n" +
                "Accept-Encoding: gzip, deflate\n" +
                "Accept-Language: zh-CN,zh;q=0.8";

        for(int i = 0; i < str.length(); i++) {
            System.out.printf("%d -> %c\n", (int)str.charAt(i), str.charAt(i));
        }

        System.out.println(str.length());
    }
}
