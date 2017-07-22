package com.villcore.net.proxy.bio.handler;


import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.bio.crypt.PasswordManager;
import com.villcore.net.proxy.bio.pkg.EncryptPackage;
import com.villcore.net.proxy.bio.pkg.Package;
import com.villcore.net.proxy.bio.pkg.PkgConf;
import com.villcore.net.proxy.bio.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.plugin2.message.OverlayWindowMoveMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EncryptHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(EncryptHandler.class);

    private PasswordManager passwordManager;
    private CryptHelper cryptHelper;

    public EncryptHandler(PasswordManager passwordManager, CryptHelper cryptHelper) {
        this.passwordManager = passwordManager;
        this.cryptHelper = cryptHelper;
    }

    @Override
    public Package handle(Package pkg) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        // DefaultPackage -> EncryptPackage
        //size/header size/body size/    (有效的内容/header iv, size(body), conn, userFlag, request body)  /干扰数据
        byte[] header = pkg.getHeader();
        byte[] body = pkg.getBody();
        long userFlag = pkg.getUserFlag(header);

        String password = passwordManager.getPassword(userFlag);
        SecretKey key = cryptHelper.getSecretKey(password);

        //生成iv
        byte[] ivBytes = cryptHelper.getSecureRandomBytes(PkgConf.getIvBytesLen());
        //LOG.debug("encrypt ivBytes = {}", ivBytes);

        //加密header
        byte[] encryptHeader = cryptHelper.encryptHeader(ivBytes, header);

        //加密body
        byte[] encryptBody = cryptHelper.encryptBody(ivBytes, key, body);
//        LOG.debug("encrypt encryptBody = {}", encryptBody);

        byte[] decryptBody = cryptHelper.decryptBody(ivBytes, key, encryptBody);

//        LOG.debug(" encrypted body size = {}\n origin content = {}", encryptBody.length, new String(decryptBody));
        //有干扰字节
        byte[] interferenceBytes = cryptHelper.getInterferenceBytes(body.length);

        // [(bodySize + 干扰数据Size) / (bodySize) /（userFlag) / ivBytes] [ body (加密内容（defaultPackage))] [(干扰数据)]
        ByteBuffer encryptPkgHeaderBuffer = ByteBuffer.wrap(new byte[PkgConf.getEndryptPackageHeaderLen()]);
        encryptPkgHeaderBuffer.putInt(encryptHeader.length + encryptBody.length + interferenceBytes.length);
        encryptPkgHeaderBuffer.putInt(encryptHeader.length + encryptBody.length);
        encryptPkgHeaderBuffer.putLong(userFlag);
        encryptPkgHeaderBuffer.put(ivBytes);

        byte[] newBody = new byte[encryptHeader.length + encryptBody.length + interferenceBytes.length];
        ByteArrayUtils.cpyToNew(encryptHeader, newBody, 0, 0, encryptHeader.length);
        ByteArrayUtils.cpyToNew(encryptBody, newBody, 0, encryptHeader.length, encryptBody.length);
        ByteArrayUtils.cpyToNew(interferenceBytes, newBody, 0, encryptHeader.length + encryptBody.length, interferenceBytes.length);

        EncryptPackage encryptPackage = new EncryptPackage();
        encryptPackage.setHeader(encryptPkgHeaderBuffer.array());
        encryptPackage.setBody(newBody);
        encryptPackage.setSize(encryptPackage.getHeader(), newBody.length);

        return encryptPackage;
    }
}
