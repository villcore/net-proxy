package com.villcore.encrypt;

import com.villcore.net.proxy.bio.compressor.GZipCompressor;
import com.villcore.net.proxy.bio.crypt.Crypt;
import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.handler.*;
import com.villcore.net.proxy.bio.pkg.DefaultPackage;
import com.villcore.net.proxy.bio.pkg.EncryptPackage;
import com.villcore.net.proxy.bio.pkg.PkgConf;
import org.junit.Test;
import sun.plugin2.message.OverlayWindowMoveMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptTest {
    private static final String MSG = "这是一行简单内容";

    @Test
    public void testCryptHeader() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        CryptHelper cryptHelper = new CryptHelper();
        PasswordManager passwordManager = new PasswordManager();

        Handler compressHandler = new CompressHandler(new GZipCompressor());
        Handler decompressHandler = new DecompressHandler(new GZipCompressor());

        Handler encryptHandler = new EncryptHandler(passwordManager, cryptHelper);
        Handler decryptHandler = new DecryptHandler(passwordManager, cryptHelper);

        DefaultPackage pkg = new DefaultPackage();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] msgBytes = MSG.getBytes();
        dos.writeInt(msgBytes.length);
        dos.writeInt(0);
        dos.writeLong(1L);
        dos.writeInt(0);
        pkg.setHeader(bos.toByteArray());
        dos.close();
        pkg.setBody(msgBytes);
        pkg.setSize(pkg.getHeader(), msgBytes.length);

        EncryptPackage encryptPackage = (EncryptPackage) encryptHandler.handle(compressHandler.handle(pkg));
        DefaultPackage decrptPackage = (DefaultPackage) decryptHandler.handle(encryptPackage);

        decrptPackage = (DefaultPackage) decompressHandler.handle(decrptPackage);

        System.out.println(new String(decrptPackage.getBody()));
    }


    @Test
    public void testCryptMethod() throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        CryptHelper cryptHelper = new CryptHelper();
        PasswordManager passwordManager = new PasswordManager();
        byte[] ivBytes = cryptHelper.getSecureRandomBytes(PkgConf.getIvBytesLen());

        SecretKey key = cryptHelper.getSecretKey("123123");

        byte[] bytes = cryptHelper.encryptBody(ivBytes, key, MSG.getBytes("utf-8"));
        System.out.println(bytes.length);
        byte[] decryptBytes = cryptHelper.decryptBody(ivBytes, key, bytes);

        System.out.println(new String(decryptBytes, "utf-8"));
    }
}
