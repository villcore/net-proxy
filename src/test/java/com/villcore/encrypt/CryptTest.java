package com.villcore.encrypt;

import com.villcore.net.proxy.bio.compressor.GZipCompressor;
import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.handler.*;
import com.villcore.net.proxy.bio.pkg2.Package;
import com.villcore.net.proxy.bio.pkg2.EncryptPackage;
import com.villcore.net.proxy.bio.pkg2.PkgConf;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

        Package pkg = new Package();

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

        EncryptPackage encryptPackage = (EncryptPackage) encryptHandler.handle(pkg);
        Package pkg2 = decryptHandler.handle(encryptPackage);

        System.out.println(new String(pkg2.getBody()));
    }


    @Test
    public void testCryptMethod() throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        CryptHelper cryptHelper = new CryptHelper();
        PasswordManager passwordManager = new PasswordManager();
        byte[] ivBytes = cryptHelper.getSecureRandomBytes(PkgConf.getIvBytesLen());

        SecretKey key = cryptHelper.getSecretKey("123123");

        byte[] bytes = cryptHelper.encryptBody(ivBytes, key, MSG.getBytes("utf-8"));
        System.out.println(MSG.getBytes("utf-8").length);
        System.out.println(bytes.length);
        byte[] decryptBytes = cryptHelper.decryptBody(ivBytes, key, bytes);

        System.out.println(new String(decryptBytes, "utf-8"));
    }
}
