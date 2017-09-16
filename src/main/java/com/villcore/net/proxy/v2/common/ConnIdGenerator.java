package com.villcore.net.proxy.v2.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用来在client与server端生成唯一的connId
 */
public class ConnIdGenerator {
    private final AtomicInteger idCount = new AtomicInteger(0);

    /**
     * 自增生成一个唯一connId，该Id只能在该Jvm维持唯一（数值越界不考虑,会自动重置）
     *
     * @return
     */
    public Integer generateConnId() {
        return idCount.getAndIncrement() & 0x7fffffff;
    }
}
