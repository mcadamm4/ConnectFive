package com.mark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TTT_Server {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    // How many players are connected and who's turn is it

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

    private static String activePlayer = "";

    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 6;

    private static String[][] players = new String[2][2];

    class Player extends Thread {
        private PrintWriter out;
        private BufferedReader in;
        private Socket socket;

        private String playerName;
        private int chipSelection;
        private String chipAssignMessage;

        public Player(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read player name
                playerName = in.readLine();

                // Read chip preference selection
                chipSelection = Integer.parseInt(in.readLine());

                //Is this a valid chip and is it available i.e. (Did the first player already choose it)
                chipAssignMessage = assignChips(playerName, chipSelection);
                out.println(chipAssignMessage);

                // Set first player to connect as the first to move
                if(activePlayer.isEmpty())
                    activePlayer = players[0][1];

                // Send playing board dimensions
                out.println(BOARD_HEIGHT);
                out.println(BOARD_WIDTH);

                System.out.println(playerName + " has successfully connected to the game!");

                // Waiting for opponent
                if(players[1][0]==null) {
                    String message = "WAITING FOR OPPONENT TO CONNECT!";
                    out.println(message);
                }

            } catch (IOException e) {
                System.out.println("OOps");
            }
        }
        public void run() {
            try {
                out.println("GAME ON!");
                while(true) {
                    String input = in.readLine();
                    System.out.println(input);
                }
            } catch (Exception e){

            }
        }
    }

    public String assignChips(String playerName, int chipSelection) {
        String message = "";

        if(chipSelection==1) {
            if(players[0][0]==null) {
                // Player is first to connect so chip is available
                players[0][0] = playerName;
                players[0][1] = "X";
                message = "You will be playing as X's";
            } else {
                // Player is last to connect and chip is unavailable
                message = assignRemainingChip(playerName);
            }
        } else if(chipSelection==2) {
            if(players[0][0]==null) {
                // Player is first to connect so chip is available
                players[0][0] = playerName;
                players[0][1] = "O";
                message = "You will be playing as O's";
            } else {
                // Player is last to connect and chip is unavailable
                message = assignRemainingChip(playerName);
            }
        }
        return message;
    }

    private String assignRemainingChip(String playerName) {
        // Player 1 has chosen selected chip, assign remaining chip to this player
        String message;
        players[1][0] = playerName;
        players[1][1] = (players[0][1]=="X") ? "O" : "X";
        message = ("Your opponent selected " + players[0][1] + "'s, you will be playing as " + players[1][1]) + "'s";
        return message;
    }
}
