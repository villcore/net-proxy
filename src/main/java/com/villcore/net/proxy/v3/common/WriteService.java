package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 负责发送Connection准备好的Package
 */
public class WriteService extends LoopTask {
    private static final Logger LOG = LoggerFactory.getLogger(WriteService.class);

//    private CopyOnWriteArrayList<Writeable> writeables = new CopyOnWriteArrayList<>();
    private Set<Writeable> copyOnWriteSet = new HashSet<>();
    private long sleepInterval = 10;

    private long time;

    public WriteService(long sleepInterval) {
        this.sleepInterval = sleepInterval;
    }

    public synchronized void addWrite(Writeable writeable) {
        //writeables.add(writeable);
        Set<Writeable> newWriteables = new HashSet<>(copyOnWriteSet);
        newWriteables.add(writeable);
        copyOnWriteSet = newWriteables;
    }

    public synchronized void removeWrite(Writeable writeable) {
        //writeables.remove(writeable);
        Set<Writeable> newWriteables = new HashSet<>(copyOnWriteSet);
        newWriteables.remove(writeable);
        copyOnWriteSet = newWriteables;
    }

    @Override
    void loop() throws InterruptedException {
//        LOG.debug("write service loop ...");
        time = System.currentTimeMillis();

        try {
            //主要的遍历writable
            copyOnWriteSet.forEach(writeable -> writeable.write());
//            if(!copyOnWriteSet.isEmpty()) {
//                LOG.debug("cur writable size = {} ...", copyOnWriteSet.size());
//            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        long workTime = System.currentTimeMillis() - time;
        if(workTime < sleepInterval) {
            TimeUnit.MILLISECONDS.sleep(sleepInterval - workTime);
        }
    }
}
