package com.villcore.net.proxy.v3.server;

public class SimpleUserManager {
    public static boolean isCorrect(String username, String passowrd) {
        if(username.equals("villcore") && passowrd.equals("123123")) {
            return true;
        }
        return false;
    }
}
