package com.villcore.net.proxy.dns;

import com.villcore.net.proxy.client.HostPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DNS {

    private static final Logger logger = LoggerFactory.getLogger(DNS.class);

    private static volatile boolean globalProxy = false;

    private static final Map<String, Accessiblity> ADDRESS_ACCESSABLITY = new LinkedHashMap<String, Accessiblity>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Accessiblity> eldest) {
            if (this.size() >= 1000) {
                return true;
            }
            return false;
        }
    };

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // auto clean expire
//        scheduler.scheduleAtFixedRate(() -> {
//            synchronized (DNS.class) {
//               Iterator<Map.Entry<String, Boolean>> entryIterator = ADDRESS_ACCESSABLITY.entrySet().iterator();
//               while (entryIterator.hasNext()) {
//                   Boolean accessablity = entryIterator.next().getValue();
//                   if(accessablity == null || !accessablity) {
//                       entryIterator.remove();
//                   }
//               }
//            }
//        }, 30L, 30L, TimeUnit.SECONDS);

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
        Accessiblity accessablity = getAccessablity(address);
        if (accessablity == null) {
            accessablity = connect(address, port);
            updateAccessablity(address, accessablity.accessable);
        }
        return accessablity.accessable;
    }

    private static Accessiblity connect(String address, int port) {
        if (globalProxy) {
            return new Accessiblity(address, false, 0);
        }

        if (address.contains("google")) {
            return new Accessiblity(address, false, 0);
        }

        try (Socket socket = SocketFactory.getDefault().createSocket()) {
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(address, port), 3000);
            return new Accessiblity(address, true, 0);
        } catch (IOException e) {
            return new Accessiblity(address, false, 0);
        }
    }

    public static synchronized void updateAccessablity(String address, Boolean accessablity) {
        Accessiblity access = ADDRESS_ACCESSABLITY.computeIfAbsent(address, k -> new Accessiblity(address,true, 0));
        access.accessable = accessablity;
    }

    public static synchronized void removeAccessablity(String address) {
        ADDRESS_ACCESSABLITY.remove(address);
    }

    public static synchronized Accessiblity getAccessablity(String address) {
        return ADDRESS_ACCESSABLITY.get(address);
    }

    public static synchronized Map<String, Accessiblity> getAddressAccessablity() {
        return new HashMap<>(ADDRESS_ACCESSABLITY);
    }

    public static synchronized void connectAddr(String address) {
        Accessiblity access = ADDRESS_ACCESSABLITY.computeIfAbsent(address, k -> new Accessiblity(address, true, 0));
        access.count = access.count + 1;
    }

    public static synchronized void disConnectAddr(String address) {
        Accessiblity access = ADDRESS_ACCESSABLITY.computeIfAbsent(address, k -> new Accessiblity(address, true, 0));
        access.count = access.count - 1;
    }

    public static boolean isGlobalProxy() {
        return globalProxy;
    }

    public static synchronized void setGlobalProxy(boolean _globalProxy) {
        globalProxy = _globalProxy;
        ADDRESS_ACCESSABLITY.clear();
    }

    public static class Accessiblity {
        String address;
        boolean accessable;
        int count;

        public Accessiblity(String address, boolean accessable, int count) {
            this.address = address;
            this.accessable = accessable;
            this.count = count;
        }

        public boolean isAccessable() {
            return accessable;
        }

        public void setAccessable(boolean accessable) {
            this.accessable = accessable;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Accessiblity that = (Accessiblity) o;
            return accessable == that.accessable &&
                    count == that.count &&
                    address.equals(that.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, accessable, count);
        }

        @Override
        public String toString() {
            return "Accessiblity{" +
                    "address='" + address + '\'' +
                    ", accessable=" + accessable +
                    ", count=" + count +
                    '}';
        }
    }
}
