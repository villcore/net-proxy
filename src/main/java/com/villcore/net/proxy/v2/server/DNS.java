package com.villcore.net.proxy.v2.server;

import io.netty.resolver.InetSocketAddressResolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DNS {
    private static final Map<String, String> DOMAIN_MAP = new ConcurrentHashMap<>();

    public static String[] parseIp(String hostname, int port) {
        if(!DOMAIN_MAP.containsKey(hostname)) {
            InetSocketAddress address = new InetSocketAddress(hostname, port);

            if(address.getAddress() == null) {
                return new String[]{hostname, String.valueOf(port)};
            }

            if(address.getAddress().getAddress() == null) {
                return new String[]{hostname, String.valueOf(port)};
            }

            byte[] addrBytes = address.getAddress().getAddress();

            int a = Byte.toUnsignedInt(addrBytes[0]);
            int b = Byte.toUnsignedInt(addrBytes[1]);
            int c = Byte.toUnsignedInt(addrBytes[2]);
            int d = Byte.toUnsignedInt(addrBytes[3]);
            String ip = a + "." + b + "." + c + "." + d;
            DOMAIN_MAP.putIfAbsent(hostname, ip);
        }
        return new String[]{DOMAIN_MAP.get(hostname), String.valueOf(port)};
    }

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        System.out.println(parseIp("www.baidu.com", 80)[0]);
        System.out.println("first use time = " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        System.out.println(parseIp("www.baidu.com", 80)[0]);
        System.out.println("second use time = " + (System.currentTimeMillis() - time));
    }
}
