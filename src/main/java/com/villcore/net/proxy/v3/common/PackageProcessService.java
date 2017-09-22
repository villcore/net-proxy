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
            //LOG.debug("connected connection size = {}", connections.size());
            //进行判断，是否connection可以发送
            connections.forEach(connection -> {
                //LOG.debug("connection send ready = {}", connection.sendPackageReady());
                if (connection.sendPackageReady()) {
                    //Connection#getAvaliableSendPackages
                    List<Package> avaliableSendPackages = tunnelManager.gatherSendPackages(connection);
                    //LOG.debug(">>{}", avaliableSendPackages.size());
//                avaliableSendPackages.stream().forEach(pkg -> {
//                    printDebug(pkg);
//                });

                    for (PackageHandler handler : sendHandlers) {
                        avaliableSendPackages = handler.handlePackage(avaliableSendPackages, connection);
                    }
//                LOG.debug("avaliable send packages len = {}", avaliableSendPackages.size());
                    connection.addSendPackages(avaliableSendPackages);
                }
            });


            connections.forEach(connection -> {
                //LOG.debug(">>>---");
                List<Package> avaliableRecvPackages = connection.getRecvPackages();
                if (!avaliableRecvPackages.isEmpty()) {
                    LOG.debug("handle recv package ...");
                }
                //authorized handler
                //connect resp handler
                //channel close handler
                //data handler
                for (PackageHandler handler : recvHandlers) {
                    if (!avaliableRecvPackages.isEmpty())
                        LOG.debug("recv handlers handle ...");
                    avaliableRecvPackages = handler.handlePackage(avaliableRecvPackages, connection);
                }

                tunnelManager.scatterRecvPackage(avaliableRecvPackages);
                //Connection#getAvalizableRecvPackages
                //遍历recvHandler对package处理，
                //1.对CONNECT_RESP处理，-1 Tunnel关闭，TunnelManager#needClose(), Tunnel#needClose() ,> 0, TunnelManager#markConnected(remoteConnId), Tunnel#markConnected();
                //2.对DATA_处理，如果TunnelManager#getTunnel, Tunnel#shouldClose或当前Tunnel为空,丢弃包，并构建SHOULD_CLOSE 发送到 SendPackage
                //如果一切正常，则将Package发送到Tunnel#putRecvPackage
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
