package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.Package;

import java.util.List;
import java.util.Optional;

/**
 * Package 处理类
 *
 * 需要线程安全考虑
 */
public interface PackageHandler {
    List<Package> handlePackage(List<Package> packages, Connection connection);
}
