package com.villcore.net.proxy.bio.common;

import com.villcore.net.proxy.bio.common.Connection;
import com.villcore.net.proxy.bio.common.DecryptTask;
import com.villcore.net.proxy.bio.pkg2.DefaultPackage;
import com.villcore.net.proxy.bio.handler.Handler;
import com.villcore.net.proxy.bio.pkg2.Package;
import com.villcore.net.proxy.bio.util.HttpParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.swing.PrintingStatus;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.sound.midi.SoundbankResource;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/18.
 */
public class EncryptTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(DecryptTask.class);

    private volatile boolean running = false;
    private Map<String, Handler> handlers = new LinkedHashMap<>();

    private Connection connection;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean fisrtLineFinish = false;
    public EncryptTask(Connection connection, InputStream inputStream, OutputStream outputStream) {
        this.connection = connection;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void addHandler(String name, Handler handler) {
        handlers.put(name, handler);
    }

    public void removeHandler(String name) {
        handlers.remove(name);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Package pkg = new Package();
                pkg.readPackageWithoutHeader(inputStream);

                //LOG.debug("encryt read pkg...");
                //LOG.debug("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
                //LOG.debug("read to encrypting request = >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n{}\n", new String(pkg.getBody()));
                //LOG.debug("origin size = {}, header = {}, body = {}", pkg.getSize(), pkg.getHeaderLen(), pkg.getBodyLen());
                for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
                    pkg = entry.getValue().handle(pkg);
                    LOG.debug("encrypt [{}] handle package size = {}, header = {}, body = {}", new Object[]{entry.getKey(), pkg.getSize(), pkg.getHeaderLen(), pkg.getBodyLen()});
                }
                pkg.writePackageWithHeader(outputStream);
                //LOG.debug("encryt write pkg ...");

            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                stop();
            } catch (BadPaddingException e) {
                LOG.error(e.getMessage(), e);
                stop();
            } catch (InvalidAlgorithmParameterException e) {
                LOG.error(e.getMessage(), e);
                stop();
            } catch (IllegalBlockSizeException e) {
                LOG.error(e.getMessage(), e);
                stop();
            } catch (InvalidKeyException e) {
                LOG.error(e.getMessage(), e);
                stop();
            }
        }
        close();
    }

    /*
    @Override
    public void run() {
        while (running) {
//            byte[] bytes = new byte[1 * 1024 * 1024];
//            int pos = -1;
            try {
//                pos = inputStream.read(bytes);
//                if(pos > 0) {
//                    outputStream.write(bytes, 0, pos);
//                    outputStream.flush();
//                }
//
//                if(pos == -1) {
//                    throw new IOException("socket closed...");
//                }
                ////LOG.debug("encrypt task runing...");
                Package pkg = new DefaultPackage();
                //pkg.LOG = LOG;
                pkg.readPackageWithoutHeader(inputStream);
                LOG.debug("encryt read pkg...");

                LOG.debug("origin size = {}, header = {}, body = {}", pkg.getSize(pkg.getHeader()), pkg.getHeader().length, pkg.getBody().length);
                for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
                    pkg = entry.getValue().handle(pkg);
                }
                LOG.debug("compress size = {}, header = {}, body = {}", pkg.getSize(pkg.getHeader()), pkg.getHeader().length, pkg.getBody().length);
//                outputStream.write(pkg.getHeader());
//                outputStream.write(pkg.getBody());
//                outputStream.flush();

                pkg.writePackageWithHeader(outputStream);
                LOG.debug("encryt write pkg...");

            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                stop();
            } catch (BadPaddingException e) {
                LOG.error(e.getMessage(), e);
                stop();
            } catch (InvalidAlgorithmParameterException e) {
                LOG.error(e.getMessage(), e);
                stop();
            } catch (IllegalBlockSizeException e) {
                LOG.error(e.getMessage(), e);
                stop();
            } catch (InvalidKeyException e) {
                LOG.error(e.getMessage(), e);
                stop();
            }
        }
        close();
    }
    */

    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void stop() {
        running = false;
    }

    public void close() {
        handlers.clear();
        connection.close();
    }
}
