package com.villcore.net.proxy.bio.common;

import com.villcore.net.proxy.bio.pkg2.DefaultPackage;
import com.villcore.net.proxy.bio.handler.Handler;
import com.villcore.net.proxy.bio.pkg2.EncryptPackage;
import com.villcore.net.proxy.bio.pkg2.Package;
import com.villcore.net.proxy.bio.pkg2.TransferPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.*;

/**
 * Created by villcore on 2017/7/18.
 */
public class DecryptTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(DecryptTask.class);

    private volatile boolean running = false;
    private Map<String, Handler> handlers = new LinkedHashMap<>();

    private Connection connection;
    private InputStream inputStream;
    private OutputStream outputStream;

    public DecryptTask(Connection connection, InputStream inputStream, OutputStream outputStream) {
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
                pkg.readPackageWithHeader(inputStream);
                LOG.debug("decrypt task read pkg size = {}, header len = {}, body len = {}", pkg.getSize(), pkg.getHeaderLen(), pkg.getBodyLen());
                for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
                    pkg = entry.getValue().handle(pkg);
                    LOG.debug("decrypt [{}] handle package size = {}, header = {}, body = {}", new Object[]{entry.getKey(), pkg.getSize(), pkg.getHeaderLen(), pkg.getBodyLen()});
                }

                pkg.writePackageWithoutHeader(outputStream);
                LOG.debug("write to decrypting =  \n{}\n", new String(pkg.getBody()));
                LOG.debug("decryt task write pkg...");
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
//        byte[] bytes = new byte[1 * 1024 * 1024];
//        int pos = -1;
        while (running) {
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
                //LOG.debug("decryt task running...");
                Package pkg = new EncryptPackage();
                //Package pkg = new DefaultPackage();
                //Package pkg = new TransferPackage();
                //pkg.LOG = LOG;

                pkg.readPackageWithHeader(inputStream);
                LOG.debug("decrypt task read pkg header len = {}, body len = {}", pkg.getHeader().length, pkg.getBody().length);
                //byte[] bytes = new byte[20];
                //LOG.debug("decrypt read bytes = {}", inputStream.read(bytes));
                //LOG.debug("decryt read pkg...");

                for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
                    pkg = entry.getValue().handle(pkg);
                }

                //TransferPackage.wrap(pkg).writePackageWithHeader(outputStream);
                pkg.writePackageWithoutHeader(outputStream);
//                outputStream.write(pkg.getBody());
//                outputStream.flush();
                LOG.debug("decryt task write pkg...");
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
