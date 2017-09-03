package com.villcore.net.proxy.v2.server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * remote vps server side
 */
public class Server {
    public static void main(String[] args) {
        //TODO 配置信息获取
        //
        List<String> strs = new ArrayList<>();
        strs.stream().filter((String s) -> s.length() > 10).filter(String::isEmpty).collect(Collectors.toList());

    }
}
