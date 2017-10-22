package com.villcore.pkg.v2;

import com.villcore.ConnectPackageTest;
import com.villcore.net.proxy.v3.pkg.v2.ConnectReqPackage;
import com.villcore.net.proxy.v3.pkg.v2.ConnectRespPackage;
import com.villcore.net.proxy.v3.pkg.v2.*;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthReqPackage;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthRespPackage;
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

    @Test
    public void testConnAuthReqPackag() throws Exception {
        Package pkg = (Package) PackageUtils.buildConnectAuthReqPackage("villcore", "123123");

        System.out.println(pkg.getTotalLen());
        System.out.println(pkg.getFixed().length);
        System.out.println(pkg.getHeaderLen());
        System.out.println(pkg.getBodyLen());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(pkg.getFixed());
        byteArrayOutputStream.write(pkg.getHeader());
        byteArrayOutputStream.write(pkg.getBody());

        ConnectAuthReqPackage connectAuthReqPackage = (ConnectAuthReqPackage) new ConnectAuthReqPackage().valueOf(byteArrayOutputStream.toByteArray());
        System.out.println(connectAuthReqPackage.getUsername());
        System.out.println(connectAuthReqPackage.getPassword());
        System.out.println(new String(connectAuthReqPackage.getBody()));
    }

    @Test
    public void testConnAuthRespPackag() throws Exception {
        Package pkg = (Package) PackageUtils.buildConnectAuthRespPackage("villcore", (short)100);

        System.out.println(pkg.getTotalLen());
        System.out.println(pkg.getFixed().length);
        System.out.println(pkg.getHeaderLen());
        System.out.println(pkg.getBodyLen());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(pkg.getFixed());
        byteArrayOutputStream.write(pkg.getHeader());
        byteArrayOutputStream.write(pkg.getBody());

        ConnectAuthRespPackage authRespPackage = (ConnectAuthRespPackage) new ConnectAuthRespPackage().valueOf(byteArrayOutputStream.toByteArray());
        System.out.println(authRespPackage.getUsername());
        System.out.println(authRespPackage.getStateCode());
        System.out.println(new String(authRespPackage.getBody()));
    }

    @Test
    public void testTransferPackag() throws Exception {
        Package pkg = (Package) PackageUtils.buildConnectAuthRespPackage("villcore", (short)100);
        Package transferPackage = PackageUtils.buildTransferPackage(pkg.getPkgType(), CompressType.COMPRESS_GZIP, new byte[16], pkg.toBytes());

        System.out.println(transferPackage.getTotalLen());
        System.out.println(transferPackage.getFixed().length);
        System.out.println(transferPackage.getHeaderLen());
        System.out.println(transferPackage.getBodyLen());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(transferPackage.getFixed());
        byteArrayOutputStream.write(transferPackage.getHeader());
        byteArrayOutputStream.write(transferPackage.getBody());


        TransferPackage transferPackage2 = (TransferPackage) new TransferPackage().valueOf(byteArrayOutputStream.toByteArray());
        byte[] wrapBytes = transferPackage2.getBody();
        System.out.println("wrap bytes len = " + wrapBytes.length);
        ConnectAuthRespPackage authRespPackage = (ConnectAuthRespPackage) new ConnectAuthRespPackage().valueOf(wrapBytes);

        System.out.println(authRespPackage.getUsername());
        System.out.println(authRespPackage.getStateCode());
        System.out.println(new String(authRespPackage.getBody()));
    }
}
