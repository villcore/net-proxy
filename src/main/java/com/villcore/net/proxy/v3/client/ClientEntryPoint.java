package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.conf.ClientConfig;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class ClientEntryPoint {
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
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
