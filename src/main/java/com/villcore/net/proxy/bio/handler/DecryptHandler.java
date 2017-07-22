package com.villcore.net.proxy.bio.handler;

import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.pkg.DefaultPackage;
import com.villcore.net.proxy.bio.pkg.EncryptPackage;
import com.villcore.net.proxy.bio.pkg.Package;
import com.villcore.net.proxy.bio.pkg.PkgConf;
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
        // EncryptPackage -> DefaultPackage
        // [(bodySize + 干扰数据Size) / (bodySize) /（userFlag) / ivBytes] [ body (加密内容（defaultPackage))] [(干扰数据)]
//        if(!(pkg instanceof EncryptPackage)) {
//            throw new IOException("package format error...");
//        }

        EncryptPackage encryptPackage = new EncryptPackage();
        encryptPackage.setHeader(pkg.getHeader());
        encryptPackage.setBody(pkg.getBody());

//        LOG.debug("pkg header len = {}", pkg.getHeader().length);
//        LOG.debug("pkg body len = {}", pkg.getBody().length);

        byte[] header = encryptPackage.getHeader();
        //LOG.debug("header len = {}", header.length);
        byte[] bodyWithInterfenrence = encryptPackage.getBody();
        //LOG.debug("bodyWithInterfenrence len = {}", ByteBuffer.wrap(pkg.getHeader()).getInt());

        long userFlag = encryptPackage.getUserFlag(header);
        byte[] ivBytes = encryptPackage.getIvBytes(header);
        //LOG.debug("decrypt ivBytes = {}", ivBytes);
        int bodySize = encryptPackage.getBodySize(header);

        //LOG.debug("body size = {}", bodySize);
        byte[] bodyBytes = new byte[bodySize];
        //LOG.debug("body = {}", new String(bodyBytes));

        ByteArrayUtils.cpyToNew(bodyWithInterfenrence, bodyBytes, 0, 0, bodyBytes.length);

        String password = passwordManager.getPassword(userFlag);
        SecretKey key = cryptHelper.getSecretKey(password);

        byte[] encryptHeader = new byte[PkgConf.getDefaultPackageHeaderLen()];
        byte[] encryptBody = new byte[bodyBytes.length - encryptHeader.length];


        ByteArrayUtils.cpyToNew(bodyBytes, encryptHeader, 0, 0, encryptHeader.length);
        ByteArrayUtils.cpyToNew(bodyBytes, encryptBody, encryptHeader.length, 0, encryptBody.length);
        //LOG.debug("decrypt encryptBody = {}", encryptBody);



        //解密header
        byte[] decryptHeader = cryptHelper.decryptHeader(ivBytes, encryptHeader);

        //解密body
        byte[] decryptBody = cryptHelper.decryptBody(ivBytes,  key, encryptBody);

        //LOG.debug("decrypted header len = {}, decrypt body len = {}", decryptHeader.length, decryptBody.length);
        //LOG.debug("decrypted body content = {}", new String(decryptBody));


        DefaultPackage defaultPackage = new DefaultPackage();
        defaultPackage.setHeader(decryptHeader);
        defaultPackage.setBody(decryptBody);
        defaultPackage.setSize(decryptHeader, decryptBody.length);
        defaultPackage.setUserFlag(decryptHeader, userFlag);

        return defaultPackage;
    }
}
