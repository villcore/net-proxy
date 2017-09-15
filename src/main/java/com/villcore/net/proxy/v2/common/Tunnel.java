package com.villcore.net.proxy.v2.common;

/**
 * client 与 server 对每个连接的代理通道
 */
public class Tunnel {

    /**
     * Netty IO线程中队Tunnel队列已经充满的不继续写信息
     */
    //代理通道 Channel

    //connId        本地端对应的connId
    //correspondId 远端对应的ConnId
    //数据队列（线程安全的双端队列）
    //lasttouch
    //close
}
