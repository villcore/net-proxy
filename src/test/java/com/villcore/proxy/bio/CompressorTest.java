package com.villcore.proxy.bio;

import com.villcore.net.proxy.bio.compressor.GZipCompressor;
import org.junit.Test;

import java.io.IOException;

public class CompressorTest {
    @Test
    public void GzipCompressorTest() throws IOException {
        String msg = "helloasdfffffffffffffffffwwssssssssssssssssssssssssssssssssssssssssssssssssssssfffffffffffffffffffffffwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwfasdfasdfasdfsadf...";
        System.out.println("ori_len = " + msg.length());
        GZipCompressor compressor = new GZipCompressor();
        byte[] compressBytes = compressor.compress(msg.getBytes());
        System.out.println(compressBytes.length);
        byte[] decompressBytes = compressor.decompress(compressBytes);
        System.out.println(decompressBytes.length);
        System.out.println(new String(decompressBytes));

    }
}
