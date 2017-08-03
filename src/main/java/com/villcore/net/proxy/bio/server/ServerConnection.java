package com.villcore.net.proxy.bio.server;


import com.villcore.net.proxy.bio.common.DecryptTask;
import com.villcore.net.proxy.bio.common.EncryptTask;
import com.villcore.net.proxy.bio.common.*;
import com.villcore.net.proxy.bio.compressor.GZipCompressor;
import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.handler.*;

import javax.crypto.NoSuchPaddingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/7/17.
 */
public class ServerConnection extends Connection {
    public ServerConnection(Socket readSocket, Socket writeSocket) {
        super(readSocket, writeSocket);
    }

    @Override
    public void initTask(InputStream inputStream, OutputStream outputStream, InputStream inputStream2, OutputStream outputStream2, EncryptTask encryptTask, DecryptTask decryptTask, Connection connection) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Handler encryptHander = new EncryptHandler(new PasswordManager(), new CryptHelper());
        Handler decryptHander = new DecryptHandler(new PasswordManager(), new CryptHelper());

        //input output server decrypt           input2 output2 proxy encrypt
        super.decryptTask = new DecryptTask(this, super.inputStream, super.outputStream2);
        super.decryptTask.addHandler("decompress", new DecompressHandler(new GZipCompressor()));
        super.decryptTask.addHandler("decrypt", decryptHander);
        super.decryptTask.addHandler("user_to_default", new FromUserPackageHandler());


        //TODO add handlers
        super.encryptTask = new EncryptTask(this, super.inputStream2, super.outputStream);
        super.encryptTask.addHandler("pack_user", new ToUserPackageHandler(-1, 1001L));
        super.encryptTask.addHandler("encrypt", encryptHander);
        super.encryptTask.addHandler("compress", new CompressHandler(new GZipCompressor()));
    }

}
