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
        return configuration.getString("proxy_port", "50082");
    }

    public String getRemoteAddr() {
        return configuration.getString("remote_addr", "12");
    }

    public String getRemotePort() {
        return configuration.getString("remote_port", "20081");
    }

    public String getUsername() {
        return configuration.getString("username", "villcore2");
    }

    public String getPassword() {
        return configuration.getString("password", "villcore2");
    }
}
