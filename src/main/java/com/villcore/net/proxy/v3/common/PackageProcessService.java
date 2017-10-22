package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v2.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Package 处理服务，主要针对发送的Package与接收的Package进行处理，通过添加Handler进行逻辑处理
 * <p>
 * PackageProcessService 使用线程池技术, 持有PackageHandler,对所有获取到的包进行处理
 */
public class PackageProcessService extends LoopTask {
    private static final Logger LOG = LoggerFactory.getLogger(PackageProcessService.class);

    private static final long SLEEP_INTERVAL = 10;

    private TunnelManager tunnelManager;
    private ConnectionManager connectionManager;

    private Set<PackageHandler> sendHandlers = new LinkedHashSet<>();
    private Set<PackageHandler> recvHandlers = new LinkedHashSet<>();

    private long time;

    private BlockingQueue<Package> sendPackageQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Package> recvPackageQueue = new LinkedBlockingQueue<>();

    public PackageProcessService(TunnelManager tunnelManager, ConnectionManager connectionManager) {
        this.tunnelManager = tunnelManager;
        this.connectionManager = connectionManager;
    }

    public void addSendHandler(PackageHandler packageHandler) {
        sendHandlers.add(packageHandler);
    }

    public void addSendHandler(PackageHandler... packageHandler) {
        sendHandlers.addAll(Arrays.asList(packageHandler));
    }

    public void addRecvHandler(PackageHandler packageHandler) {
        recvHandlers.add(packageHandler);
    }

    public void addRecvHandler(PackageHandler... packageHandler) {
        recvHandlers.addAll(Arrays.asList(packageHandler));
    }

    @Override
    public void loop() throws InterruptedException {
//        LOG.debug("package process service running ... send handler size = {}, recv handler size = {}", sendHandlers.size(), recvHandlers.size());
        time = System.currentTimeMillis();

        try {
            connectionManager.allConnected().forEach(connection -> {
                if (connection.sendPackageReady()) {
//                    System.out.println("==============================================================================");

                    List<Package> avaliableSendPackages = tunnelManager.gatherSendPackages(connection);
                    if (avaliableSendPackages.isEmpty()) {
                        return;
                    } else {
                    }
                    for (PackageHandler handler : sendHandlers) {
//                        System.out.println("==============================================================================");
                        avaliableSendPackages = handler.handlePackage(avaliableSendPackages, connection);
                        LOG.debug("handle send pkg ...");
                    }
                    LOG.debug("add send pkg ...");
                    connection.addSendPackages(avaliableSendPackages);
                } else {
                }
            });

            connectionManager.allConnected().forEach(connection -> {
                List<Package> avaliableRecvPackages = connection.getRecvPackages();
                for (PackageHandler handler : recvHandlers) {
                    if (!avaliableRecvPackages.isEmpty())
                        avaliableRecvPackages = handler.handlePackage(avaliableRecvPackages, connection);
                }
                tunnelManager.scatterRecvPackage(avaliableRecvPackages);
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        long workTime = System.currentTimeMillis() - time;
        if (workTime < SLEEP_INTERVAL) {
            TimeUnit.MILLISECONDS.sleep(SLEEP_INTERVAL - workTime);
        }
    }
}
