package com.villcore.net.proxy.bio.handler;

import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.pkg2.DefaultPackage;
import com.villcore.net.proxy.bio.pkg2.EncryptPackage;
import com.villcore.net.proxy.bio.pkg2.Package;
import com.villcore.net.proxy.bio.pkg2.PkgConf;
import com.villcore.net.proxy.bio.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Arrays;

public class DecryptHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(DecryptHandler.class);

    private PasswordManager passwordManager;
    private CryptHelper cryptHelper;

    public DecryptHandler(PasswordManager passwordManager, CryptHelper cryptHelper) {
        this.passwordManager = passwordManager;
        this.cryptHelper = cryptHelper;
    }

    @Override
    public Package handle(Package pkg) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        // EncryptPackage -> UserPackage

        EncryptPackage encryptPackage = new EncryptPackage();
        encryptPackage.setHeader(pkg.getHeader());
        encryptPackage.setBody(pkg.getBody());

//        LOG.debug("pkg header len = {}", pkg.getHeader().length);
//        LOG.debug("pkg body len = {}", pkg.getBody().length);

        byte[] header = encryptPackage.getHeader();
        //LOG.debug("header len = {}", header.length);
        byte[] bodyWithInterfenrence = encryptPackage.getBody();
        //LOG.debug("bodyWithInterfenrence len = {}", ByteBuffer.wrap(pkg.getHeader()).getInt());

        int innerHeaderSize = encryptPackage.getHeaderSize();
        int innerBodySize = encryptPackage.getBodySize();
        long userFlag = encryptPackage.getUserFlag();
        byte[] ivBytes = encryptPackage.getIvBytes();
        //LOG.debug("decrypt ivBytes = {}", ivBytes);

        //LOG.debug("body size = {}", bodySize);

        byte[] encryptHeader = new byte[innerHeaderSize];
        byte[] encryptBody = new byte[innerBodySize];
        //LOG.debug("body = {}", new String(bodyBytes));

        ByteArrayUtils.cpyToNew(bodyWithInterfenrence, encryptHeader, 0, 0, encryptHeader.length);
        ByteArrayUtils.cpyToNew(bodyWithInterfenrence, encryptBody, encryptHeader.length, 0, encryptBody.length);

        String password = passwordManager.getPassword(userFlag);
        SecretKey key = cryptHelper.getSecretKey(password);

        //LOG.debug("decrypt encryptBody = {}", encryptBody);



        //解密header
        byte[] decryptHeader = cryptHelper.decryptHeader(ivBytes, encryptHeader);

        //解密body
        byte[] decryptBody = cryptHelper.decryptBody(ivBytes,  key, encryptBody);

        //LOG.debug("decrypted header len = {}, decrypt body len = {}", decryptHeader.length, decryptBody.length);
        //LOG.debug("decrypted body content = {}", new String(decryptBody));

        Package newPkg = new Package();
        newPkg.setHeader(decryptHeader);
        newPkg.setBody(decryptBody);

        return newPkg;
    }
}
