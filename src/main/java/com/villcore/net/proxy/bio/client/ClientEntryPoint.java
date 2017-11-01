package com.villcore.net.proxy.bio.client;

import com.villcore.net.proxy.conf.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(ClientEntryPoint.class);

    public static void main(String[] args) {
        Path propPath = Paths.get("conf","client.conf");

        if(args.length != 1) {
            System.err.println("please set config file path as argument ...");
        } else {
            propPath = Paths.get(args[0]);
        }

        if(!propPath.toFile().exists()) {
            System.err.println("config file not exist ...");
            System.exit(0);
        }

        ClientConfig config = new ClientConfig();
        config.setPropPath(propPath);

        try {
            Client.start(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
