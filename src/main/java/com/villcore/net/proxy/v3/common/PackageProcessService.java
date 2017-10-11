package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v1.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Package 处理服务，主要针对发送的Package与接收的Package进行处理，通过添加Handler进行逻辑处理
 *
 */
public class PackageProcessService extends LoopTask {
    private static final Logger LOG = LoggerFactory.getLogger(PackageProcessService.class);

    private static final long SLEEP_INTERVAL = 10;

    private TunnelManager tunnelManager;
    private ConnectionManager connectionManager;

    private Set<PackageHandler> sendHandlers = new LinkedHashSet<>();
    private Set<PackageHandler> recvHandlers = new LinkedHashSet<>();

    private Set<PackageHandler> connectionReqHandlers = new LinkedHashSet<>();
    private Set<PackageHandler> connectionRespHandlers = new LinkedHashSet<>();


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

    public void addConnectionReqHandler(PackageHandler packageHandler) {
        connectionReqHandlers.add(packageHandler);
    }

    public void addConnectionReqHandler(PackageHandler... packageHandler) {
        connectionReqHandlers.addAll(Arrays.asList(packageHandler));
    }

    public void addConnectionRespHandler(PackageHandler packageHandler) {
        connectionRespHandlers.add(packageHandler);
    }

    public void addConnectionRespHandler(PackageHandler... packageHandler) {
        connectionRespHandlers.addAll(Arrays.asList(packageHandler));
    }


    @Override
    public void loop() throws InterruptedException {
        //LOG.debug("package process service loop ...");
        time = System.currentTimeMillis();

        try {
            List<Connection> connections = connectionManager.allConnected();
            connections.forEach(connection -> {
                if (connection.sendPackageReady()) {
                    //LOG.debug(">>>");
                    List<Package> avaliableSendPackages = tunnelManager.gatherSendPackages(connection);
                    if(avaliableSendPackages.size() > 0) {
//                        LOG.debug("tunnels has pkg to send ...");
                    } else {
                        //LOG.debug("tunnels has no pkg to send ...");
                    }
                    for (PackageHandler handler : sendHandlers) {
                        avaliableSendPackages = handler.handlePackage(avaliableSendPackages, connection);
                    }
                    connection.addSendPackages(avaliableSendPackages);
                } else {
//                    LOG.debug("===");
                }
            });

            connectionManager.allConnected().forEach(connection -> {
                List<Package> avaliableRecvPackages = connection.getRecvPackages();
//                if(!avaliableRecvPackages.isEmpty()) {
//                    LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>{}, {}", avaliableRecvPackages.size(), avaliableRecvPackages.size());
//                } else {
//                    LOG.debug("recv queue for conn {} empty ...", connection);
//                }

                //LOG.debug("before {}", avaliableRecvPackages.size());
                for (PackageHandler handler : recvHandlers) {
                    //LOG.debug("{}", handler.getClass().toString());
                    if (!avaliableRecvPackages.isEmpty())
                    avaliableRecvPackages = handler.handlePackage(avaliableRecvPackages, connection);
                }
                //LOG.debug("---");
                //LOG.debug("after {}", avaliableRecvPackages.size());
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
}
