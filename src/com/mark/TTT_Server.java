package com.mark;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TTT_Server {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    private String currentPlayer;

    public static void main(String[] args) throws Exception {
        try(ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Connect Five server is running...");
            while(true) {
                try (Socket socket = listener.accept()) {
                    System.out.println("New Connection");
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("Connected");
                }
            }
        }
    }
}