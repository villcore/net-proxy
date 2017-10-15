package com.villcore.pkg.v2;

import com.villcore.ConnectPackageTest;
import com.villcore.net.proxy.v3.pkg.v2.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.v2.ConnectRespPackage;
import com.villcore.net.proxy.v3.pkg.v2.*;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class PackageTest {
    @Test
    public void testConnectReqPackag() throws Exception {
        Package pkg = (Package) PackageUtils.buildConnectPackage("www.baidu.com", (short)80, 100, 1L);

        System.out.println(pkg.getTotalLen());
        System.out.println(pkg.getFixed().length);
        System.out.println(pkg.getHeaderLen());
        System.out.println(pkg.getBodyLen());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(pkg.getFixed());
        byteArrayOutputStream.write(pkg.getHeader());
        byteArrayOutputStream.write(pkg.getBody());

        ConnectReqPackage connectReqPackage = (ConnectReqPackage) new ConnectReqPackage().valueOf(byteArrayOutputStream.toByteArray());
        System.out.println(connectReqPackage.getHostname());
        System.out.println(connectReqPackage.getPort());
        System.out.println(connectReqPackage.getConnId());
    }

    @Test
    public void testConnectRespPackag() throws Exception {
        Package pkg = (Package) PackageUtils.buildConnectRespPackage(100, 200, 1L);

        System.out.println(pkg.getTotalLen());
        System.out.println(pkg.getFixed().length);
        System.out.println(pkg.getHeaderLen());
        System.out.println(pkg.getBodyLen());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(pkg.getFixed());
        byteArrayOutputStream.write(pkg.getHeader());
        byteArrayOutputStream.write(pkg.getBody());

        ConnectRespPackage connectRespPackage = (ConnectRespPackage) new ConnectRespPackage().valueOf(byteArrayOutputStream.toByteArray());
        System.out.println(connectRespPackage.getLocalConnId());
        System.out.println(connectRespPackage.getRemoteConnId());
    }

    @Test
    public void testChannelClosePackag() throws Exception {
        Package pkg = (Package) PackageUtils.buildChannelClosePackage(1000, 2000, 1L);

        System.out.println(pkg.getTotalLen());
        System.out.println(pkg.getFixed().length);
        System.out.println(pkg.getHeaderLen());
        System.out.println(pkg.getBodyLen());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(pkg.getFixed());
        byteArrayOutputStream.write(pkg.getHeader());
        byteArrayOutputStream.write(pkg.getBody());

        ChannelClosePackage channelClosePackage = (ChannelClosePackage) new ChannelClosePackage().valueOf(byteArrayOutputStream.toByteArray());
        System.out.println(channelClosePackage.getLocalConnId());
        System.out.println(channelClosePackage.getRemoteConnId());
    }

    @Test
    public void testDataPackag() throws Exception {
        Package pkg = (Package) PackageUtils.buildDataPackage(10000, 20000, 1L, "HELLO".getBytes());

        System.out.println(pkg.getTotalLen());
        System.out.println(pkg.getFixed().length);
        System.out.println(pkg.getHeaderLen());
        System.out.println(pkg.getBodyLen());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(pkg.getFixed());
        byteArrayOutputStream.write(pkg.getHeader());
        byteArrayOutputStream.write(pkg.getBody());

        DefaultDataPackage defaultDataPackage = (DefaultDataPackage) new DefaultDataPackage().valueOf(byteArrayOutputStream.toByteArray());
        System.out.println(defaultDataPackage.getLocalConnId());
        System.out.println(defaultDataPackage.getRemoteConnId());
        System.out.println(new String(defaultDataPackage.getBody()));
    }
}
