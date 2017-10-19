package com.villcore.net.proxy.v3.common.handlers.client.connection;

import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.pkg.v2.*;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthReqPackage;
import com.villcore.net.proxy.v3.server.AuthStateCode;
import com.villcore.net.proxy.v3.server.SimpleUserManager;
import com.villcore.net.proxy.v3.server.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConnectAuthReqHandler implements PackageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectAuthReqHandler.class);

    private SimpleUserManager simpleUserManager;

    public ConnectAuthReqHandler(SimpleUserManager simpleUserManager) {
        this.simpleUserManager = simpleUserManager;
    }

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        List<Package> connectReqPackage = packages.stream()
                .filter(pkg -> pkg.getPkgType() == PackageType.PKG_CONNECTION_AUTH_REQ)
                .collect(Collectors.toList());

        List<Package> otherPackage = packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECTION_AUTH_REQ).collect(Collectors.toList());

        connectReqPackage.stream().map(pkg -> ConnectAuthReqPackage.class.cast(pkg)).collect(Collectors.toList())
                .forEach(pkg -> {
                    String username = pkg.getUsername();
                    String password = pkg.getPassword();
                    PackageUtils.release(Optional.of(pkg));

                    try {
                        if (SimpleUserManager.isCorrect(username, password)) {
                            UserInfo userInfo = new UserInfo(username, password);
                            connection.addSendPackages(Collections.singletonList(PackageUtils.buildConnectAuthRespPackage(username, AuthStateCode.AU_SUCCESS)));
                            connection.setAuthed(true);

                        } else {
                            connection.addSendPackages(Collections.singletonList(PackageUtils.buildConnectAuthRespPackage(username, AuthStateCode.AU_FAIL)));
                            connection.setUserInfo(null);
                            connection.setAuthed(false);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                    LOG.debug("handle connect auth req pkg, [username:{}] [password:{}]...", username, password);
                });
        return otherPackage;
    }
}
