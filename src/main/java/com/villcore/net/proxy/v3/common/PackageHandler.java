package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v1.Package;

import java.util.List;

/**
 * Package 处理类
 *
 * 需要线程安全考虑
 */
public interface PackageHandler {
    List<Package> handlePackage(List<Package> packages, Connection connection);
}
