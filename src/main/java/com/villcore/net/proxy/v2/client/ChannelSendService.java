//package com.villcore.net.proxy.v2.client;
//
//import com.villcore.net.proxy.v2.pkg.Package;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///**
// * 从PackageQueue中读取数据，然后进行逻辑处理，最后发送给相应的channel
// */
//public abstract class ChannelSendService implements Runnable {
//    private static final Logger LOG = LoggerFactory.getLogger(ChannelSendService.class);
//
//    private volatile boolean running;
//    protected PackageQeueu packageQeueu;
//
//    public ChannelSendService(PackageQeueu packageQeueu) {
//        this.packageQeueu = packageQeueu;
//    }
//
//    public void start() {
//        running = true;
//    }
//
//    public void stop() {
//        running = false;
//    }
//
//    @Override
//    public void run() {
//        while (!Thread.currentThread().isInterrupted() && running) {
//            List<Package> packages = packageQeueu.drainPackage();
//            if (packages.isEmpty()) {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(100L);
//                } catch (InterruptedException e) {
//                    LOG.error(e.getMessage(), e);
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//            try {
//                packages = processPackages(packages);
//                sendPackages(packages);
//            } catch (Exception e) {
//                LOG.error(e.getMessage(), e);
//            }
//        }
//    }
//
//    protected abstract void sendPackages(List<Package> packages) throws Exception;
//
//    protected abstract List<Package> processPackages(List<Package> packages) throws Exception;
//}
