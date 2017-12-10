package com.villcore.net.proxy.v4.villcore.bio.handler;

import com.villcore.net.proxy.v4.villcore.bio.pkg2.Package;

/**
 * Created by villcore on 2017/7/17.
 */
public interface Handler {
    public Package handle(Package pkg) throws Exception;
}
