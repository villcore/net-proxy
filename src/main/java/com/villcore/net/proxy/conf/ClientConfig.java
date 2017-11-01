package com.villcore.net.proxy.conf;

import org.apache.commons.configuration.ConfigurationException;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientConfig extends Config {
    public void setPropPath(Path propPath) {
        this.propPath = propPath;
        try {
            loadProp();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }


    public String getProxyPort() {
        return configuration.getString("proxy_port", "10080");
    }

    public String getRemoteAddr() {
        return configuration.getString("remote_addr", "");
    }

    public String getRemotePort() {
        return configuration.getString("remote_port", "");
    }

    public String getUsername() {
        return configuration.getString("username", "villcore");
    }

    public String getPassword() {
        return configuration.getString("password", "123123");
    }
}
