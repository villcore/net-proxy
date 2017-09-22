package com.villcore.net.proxy.v3.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 负责发送Connection准备好的Package
 */
public class WriteService extends LoopTask {
    private static final Logger LOG = LoggerFactory.getLogger(WriteService.class);

    private static final long SLEEP_INTERVAL = 50;

    private CopyOnWriteArrayList<Writeable> writeables = new CopyOnWriteArrayList<>();

    private long time;

    public void addWrite(Writeable writeable) {
        writeables.add(writeable);
    }

    public void removeWrite(Writeable writeable) {
        writeables.remove(writeable);
    }

    @Override
    void loop() throws InterruptedException {
        //LOG.debug("write service loop ...");
        time = System.currentTimeMillis();

        try {
            //主要的遍历writable
            writeables.forEach(writeable -> writeable.write());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        long workTime = System.currentTimeMillis() - time;
        if(workTime < SLEEP_INTERVAL) {
            TimeUnit.MILLISECONDS.sleep(SLEEP_INTERVAL - workTime);
        }
    }
}
