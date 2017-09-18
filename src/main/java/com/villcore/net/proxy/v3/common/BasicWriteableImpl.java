package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.Package;

import java.util.List;

/**
 * 发送服务接口
 */
public abstract class BasicWriteableImpl implements Writeable {
    public void write() {
        List<Package> writePackages = getWritePackages();
        for(Package pkg : writePackages) {
            if(canWrite()) {
                if(!write(pkg)) {
                    failWrite(pkg);
                    continue;
                }
                touch(pkg);
            }
        }
    }
}
