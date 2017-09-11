package com.villcore;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueTest {
    @Test
    public void queueDrainTest() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();
        blockingQueue.put("1");
        blockingQueue.put("2");
        blockingQueue.put("3");

//        while(blockingQueue.size() > 0) {
//            System.out.println(blockingQueue.take());
//        }

        List<String> list = new ArrayList<>();
        blockingQueue.drainTo(list);
        for(String s : list) {
            System.out.println(s);
        }
    }
}
