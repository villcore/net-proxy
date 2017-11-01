package com.villcore.net.proxy.conf;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class Config {
    protected Path propPath;

    protected Configuration configuration;

    protected void loadProp() throws FileNotFoundException, ConfigurationException {
        File propFile = propPath.toFile();
        if(propPath == null || !propFile.exists()) {
            throw new FileNotFoundException(String.format("prop file [%s] not exist ...", propFile.getAbsolutePath()));
        }
        configuration = new PropertiesConfiguration(propFile);
    }
}
