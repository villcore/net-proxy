package com.villcore.net.proxy.v3.common;

import java.util.concurrent.*;

public class ScheduleService {
    private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);

    public void scheduleTask(Runnable target, long afterTime) {
        scheduleService.schedule(target, afterTime, TimeUnit.MILLISECONDS);
    }

    public void scheduleTaskAtFixedRate(Runnable target, long delay, long perid) {
        scheduleService.scheduleWithFixedDelay(target, delay, perid, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        ScheduleService ss = new ScheduleService();
        ss.scheduleTask(new Runnable() {
            @Override
            public void run() {
                System.out.println("runing...");
            }
        }, 1000);
    }
}
