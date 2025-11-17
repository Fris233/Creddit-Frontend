package com.fhm.take2;

import com.Client;

public class Main {
    public static void main(String[] args) {
        Client.init();
        if(!Client.isServerReachable()) {
            System.out.println("Server Unreachable!");
            return;
        }
        HelloApplication.main(args);
    }
}