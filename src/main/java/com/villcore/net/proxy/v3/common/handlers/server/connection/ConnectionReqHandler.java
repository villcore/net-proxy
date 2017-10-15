//package com.villcore.net.proxy.v3.common.handlers.server.connection;
//
//import com.villcore.net.proxy.v3.common.Connection;
//import com.villcore.net.proxy.v3.common.PackageHandler;
//import com.villcore.net.proxy.v3.pkg.v1.Package;
//import com.villcore.net.proxy.v3.pkg.v1.PackageType;
//import com.villcore.net.proxy.v3.pkg.v1.connection.ConnectAuthRespPackage;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class ConnectionReqHandler implements PackageHandler {
//    @Override
//    public List<Package> handlePackage(List<Package> packages, Connection connection) {
//        packages.stream().filter(pkg -> pkg.getPkgType() == PackageType.PKG_CONNECTION_AUTH_REQ)
//                .map(( pkg -> ConnectAuthRespPackage.class.cast(pkg)))
//                .collect(Collectors.toList())
//                .forEach(pkg -> {
//                    //server side 授权处理
//                });
//
//        return packages.stream().filter(pkg -> pkg.getPkgType() != PackageType.PKG_CONNECTION_AUTH_REQ).collect(Collectors.toList());
//    }
//}
