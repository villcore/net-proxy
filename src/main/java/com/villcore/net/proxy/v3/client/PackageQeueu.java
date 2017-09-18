package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v2.pkg.Package;
import com.villcore.net.proxy.v2.pkg.PackageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PackageQeueu {
    private static Logger LOG = LoggerFactory.getLogger(PackageQeueu.class);

    private int capcity;
    private BlockingQueue<Package> packageQueue;

    public PackageQeueu(int capcity) {
        this.capcity = capcity;
        packageQueue = new LinkedBlockingQueue<>();
    }

    public void putPackage(Package pkg) throws InterruptedException {
        try {
            //LOG.debug("------------> {}, put pkg {} - >{}", packageQueue.size(), pkg.getClass().toString(), PackageUtils.toString(pkg.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
