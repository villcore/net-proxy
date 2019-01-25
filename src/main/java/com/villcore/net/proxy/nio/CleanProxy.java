package com.villcore.net.proxy.nio;

import com.villcore.net.proxy.sys.WinSystemProxy;

import java.io.IOException;

/**
 * Created by Administrator on 2017/7/10.
 */
public class CleanProxy {
    public static void main(String[] args) throws IOException {
        WinSystemProxy proxy = new WinSystemProxy("win_utils");
        proxy.clearProxy();
    }
}
