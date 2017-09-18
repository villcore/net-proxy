package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.Package;

import java.util.List;

/**
 * 发送服务接口
 */
public interface Writeable {
    public boolean canWrite();

    public boolean write(Package pkg);

    public void touch(Package pkg);

    public void failWrite(Package pkg);

    public List<Package> getWritePackages();

    public void write();
}
