package com.villcore.net.proxy.v3.common;

import com.sun.xml.internal.ws.handler.HandlerException;
import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * 发送服务接口
 */
public abstract class BasicWriteableImpl implements Writeable {
    public void write() {
        List<Package> writePackages = getWritePackages();
        for(Package pkg : writePackages) {
            if(canWrite()) {
                ByteBuf header = pkg.getHeader().copy();
                ByteBuf body = pkg.getBody().copy();
                Package pkg2 = new Package();
                pkg2.setHeader(header);
                pkg2.setBody(body);
                if(!write(pkg)) {
                    failWrite(pkg);
                    continue;
                }
                touch(pkg2);
            }
        }
    }
}
