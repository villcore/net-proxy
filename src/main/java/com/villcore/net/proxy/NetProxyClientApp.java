package com.villcore.net.proxy;

import com.villcore.net.proxy.client.NetProxyClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.nio.channels.SelectionKey;

@SpringBootApplication
@ComponentScan(basePackages = "com.villcore.net.proxy.web")
public class NetProxyClientApp {
    public static void main(String[] args) {
        System.out.println(SelectionKey.OP_READ);
        SpringApplication.run(NetProxyClientApp.class, args);
        NetProxyClient.run(args);
    }
}
