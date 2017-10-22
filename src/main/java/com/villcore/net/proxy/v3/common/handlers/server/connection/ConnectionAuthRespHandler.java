package com.villcore.net.proxy.v3.common.handlers.server.connection;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.common.handlers.client.connection.ConnectAuthReqHandler;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import com.villcore.net.proxy.v3.pkg.v2.PackageType;
import com.villcore.net.proxy.v3.pkg.v2.PackageUtils;
import com.villcore.net.proxy.v3.pkg.v2.connection.ConnectAuthRespPackage;
import com.villcore.net.proxy.v3.server.AuthStateCode;
import com.villcore.net.proxy.v3.server.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConnectionAuthRespHandler implements PackageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectAuthReqHandler.class);

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        List<Package> connectReqPackage = packages.stream()
                .filter(pkg -> pkg.getPkgType() == PackageType.PKG_CONNECTION_AUTH_RESP)
                .collect(Collectors.toList());

        List<Package> otherPackage = packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECTION_AUTH_RESP).collect(Collectors.toList());

        connectReqPackage.stream().map(pkg -> ConnectAuthRespPackage.class.cast(pkg)).collect(Collectors.toList())
                .forEach(pkg -> {
                    String username = pkg.getUsername();
                    short stateCode = pkg.getStateCode();
                    PackageUtils.release(Optional.of(pkg));

                    try {
                        if(stateCode == AuthStateCode.AU_SUCCESS) {
                            connection.setAuthed(true);
                        }
                        if(stateCode == AuthStateCode.AU_FAIL) {
                            connection.setAuthed(false);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                    LOG.debug("handle connect auth resp pkg, [username:{}] [stateCode:{}]...", username, stateCode);
                });
        return otherPackage;
    }
}
