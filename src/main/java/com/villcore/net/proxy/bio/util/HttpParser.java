package com.villcore.net.proxy.bio.util;

import java.net.InetSocketAddress;

public class HttpParser {
    public static InetSocketAddress parseAddress(byte[] httpRequest) {
        for (int i = 0; i < httpRequest.length; i++) {
            if (httpRequest[i] == 10 || httpRequest[i] == 13) {
                String firstLine = new String(httpRequest, 0, i);
                System.out.println("http first line ========== " + firstLine);

                int start = 0, end = 0;
                for (int j = 0; j < i; j++) {
                    if (httpRequest[j] == 32) {
                        start = j;
                        break;
                    }
                }

                for (int j = i - 1; j > 0; j--) {
                    if (httpRequest[j] == 32) {
                        end = j;
                        break;
                    }
                }

                String addressInfo = new String(httpRequest, start, end - start).trim();

                int preSplitIndex = 0;
                int postSplitIndex = addressInfo.length();
                String correctAddress = "";
                String protocal = "";

                preSplitIndex = addressInfo.indexOf("://");

                if (preSplitIndex > 0) {
                    preSplitIndex = preSplitIndex + "://".length();
                } else {
                    preSplitIndex = 0;
                }

                postSplitIndex = addressInfo.indexOf("/", preSplitIndex);
                if (postSplitIndex > 0) {

                } else {
                    postSplitIndex = addressInfo.length();
                }

                protocal = addressInfo.substring(0, preSplitIndex).replace("://", "");
                correctAddress = addressInfo.substring(preSplitIndex, postSplitIndex).trim();

//                            System.out.println(correctAddress);


                String[] addressAndPortArr = correctAddress.split(":");

                String address = addressAndPortArr[0];
                int port = 80;

                String portStr = addressAndPortArr.length > 1 ? addressAndPortArr[1] : "";
                if (portStr != null) {
                    port = 80;

                    if (protocal != null && protocal.length() != 0) {
                        if ("ftp".equalsIgnoreCase(protocal)) {
                            port = 21;
                        }
                    }
                }

                if (portStr != null) {
                    port = Integer.valueOf(portStr);
                }

                System.out.printf("protocal = %s, address: %s, port = %d\n", protocal, address, port);
                return new InetSocketAddress(address, port);
            }
        }
        return null;
    }
}
