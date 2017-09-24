package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Package 处理服务，主要针对发送的Package与接收的Package进行处理，通过添加Handler进行逻辑处理
 *
 */
public class PackageProcessService extends LoopTask {
    private static final Logger LOG = LoggerFactory.getLogger(PackageProcessService.class);

    private static final long SLEEP_INTERVAL = 50;

    private TunnelManager tunnelManager;
    private ConnectionManager connectionManager;

    private Set<PackageHandler> sendHandlers = new HashSet<>();
    private Set<PackageHandler> recvHandlers = new HashSet<>();

    private long time;

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
        //LOG.debug("package process service loop ...");
        time = System.currentTimeMillis();

        try {
            //TODO connection waterMarker handle...
            List<Connection> connections = connectionManager.allConnected();
            connections.forEach(connection -> {
                if (connection.sendPackageReady()) {
                    List<Package> avaliableSendPackages = tunnelManager.gatherSendPackages(connection);

                    for (PackageHandler handler : sendHandlers) {
                        avaliableSendPackages = handler.handlePackage(avaliableSendPackages, connection);
                    }
                    connection.addSendPackages(avaliableSendPackages);
                }
            });

            connections.forEach(connection -> {
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
        if(workTime < SLEEP_INTERVAL) {
            TimeUnit.MILLISECONDS.sleep(SLEEP_INTERVAL - workTime);
        }
    }

    private void printDebug(Package pkg) {
        try {
            LOG.debug("pkg corspondId = {}, {}", DefaultDataPackage.class.cast(pkg).getRemoteConnId(), pkg.toString());
            LOG.debug(PackageUtils.toString(pkg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
