package com.villcore.encrypt;

import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.pkg2.PkgConf;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptSend {
    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InterruptedException {
        Socket socket = new Socket("127.0.0.1", 30080);
        File file = new File("crypt.txt");
        InputStream is = new FileInputStream("crypt.txt");

        byte[] textBytes = new byte[is.available()];
        is.read(textBytes);

        CryptHelper cryptHelper = new CryptHelper();
        PasswordManager passwordManager = new PasswordManager();
        byte[] ivBytes = cryptHelper.getSecureRandomBytes(PkgConf.getIvBytesLen());

        SecretKey key = cryptHelper.getSecretKey("123123");

        byte[] bytes = cryptHelper.encryptBody(ivBytes, key, textBytes /*MSG.getBytes("utf-8")*/);

        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();

        System.out.println(bytes.length);

        Thread.sleep(100 * 1000);
//        byte[] decryptBytes = cryptHelper.decryptBody(ivBytes, key, bytes);
//        System.out.println(new String(decryptBytes, "utf-8"));
    }
}
