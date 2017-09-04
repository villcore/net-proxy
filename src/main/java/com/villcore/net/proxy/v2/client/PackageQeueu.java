package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.v2.pkg.Package;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PackageQeueu {
    private int capcity;
    private BlockingQueue<Package> packageQueue;

    public PackageQeueu(int capcity) {
        this.capcity = capcity;
        packageQueue = new LinkedBlockingQueue<>();
    }

    public void putPackage(Package pkg) throws InterruptedException {
        packageQueue.put(pkg);
    }

    public int size() {
        return packageQueue.size();
    }

    public List<Package> drainPackage() {
        List<Package> packages = new LinkedList<>();
        if(packageQueue.drainTo(packages) > 0) {
            return packages;
        }
        return packages;
    }
}
