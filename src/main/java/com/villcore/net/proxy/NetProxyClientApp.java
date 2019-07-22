package com.villcore.net.proxy;

import com.villcore.net.proxy.client.NetProxyClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.villcore.net.proxy.web")
public class NetProxyClientApp {
    public static void main(String[] args) {
        SpringApplication.run(NetProxyClientApp.class, args);
        NetProxyClient.run(args);
    }
}
