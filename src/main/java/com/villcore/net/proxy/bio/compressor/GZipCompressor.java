package com.villcore.net.proxy.bio.compressor;

import com.villcore.net.proxy.bio.compressor.Compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) throws IOException {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bytes.length * 2);
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.flush();
            gzipOutputStream.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IOException {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            int pos = -1;
            byte[] tmp = new byte[128 * 1024];
            while((pos = gzipInputStream.read(tmp)) > 0) {
                byteArrayOutputStream.write(tmp, 0, pos);
            }
            gzipInputStream.close();
            byteArrayInputStream.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }
    }
}
