//package com.villcore.net.proxy.v3.common.handlers;
//
//
//import com.villcore.net.proxy.bio.crypt.CryptHelper;
//import com.villcore.net.proxy.bio.pkg2.PkgConf;
//import com.villcore.net.proxy.bio.util.ByteArrayUtils;
//import com.villcore.net.proxy.v3.common.Connection;
//import com.villcore.net.proxy.v3.common.PackageHandler;
//import com.villcore.net.proxy.v3.pkg.v2.Package;
//import com.villcore.net.proxy.v3.pkg.v2.PackageUtils;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.crypto.BadPaddingException;
//import javax.crypto.IllegalBlockSizeException;
//import javax.crypto.SecretKey;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.InvalidKeyException;
//import java.util.LinkedList;
//import java.util.List;
//
//public class EncryptHandler implements PackageHandler {
//    private static final Logger LOG = LoggerFactory.getLogger(EncryptHandler.class);
//
//    private CryptHelper cryptHelper;
//    private String username;
//    private String password;
//
//    public EncryptHandler(CryptHelper cryptHelper, String username, String password) {
//        this.cryptHelper = cryptHelper;
//        this.username = username;
//        this.password = password;
//    }
//
//    @Override
//    public List<Package> handlePackage(List<Package> packages, Connection connection) {
//        List<Package> encryptPackages = new LinkedList<>();
//
//        for (Package pkg : packages) {
//            try {
//                byte[] header = pkg.getHeader();
//                byte[] body = pkg.getBody();
//
//                SecretKey key = cryptHelper.getSecretKey(password);
//
//                //生成iv
//                byte[] ivBytes = cryptHelper.getSecureRandomBytes(16);
//
//                //加密header
//                byte[] encryptHeader = cryptHelper.encryptHeader(ivBytes, header);
//
//                //加密body
//                byte[] encryptBody = cryptHelper.encryptBody(ivBytes, key, body);
//
//            } catch (Exception e) {
//                LOG.error(e.getMessage(), e);
//            }
//        }
//    }
//}
