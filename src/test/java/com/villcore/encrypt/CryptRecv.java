package com.villcore.encrypt;

import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.pkg2.PkgConf;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptRecv {
    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(30080);

        Socket socket = serverSocket.accept();

        byte[] tmp = new byte[1024];
        int pos = -1;
        InputStream is = socket.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while((pos = is.read(tmp)) > 0) {
            bos.write(tmp, 0, pos);
        }

        CryptHelper cryptHelper = new CryptHelper();
        PasswordManager passwordManager = new PasswordManager();
        byte[] ivBytes = cryptHelper.getSecureRandomBytes(PkgConf.getIvBytesLen());

        SecretKey key = cryptHelper.getSecretKey("123123");

        byte[] decryptBytes = cryptHelper.decryptBody(ivBytes, key, bos.toByteArray());
        System.out.println(new String(decryptBytes, "utf-8"));
        Thread.sleep(100 * 1000);
    }
}
