package com.mark;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TTT_Client {

    //    Construct a client and connects to the server
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    public TTT_Client() throws Exception {
        // Setup connection to server
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("Connection status: " + socket.isConnected());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String response = in.readLine();
            System.out.println(response);
        }
    }
    public static void main(String[] args) throws Exception {
        TTT_Client client = new TTT_Client();
    }
}