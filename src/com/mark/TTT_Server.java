package com.mark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class TTT_Server {
    private static final int PORT = 9090;

    public static void main(String[] args) throws Exception {
        try(ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println(String.format("Connect Five server is running on port %s...", PORT));
            while(true) {
                ConnectFive newGame = new ConnectFive();

                // Create two new players for the game and give each a thread
                ConnectFive.Player playerOne = newGame.new Player(listener.accept());
                ConnectFive.Player playerTwo = newGame.new Player(listener.accept());
                // Set the players as opponents and let P1 move first
                playerOne.setOpponent(playerTwo);
                playerTwo.setOpponent(playerOne);
                newGame.activePlayer = playerOne;

                playerOne.start();
                playerTwo.start();
            }
        }
    }
}

class ConnectFive {

    Player activePlayer;
    private static int playerCount = 0;

    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 6;
    private static final int CONNECT_FIVE = 5;

    private static String[][] players = new String[2][2];
    private static String[][] board;
    private static int[][] lastDropCoords; //Coordinates of the last chip dropped in a column

    public ConnectFive() {
        setupBoard();
        lastDropCoords = new int[BOARD_WIDTH-1][2];
        for(int i = 0; i < BOARD_WIDTH-1; i++) {
            // Add lastDrop coords in the form (x, y) - x being the row and y the column
            // Decrement the x value i.e. the height of each lastDrop as chips are added
            lastDropCoords[i] = new int[]{BOARD_HEIGHT, i};
        }
    }

    class Player extends Thread {
        private String playerChip;
        private PrintWriter out;
        private BufferedReader in;
        private Socket socket;

        private String playerName;
        private Player opponent;
        private int chipSelection;
        private String chipAssignMessage;

        public Player(Socket socket) {
            this.socket = socket;
            playerCount++;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read player name
                playerName = in.readLine();

                // Read chip preference selection
                chipSelection = Integer.parseInt(in.readLine());

                //Is this a valid chip and is it available i.e. (Did the first player already choose it)
                chipAssignMessage = assignChips(playerName, chipSelection);
                // Send the player a message to tell them what chip they have.
                out.println(chipAssignMessage);
                out.println(players[playerCount-1][1]);
                playerChip = players[playerCount-1][1];

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
                out.println(String.format("GAME_ON YOUR OPPONENT IS %s", this.opponent.playerName.toUpperCase()));
                while(true) {
                    // Read in players move
                    if(this.equals(activePlayer)) {
                        out.println("MAKE_A_MOVE");
                        int selectedColumn = Integer.parseInt(in.readLine());

                        if (isValidMove(selectedColumn)){
                            int[] dropCoords = getDropCoords(selectedColumn);

                            if(isWinningMove()) {
                                // Player has won the game
                                out.println("WINNER");
                                // Send back coords of winning row to both players + winner name

                                out.println(dropCoords[0]);
                                out.println(dropCoords[1]);

                                //....

                            } else {
                                // Move was valid, send back coords to client
                                out.println("VALID_MOVE");
                                out.println(dropCoords[0]);
                                out.println(dropCoords[1]);

                                // Alert opponent to new move
                                alertOpponentToMove(dropCoords);

                                // Update servers board
                                updateBoard(dropCoords, playerChip);
                                displayBoard();

                                // Opponents turn has begun
                                activePlayer = this.opponent;
                            }
                        } else {
                            // Selected column is already full
                            out.println("INVALID_MOVE");
                        }
                    } else if (!this.equals(activePlayer)) {
                        out.println("NOT_YOUR_TURN");
                    }
                }
            } catch (Exception e){
                System.out.print("Whoops " + e);
            }
        }

        private void alertOpponentToMove(int[] dropCoords) {
            opponent.out.println("NEW_MOVE");
            opponent.out.println(dropCoords[0]);
            opponent.out.println(dropCoords[1]);
        }

        void setOpponent(Player opponent) {
            this.opponent = opponent;
        }
    }


    // ConnectFive methods

    private static void updateBoard(int[] dropCoords, String playerChip) {
        board[dropCoords[0]][dropCoords[1]] = ("["+ playerChip + "]");
    }

    private static void displayBoard() {
        for (String[] arr : board) {
            for (String str : arr)
                System.out.print(str + " ");
            System.out.println();
        }
    }

    private static void setupBoard() {
        board = new String[BOARD_HEIGHT][BOARD_WIDTH];
        for(int i = 0; i<BOARD_HEIGHT; i++) {
            Arrays.fill(board[i], "[_]");
        }
    }

    private boolean isWinningMove() {
        // Check the last chip added, is this a winning move?
        return false;
    }

    private int[] getDropCoords(int selectedColumn) {
        selectedColumn -= 1; // Board is zero based
        int[] lastDropForColumn = lastDropCoords[selectedColumn];
        int[] newDropCoords = {lastDropForColumn[0]-1, lastDropForColumn[1]};

        System.out.println("Last drop: (" + lastDropCoords[selectedColumn][0] + ", " + lastDropCoords[selectedColumn][1] + ")");
        System.out.println("New drop: (" + (lastDropForColumn[0]-1) + ", " + lastDropForColumn[1] + ")");

        lastDropCoords[selectedColumn] = newDropCoords;
        return newDropCoords;
    }

    private boolean isValidMove(int selectedColumn) {
        int[] dropCoords = lastDropCoords[selectedColumn];
        if(dropCoords[0]==0) {
            //Column is full
            return false;
        }
        return true;
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
