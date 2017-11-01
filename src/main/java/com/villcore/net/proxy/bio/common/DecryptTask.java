package com.villcore.net.proxy.bio.common;

import com.villcore.net.proxy.bio.handler.Handler;
import com.villcore.net.proxy.bio.pkg2.Package;
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

                for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
                    pkg = entry.getValue().handle(pkg);
                }
                pkg.writePackageWithoutHeader(outputStream);
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
