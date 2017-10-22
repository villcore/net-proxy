package com.villcore.net.proxy.v3.common.handlers;


import com.villcore.net.proxy.bio.compressor.Compressor;
import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.pkg.v2.*;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthReqPackage;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthRespPackage;
import com.villcore.net.proxy.v3.server.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransferHandler implements PackageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferHandler.class);

    private static final short DEFAULT_COMPRESS_TYPE = CompressType.COMPRESS_GZIP;

    private CryptHelper cryptHelper;
    private Map<UserInfo, SecretKey> userInfoSecretKeyMap = new ConcurrentHashMap<>();

    private boolean isServer;

    private Compressor compressor;

    public TransferHandler(CryptHelper cryptHelper, Compressor compressor, boolean isServer) {
        LOGGER.debug("transfer handler init ......");
        this.cryptHelper = cryptHelper;
        this.compressor = compressor;
        this.isServer = isServer;
    }

    private SecretKey getSecretKey(UserInfo userInfo) {
        return userInfoSecretKeyMap.get(userInfo);
    }

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        List<Package> newPackages = new LinkedList<>();
        LOGGER.debug("transfer handler handle package ...");
        //先解压, 后解密
        for (Package pkg : packages) {

            if (isServer) {
                if(pkg.getPkgType() == PackageType.PKG_CONNECTION_AUTH_REQ) {
                    newPackages.add(pkg);
                    continue;
                }

                if(pkg.getPkgType() == PackageType.PKG_CONNECTION_AUTH_RESP) {
                    newPackages.add(pkg);
                    continue;
                }

                if(pkg.getPkgType() == PackageType.PKG_CHANNEL_CLOSE) {
                    newPackages.add(pkg);
                    continue;
                }

                try {
                    //Transfer package
                    TransferPackage transferPackage = TransferPackage.class.cast(pkg);
                    byte[] header = transferPackage.getHeader();
                    byte[] body = transferPackage.getBody();
                    byte[] ivBytes = transferPackage.getIvBytes();

                    UserInfo userInfo = connection.getUserInfo();
                    SecretKey key = userInfoSecretKeyMap.compute(userInfo, (u, s) -> {
                        try {
                            return s == null ? cryptHelper.getSecretKey(u.getPassword()) : s;
                        } catch (UnsupportedEncodingException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        return null;
                    });

                    if(key == null) {
                        continue;
                    }

                    LOGGER.debug("header len = {}, body len = {}, iv bytes len = {}", header.length, body.length, ivBytes.length);

                    byte[] encryptBytes =  compressor.decompress(body);
                    byte[] data = cryptHelper.decryptBody(ivBytes, key, encryptBytes);

                    Package correctPkg = correctPkg(data, transferPackage.getWrapPackageType());
                    LOGGER.debug("ori data len = {}, pkg type = {}", new Object[]{correctPkg.toBytes().length, correctPkg.getPkgType()});

                    newPackages.add(correctPkg);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {

                if(pkg.getPkgType() == PackageType.PKG_CONNECTION_AUTH_REQ) {
                    newPackages.add(pkg);
                    continue;
                }

                if(pkg.getPkgType() == PackageType.PKG_CONNECTION_AUTH_RESP) {
                    newPackages.add(pkg);
                    continue;
                }

                LOGGER.debug("encrypt pkg ...");
                //client
                //先加密, 再压缩
                try {
                    UserInfo userInfo = connection.getUserInfo();
                    //System.out.println(userInfo);
                    SecretKey key = userInfoSecretKeyMap.compute(userInfo, (u, s) -> {
                        try {
                            return s == null ? cryptHelper.getSecretKey(u.getPassword()) : s;
                        } catch (UnsupportedEncodingException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        return null;
                    });

                    if(key == null) {
                        continue;
                    }

                    byte[] data = pkg.toBytes();
                    short pkgType = pkg.getPkgType();

                    //生成iv
                    byte[] ivBytes = cryptHelper.getSecureRandomBytes(16);

                    //加密body
                    byte[] encryptData = cryptHelper.encryptBody(ivBytes, key, data);

                    byte[] compressedData = compressor.compress(encryptData);
                    TransferPackage transferPackage = PackageUtils
                            .buildTransferPackage(pkgType, DEFAULT_COMPRESS_TYPE, ivBytes, compressedData);
                    newPackages.add(transferPackage);

                    LOGGER.debug("ori data len = {}, iv bytes len = {}, encrypt data len = {}, compressed data len = {}", new Object[]{data.length, ivBytes.length, encryptData.length, compressedData.length});
                    LOGGER.debug("transfer pkg ...");
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return newPackages;
    }

    private Package correctPkg(byte[] all, short pkgType) {
        Package pkg = null;

        switch (pkgType) {
            case PackageType.PKG_CONNECT_REQ: {
                //不做处理
                ConnectReqPackage newPkg = (ConnectReqPackage) new ConnectReqPackage().valueOf(all);
                pkg = newPkg;
                //LOGGER.debug("pkg = {}, need connect = {}", pkg.getClass().getSimpleName(), newPkg.getHostname() + ":" + newPkg.getPort());
            }
            break;

            case PackageType.PKG_CONNECT_RESP: {
                ConnectRespPackage connectRespPackage = (ConnectRespPackage) new ConnectRespPackage().valueOf(all);
                int connId = connectRespPackage.getLocalConnId();
                int corospondConnId = connectRespPackage.getRemoteConnId();

                ConnectRespPackage newPkg = PackageUtils.buildConnectRespPackage(corospondConnId, connId, 1L);
                pkg = newPkg;
                //LOGGER.debug("pkg = {}, tunnel success for [{}] <===> [{}]", pkg.getClass().getSimpleName(), newPkg.getLocalConnId(), newPkg.getRemoteConnId());
            }
            break;

            case PackageType.PKG_CHANNEL_CLOSE: {
                ChannelClosePackage channelClosePackage = (ChannelClosePackage) new ChannelClosePackage().valueOf(all);
                int connId = channelClosePackage.getLocalConnId();
                int corospondConnId = channelClosePackage.getRemoteConnId();

                ChannelClosePackage newPkg = PackageUtils.buildChannelClosePackage(corospondConnId, connId, 1L);
                pkg = newPkg;
                LOGGER.debug("pkg = {}, need close [{}]", pkg.getClass().getSimpleName(), corospondConnId);
            }
            break;

            case PackageType.PKG_DEFAULT_DATA: {
                DefaultDataPackage dataPackage = (DefaultDataPackage) new DefaultDataPackage().valueOf(all);
                int connId = dataPackage.getLocalConnId();
                int corospondConnId = dataPackage.getRemoteConnId();

                DefaultDataPackage newPkg = PackageUtils.buildDataPackage(corospondConnId, connId, 1L, dataPackage.getBody());
                pkg = newPkg;
//                LOGGER.debug("pkg = {}, data send [{}] -> [{}]", pkg.getClass().getSimpleName(), newPkg.getRemoteConnId(), newPkg.getLocalConnId());
            }
            break;

            case PackageType.PKG_CHANNEL_CONTROL_PAUSE: {
                ChannelReadPausePackage channelReadPausePackage = (ChannelReadPausePackage) new ChannelReadPausePackage().valueOf(all);
                int connId = channelReadPausePackage.getLocalConnId();
                int corospondConnId = channelReadPausePackage.getRemoteConnId();

                ChannelReadPausePackage newPkg = PackageUtils.buildChannelReadPausePackage(corospondConnId, connId, 1L);
                pkg = newPkg;
                //LOGGER.debug("pkg = {}, need close [{}]", pkg.getClass().getSimpleName(), newPkg.getLocalConnId());
            }
            break;

            case PackageType.PKG_CHANNEL_CONTROL_START: {
                ChannelReadStartPackage channelReadStartPackage = (ChannelReadStartPackage) new ChannelReadStartPackage().valueOf(all);
                int connId = channelReadStartPackage.getLocalConnId();
                int corospondConnId = channelReadStartPackage.getRemoteConnId();

                ChannelReadStartPackage newPkg = PackageUtils.buildChannelReadStartPackage(corospondConnId, connId, 1L);
                pkg = newPkg;
                //LOGGER.debug("pkg = {}, need close [{}]", pkg.getClass().getSimpleName(), newPkg.getLocalConnId());
            }
            break;

            case PackageType.PKG_CONNECTION_AUTH_REQ:
                ConnectAuthReqPackage connectAuthReqPackage = (ConnectAuthReqPackage) new ConnectAuthReqPackage().valueOf(all);
                pkg = connectAuthReqPackage;
                break;

            case PackageType.PKG_CONNECTION_AUTH_RESP:
                ConnectAuthRespPackage connectAuthRespPackage = (ConnectAuthRespPackage) new ConnectAuthRespPackage().valueOf(all);
                pkg = connectAuthRespPackage;
                break;
            default:
                break;
        }
        return pkg;
    }
}
