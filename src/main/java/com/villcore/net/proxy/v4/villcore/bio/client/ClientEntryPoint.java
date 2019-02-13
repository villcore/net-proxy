package com.villcore.net.proxy.v4.villcore.bio.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

public class ClientEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(ClientEntryPoint.class);

    public static void main(String[] args) {
        try {
            Client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
