package com.villcore.net.proxy.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 配置信息类, 通过启动时加载配置文件,将必要信息加载到内存中
 */
public class Configuration {
    private static Path PROP_PATH;

    private void loadProp() throws FileNotFoundException{
        File propFile = PROP_PATH.toFile();

        if(PROP_PATH == null || !propFile.exists()) {
            //throw new FileNotFoundException("prop file [{}] not exist ...", propFile);
        }
    }
}
