package com.villcore.net.proxy.bio.client;

import com.villcore.net.proxy.bio.common.DecryptTask;
import com.villcore.net.proxy.bio.common.EncryptTask;
import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.handler.*;
import com.villcore.net.proxy.bio.common.Connection;
import com.villcore.net.proxy.bio.compressor.GZipCompressor;
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

    public ClientConnection(Socket readSocket, Socket writeSocket) {
        super(readSocket, writeSocket);
    }

    @Override
    public void initTask(InputStream inputStream, OutputStream outputStream, InputStream inputStream2, OutputStream outputStream2, EncryptTask encryptTask, DecryptTask decryptTask, Connection connection) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Handler encryptHander = new EncryptHandler(new PasswordManager(), new CryptHelper());
        Handler decryptHander = new DecryptHandler(new PasswordManager(), new CryptHelper());

        LOG.debug("init task...");
        //input output local encrypt        input2 output2 remote decrypt
        super.encryptTask = new EncryptTask(connection, inputStream, outputStream2);

       super.encryptTask.addHandler("compress", new CompressHandler(new GZipCompressor()));
       super.encryptTask.addHandler("encrypt", encryptHander);

        super.decryptTask = new DecryptTask(connection, inputStream2, outputStream);
        super.decryptTask.addHandler("decrypt", decryptHander);

        super.decryptTask.addHandler("decompress", new DecompressHandler(new GZipCompressor()));

    }
}
