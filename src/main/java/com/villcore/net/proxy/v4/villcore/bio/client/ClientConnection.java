package com.villcore.net.proxy.v4.villcore.bio.client;


import com.villcore.net.proxy.v4.villcore.bio.common.BytesToPackageTask;
import com.villcore.net.proxy.v4.villcore.bio.common.Connection;
import com.villcore.net.proxy.v4.villcore.bio.common.PackageToBytesTask;
import com.villcore.net.proxy.v4.villcore.bio.handler.DecryptHandler;
import com.villcore.net.proxy.v4.villcore.bio.handler.EncryptHandler;
import com.villcore.net.proxy.v4.villcore.bio.handler.Handler;
import com.villcore.net.proxy.v4.villcore.crypt.Crypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

/**
 * Created by villcore on 2017/7/17.
 */
public class ClientConnection extends Connection {
    private static final Logger LOG = LoggerFactory.getLogger(ClientConnection.class);

    private String password;

    public ClientConnection(Socket readSocket, Socket writeSocket, String password) {
        super(readSocket, writeSocket);
        this.password = password;
    }

    @Override
    public void initTask(InputStream inputStream, OutputStream outputStream, InputStream inputStream2, OutputStream outputStream2, BytesToPackageTask bytesToPackageTask, PackageToBytesTask packageToBytesTask, Connection connection) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Crypt crypt = new Crypt();
        byte[] key = crypt.generateKey("villcore");
        byte[] iv = crypt.generateIv();

        crypt.setIv(iv);
        crypt.setKey(key);
        crypt.initDecrypt();
        crypt.initEncrypt();

        Handler encryptHander = new EncryptHandler(crypt);
        Handler decryptHander = new DecryptHandler(crypt);

        super.bytesToPackageTask = new BytesToPackageTask(connection, inputStream, outputStream2);
        super.bytesToPackageTask.addHandler("encrypt", encryptHander);

        super.packageToBytesTask = new PackageToBytesTask(connection, inputStream2, outputStream);
        super.packageToBytesTask.addHandler("decrypt", decryptHander);
    }
}
