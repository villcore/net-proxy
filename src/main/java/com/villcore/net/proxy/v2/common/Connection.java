package com.villcore.net.proxy.v2.common;

/**
 * 主要的通信连接，该连接主要面向数据传输，从上层的虚拟通道
 */
public class Connection {
    //管理连接，网络情况差时能主动重连（client端）
    //定期任务，定时清理关闭和无响应的tunnel

    //主要run方法， write recv

    //tunnels
        //connId -> tunnel
        //channel -> tunnel

    //channel 数据传输channel
    //poll 发送与读取处理

    //addTunnel
    //closeTunnel

    //write
        //tunnel gather

    //read
        //tunnel scatter
}
