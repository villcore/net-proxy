package com.villcore.encrypt;

import com.villcore.net.proxy.bio.compressor.GZipCompressor;
import com.villcore.net.proxy.bio.crypt.Crypt;
import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.handler.*;
import com.villcore.net.proxy.bio.pkg2.Package;
import com.villcore.net.proxy.bio.pkg2.DefaultPackage;
import com.villcore.net.proxy.bio.pkg2.EncryptPackage;
import com.villcore.net.proxy.bio.pkg2.PkgConf;
import org.junit.Test;
import sun.plugin2.message.OverlayWindowMoveMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptTest {
    private static String MSG = "这是一行简单内容";


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


        for(byte b : encryptPackage.getHeader()) {
           System.out.print(b+",");
        }
        System.out.println("");
        for(byte b : encryptPackage.getBody()) {
            System.out.print(b+",");
        }
        System.out.println("");

        Package pkg2 = decryptHandler.handle(encryptPackage);

        System.out.println(new String(pkg2.getBody()));
    }


    @Test
    public void testCryptMethod() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        File file = new File("crypt.txt");
        InputStream is = new FileInputStream("crypt.txt");

        byte[] textBytes = new byte[is.available()];
        is.read(textBytes);

        CryptHelper cryptHelper = new CryptHelper();
        PasswordManager passwordManager = new PasswordManager();
        byte[] ivBytes = cryptHelper.getSecureRandomBytes(PkgConf.getIvBytesLen());

        SecretKey key = cryptHelper.getSecretKey("123123");

        byte[] bytes = cryptHelper.encryptBody(ivBytes, key, textBytes /*MSG.getBytes("utf-8")*/);
        System.out.println(bytes.length);
        byte[] decryptBytes = cryptHelper.decryptBody(ivBytes, key, bytes);

        System.out.println(new String(decryptBytes, "utf-8"));
    }

    @Test
    public void testWinToLinux() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        CryptHelper cryptHelper = new CryptHelper();
        PasswordManager passwordManager = new PasswordManager();

        Handler decryptHandler = new DecryptHandler(passwordManager, cryptHelper);

        String headerBytesStr = "0,0,0,20,0,0,0,24,0,0,0,0,0,0,0,0,90,39,110,-82,-58,101,45,-26,-99,4,23,75,9,-54,-34,42";
        String bodyBytesStr = "-106,-84,62,39,61,-107,-14,-29,-94,-79,-10,-108,67,57,-32,-62,92,96,-95,37,-26,-11,55,-112,-94,81,-55,-86,15,35,112,-15,-5,-42,79,59,83,78,53,-109,53,10,-116,-20";

        String[] headerByteStr = headerBytesStr.split(",");
        byte[] header = new byte[headerByteStr.length];
        for(int i = 0; i < header.length; i++) {
            header[i] = Byte.valueOf(headerByteStr[i]);
        }


        String[] bodyByteStr = bodyBytesStr.split(",");
        byte[] body = new byte[bodyByteStr.length];
        for(int i = 0; i < body.length; i++) {
            body[i] = Byte.valueOf(bodyByteStr[i]);
        }

        EncryptPackage encryptPackage = new EncryptPackage();
        encryptPackage.setHeader(header);
        encryptPackage.setBody(body);
        System.out.println(encryptPackage.getIvBytes().length);
        System.out.println(encryptPackage.getBodySize());
        System.out.println(encryptPackage.getHeaderSize());
        System.out.println(encryptPackage.getUserFlag());
        System.out.println(encryptPackage.getBodyLen());


        Package pkg2 = decryptHandler.handle(encryptPackage);

        System.out.println(new String(pkg2.getBody()));
    }
}
