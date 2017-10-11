package com.villcore;

import com.villcore.net.proxy.v3.pkg.v1.Package;
import com.villcore.net.proxy.v3.pkg.v1.PackageUtils;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class DataPackageTest {
    @Test
    public void testDataPkg() {
        String testStr = "hello this is a test ...";
        Package pkg = PackageUtils.buildDataPackage(0, 0, -1L, Unpooled.wrappedBuffer(testStr.getBytes()));
        System.out.println(new String(pkg.getBody().array()));
    }
}
