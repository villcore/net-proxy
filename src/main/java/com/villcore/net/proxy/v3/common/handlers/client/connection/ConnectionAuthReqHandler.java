package com.villcore.net.proxy.v3.common.handlers.client.connection;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionAuthReqHandler implements PackageHandler {

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
//        Package authPkg = connection.getConnectionAuthReq();
//        connection.addSendPackages(Collections.singletonList(authPkg));
        return Collections.emptyList();
    }
}
