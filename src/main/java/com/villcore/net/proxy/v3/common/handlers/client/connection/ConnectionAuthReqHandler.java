package com.villcore.net.proxy.v3.common.handlers.client.connection;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageHandler;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageType;

import java.util.List;
import java.util.stream.Collectors;

public class ConnectionAuthReqHandler implements PackageHandler {
    @Override
    public List<Package> handlePackage(List<Package> packages, Connection connection) {
        short packageType = PackageType.PKG_CONNECTION_AUTH_REQ;

        return getOthersPackages(packagespackageType);
    }

    private List<Package> getOthersPackages(List<Package> packages, short packageType) {
        return packages.stream().filter(pkg -> pkg.getPkgType() != packageType).collect(Collectors.toList());
    }

    private List<Package> getPackages(List<Package> packages, short packageType) {
        return packages.stream().filter(pkg -> pkg.getPkgType() == packageType).collect(Collectors.toList());
    }
}
