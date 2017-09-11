package com.villcore;

import com.villcore.net.proxy.v2.pkg.ConnectPackage;
import com.villcore.net.proxy.v2.pkg.PackageUtils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class ConnectPackageTest {
    @Test
    public void connectPackageBuildTest() throws UnsupportedEncodingException {
        ConnectPackage connectPackage = PackageUtils.buildConnectPackage("www.baidu.com", Short.valueOf("10090"), 1, 1L);
        System.out.println(connectPackage.getTotalLen());
        System.out.println(connectPackage.toByteBuf().readableBytes());

        //System.out.println(connectPackage.getHeaderLen());
        System.out.println(connectPackage.getHeader().readableBytes());

        //System.out.println(connectPackage.getBodyLen());
        System.out.println(connectPackage.getBody().readableBytes());
//        System.out.println(connectPackage.getTotalLen());
//        System.out.println(connectPackage.getTotalLen());
//        System.out.println(connectPackage.getTotalLen());

    }
}
