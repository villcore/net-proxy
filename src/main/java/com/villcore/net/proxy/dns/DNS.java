package com.villcore.net.proxy.dns;

import com.villcore.net.proxy.client.HostPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DNS {

    private static final Logger logger = LoggerFactory.getLogger(DNS.class);

    private static final Map<String, Boolean> ADDRESS_ACCESSABLITY = new LinkedHashMap<String, Boolean>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            if (this.size() >= 1000) {
                return true;
            }
            return false;
        }
    };

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // auto clean expire
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (DNS.class) {
               Iterator<Map.Entry<String, Boolean>> entryIterator = ADDRESS_ACCESSABLITY.entrySet().iterator();
               while (entryIterator.hasNext()) {
                   Boolean accessablity = entryIterator.next().getValue();
                   if(accessablity == null || !accessablity) {
                       entryIterator.remove();
                   }
               }
            }
        }, 30L, 30L, TimeUnit.SECONDS);

        // auto print access urls
//        scheduler.scheduleAtFixedRate(() -> {
//            synchronized (DNS.class) {
//                logger.info(" ================================================== ");
//                ADDRESS_ACCESSABLITY.forEach((k, v) -> {
//                    logger.info("url: {} -> {}", k, v);
//                });
//                logger.info(" ================================================= ");
//            }
//        }, 3L, 3L, TimeUnit.SECONDS);
    }

    public static boolean isAccessable(HostPort hostPort) {
        return isAccessable(hostPort.getHost(), hostPort.getPort());
    }

    public static boolean isAccessable(String address, int port) {
        if (address.contains("google")) {
            return false;
        }
        Boolean accessablity = getAccessablity(address);
        if (accessablity == Boolean.TRUE) {
            return true;
        }

        if (accessablity == null) {
            accessablity = connect(address, port);
            updateAccessablity(address, Boolean.valueOf(accessablity));
        }
        return accessablity;
    }

    private static boolean connect(String address, int port) {
        try (Socket socket = SocketFactory.getDefault().createSocket()) {
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(address, port), 3000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static synchronized void updateAccessablity(String address, Boolean accessablity) {
        ADDRESS_ACCESSABLITY.put(address, accessablity);
    }

    private static synchronized Boolean getAccessablity(String address) {
        return ADDRESS_ACCESSABLITY.get(address);
    }

    public static synchronized Map<String, Boolean> getAddressAccessablity() {
        return new HashMap<>(ADDRESS_ACCESSABLITY);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(isAccessable("www.youtube.com", 80));
        }
    }
}
