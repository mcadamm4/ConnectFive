package com.mark;

import java.net.ServerSocket;

public class TTT_Server {
    private static final int PORT = 9090;

    private String currentPlayer;

    public static void main(String[] args) throws Exception {
        try(ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Connect Five server is running...");
            while(true) {
                // Server continues to run until stopped
                System.out.println("Still running....");
            }
        }
    }
}