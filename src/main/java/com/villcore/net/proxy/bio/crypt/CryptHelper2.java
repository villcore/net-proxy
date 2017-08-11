package com.villcore.net.proxy.bio.crypt;

import com.villcore.net.proxy.bio.pkg.PkgConf;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CryptHelper2 {

    public static final String CHARSET = "utf-8";

    private SecureRandom secureRandom;
    private Cipher cipher;
    private KeyGenerator keyGenerator;
    private int ivSize;
    private float interferenceFactor;
    private String password;

    public CryptHelper2(String password) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.password = password;
        this.interferenceFactor = PkgConf.getInterferenceFactor();
        secureRandom = new SecureRandom(password.getBytes());

        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128, secureRandom);
            cipher = Cipher.getInstance("AES/CFB/NoPadding"); // Advanced Encryption Standard - Cipher Feedback Mode - No Padding


        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {

            throw new RuntimeException(ex);

        }
    }

    public byte[] getSecureRandomBytes(int size) {
        return secureRandom.generateSeed(size);
    }

    public SecretKey getSecretKey(String password) throws UnsupportedEncodingException {
        String stringKey = DigestUtils.md5Hex(password.getBytes(CHARSET));
        byte[] bytes = Base64.decodeBase64(stringKey);
        return new SecretKeySpec(bytes, 0, bytes.length, "AES");
    }

    public SecretKey getSecretKey(byte[] bytes) {
        return new SecretKeySpec(bytes, 0, bytes.length, "AES");
    }

    public byte[] getInterferenceBytes(int size) {
        int inferenceSize = (int) (size * interferenceFactor);
        return getSecureRandomBytes(inferenceSize);
    }

    public byte[] encryptHeader(byte[] iv, byte[] header) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec IVSpec = new IvParameterSpec(iv);
        SecretKey key = getSecretKey(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, IVSpec);
        byte[] encryptBytes = cipher.doFinal(header);
        return encryptBytes;
    }

    public byte[] decryptHeader(byte[] iv, byte[] header) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec IVSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(iv), IVSpec);
        return cipher.doFinal(header);
    }

    public byte[] encryptBody(byte[] iv, SecretKey key, byte[] body) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //byte[] iv = getSecureRandomBytes(PkgConf.getIvBytesLen());
        IvParameterSpec IVSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, IVSpec);
        byte[] encryptBytes = cipher.doFinal(body);
        return encryptBytes;
    }

    public byte[] decryptBody(byte[] iv, SecretKey key, byte[] body) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //byte[] iv = getSecureRandomBytes(PkgConf.getIvBytesLen());
        IvParameterSpec IVSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, IVSpec);
        byte[] encryptBytes = cipher.doFinal(body);
        return encryptBytes;
    }
}
