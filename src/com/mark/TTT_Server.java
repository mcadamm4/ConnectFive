package com.mark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TTT_Server {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private static int playerCount = 0;

    // How many players are connected and who's turn is it
    private String activePlayer;

    public static void main(String[] args) throws Exception {
        String playerName = null;
        try(ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Connect Five server is running...");
            while(true) {
                ConnectFive newGame = new ConnectFive();
                // Create two new players for the game and give each a thread
                ConnectFive.Player playerOne = newGame.new Player(listener.accept());
                ConnectFive.Player playerTwo = newGame.new Player(listener.accept());
                playerOne.start();
                playerTwo.start();
            }
        }
    }
}

class ConnectFive {

    private static int playerCount = 0;

    class Player extends Thread {
        private PrintWriter out;
        private BufferedReader in;
        private Socket socket;

        private String playerName;
        private String playerChip;

        public Player(Socket socket) {
            this.socket = socket;
            playerCount++;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                playerName = in.readLine();
                playerChip = in.readLine();

                System.out.println(playerName + " has successfully connected to the game!");
                String message = "| Player Name: " + playerName + " | Player Number: " + playerCount + " | " + " Chip: " + playerChip + " | ";

                out.println(message);
            } catch (IOException e) {
                System.out.println("OOps");
            }
        }
    }
}
