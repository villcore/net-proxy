package com.villcore.proxy.bio;

import org.junit.Test;

import javax.sound.midi.SoundbankResource;

/**
 *
 */
public class StringValuePrint {

    @Test
    public void printLineSeperator() {
        String str = "CONNECT s.360.cn:443 HTTP/1.1";
        for(int i = 0; i < 5; i++) {
            System.out.println(str);
        }

        for(byte byteVal : str.getBytes()) {
            System.out.printf("%s ", (int)byteVal);
        }
    }
}
