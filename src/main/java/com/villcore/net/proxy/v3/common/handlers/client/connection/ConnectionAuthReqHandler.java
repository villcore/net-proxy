package com.villcore.net.proxy.v3.common.handlers.client.connection;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.pkg.v1.Package;

import java.util.Collections;
import java.util.List;

public class ConnectionAuthReqHandler implements PackageHandler {

    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
//        Package authPkg = connection.getConnectionAuthReq();
//        connection.addSendPackages(Collections.singletonList(authPkg));
        return Collections.emptyList();
    }
}
