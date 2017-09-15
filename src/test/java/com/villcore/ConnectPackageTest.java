package com.villcore;

import com.villcore.net.proxy.v2.pkg.ConnectReqPackage;
import com.villcore.net.proxy.v2.pkg.PackageUtils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class ConnectPackageTest {
    @Test
    public void connectPackageBuildTest() throws UnsupportedEncodingException {
        ConnectReqPackage connectReqPackage = PackageUtils.buildConnectPackage("www.baidu.com", Short.valueOf("10090"), 1, 1L);
        System.out.println(connectReqPackage.getTotalLen());
        System.out.println(connectReqPackage.toByteBuf().readableBytes());

        //System.out.println(connectReqPackage.getHeaderLen());
        System.out.println(connectReqPackage.getHeader().readableBytes());

        //System.out.println(connectReqPackage.getBodyLen());
        System.out.println(connectReqPackage.getBody().readableBytes());
//        System.out.println(connectReqPackage.getTotalLen());
//        System.out.println(connectReqPackage.getTotalLen());
//        System.out.println(connectReqPackage.getTotalLen());

    }
}
