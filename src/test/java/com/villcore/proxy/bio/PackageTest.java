package com.villcore.proxy.bio;

import com.villcore.net.proxy.bio.pkg.DefaultPackage;
import com.villcore.net.proxy.bio.pkg.Package;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPOutputStream;

public class PackageTest {
    private byte[] bytes;

    @Before
    public void constructHeaderBytes() {
        int headerLen = 20;

        String msg = "hello this is a test str...";
        int size = msg.length();
        byte[] headerBytes = new byte[headerLen];
        if(headerLen > 4) {
            ByteBuffer.wrap(headerBytes).putInt(size);
        }

        bytes = ByteBuffer.wrap(new byte[headerLen + size]).put(headerBytes).put(msg.getBytes()).array();
    }

//    @Before
//    public void constructBytes() {
//        int headerLen = 0;
//        String msg = "hello this is a test str...";
//        int size = msg.length();
//        byte[] bytes = new byte[headerLen];
//        if(headerLen > 4) {
//            ByteBuffer.wrap(bytes).putInt(size);
//        }
//
//        bytes = ByteBuffer.wrap(new byte[headerLen + size]).put(bytes).put(msg.getBytes()).array();
//    }

    @Test
    public void readPkgWithHeader() throws IOException {
        System.out.println(bytes == null);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        Package pkg = new DefaultPackage();
        pkg.readPackageWithHeader(byteArrayInputStream);
        System.out.println(pkg.getHeader().length);
        System.out.println(pkg.getBody().length);
        System.out.println(pkg.getSize(pkg.getHeader()));
        System.out.println(new String(pkg.getBody()));
    }

    @Test
    public void readPkgWithoutHeader() throws IOException {
        System.out.println(bytes == null);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("this is a msg...".getBytes());

        Package pkg = new DefaultPackage();
        pkg.readPackageWithoutHeader(byteArrayInputStream);
        System.out.println(pkg.getHeader().length);
        System.out.println(pkg.getBody().length);
        System.out.println(pkg.getSize(pkg.getHeader()));
        System.out.println(new String(pkg.getBody()));
        System.out.println("--");
    }

    @Test
    public void writePkgWithHeader() throws IOException {
        System.out.println(bytes == null);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        Package pkg = new DefaultPackage();
        pkg.readPackageWithHeader(byteArrayInputStream);
//        System.out.println(pkg.getHeader().length);
//        System.out.println(pkg.getBody().length);
//        System.out.println(pkg.getSize(pkg.getHeader()));
//        System.out.println(new String(pkg.getBody()));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pkg.writePackageWithHeader(byteArrayOutputStream);
        byte[] bytes2 = byteArrayOutputStream.toByteArray();
        System.out.println(ByteBuffer.wrap(bytes2).getInt());
        System.out.println(new String(bytes2, 20, bytes2.length - 20));
    }

    @Test
    public void writePkgWithoutHeader() throws IOException {
        System.out.println(bytes == null);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        Package pkg = new DefaultPackage();
        pkg.readPackageWithHeader(byteArrayInputStream);
//        System.out.println(pkg.getHeader().length);
//        System.out.println(pkg.getBody().length);
//        System.out.println(pkg.getSize(pkg.getHeader()));
//        System.out.println(new String(pkg.getBody()));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pkg.writePackageWithoutHeader(byteArrayOutputStream);
        byte[] bytes2 = byteArrayOutputStream.toByteArray();
        System.out.println(new String(bytes2, 0, bytes2.length));
    }

    @Test
    public void testCompress() throws IOException {
       String msg = "ssssssssssssssssssssssssssssssssssssssssssssssssss";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(msg.getBytes());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos);
        int pos = -1;
        byte[] bytes = new byte[1024];
        while((pos = byteArrayInputStream.read(bytes)) > 0) {
            gzipOutputStream.write(bytes, 0, pos);
        }

        System.out.println(bos.toByteArray().length);
    }
}
