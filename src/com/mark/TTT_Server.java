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
                ConnectFive newGame;
                newGame = new ConnectFive();

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

    private static final int BOARD_WIDTH = 9; // Easily alter the rules of the game via these values
    private static final int BOARD_HEIGHT = 6;
    private static final int CONNECT_NUM = 5;

    private static String[][] players = new String[2][2];
    private static String[][] board;
    private static int[][] lastDropCoords; //Coordinates of the last chip dropped in a column
    private static int[][] winningRowCoords;

    ConnectFive() {
        setupBoard();

        lastDropCoords = new int[BOARD_WIDTH][2];
        for(int i = 0; i < BOARD_WIDTH; i++) {
            // Add lastDrop coords in the form (x, y) - x being the row and y the column
            // Decrement the x value i.e. the height of each lastDrop as chips are added
            lastDropCoords[i] = new int[]{BOARD_HEIGHT, i};
        }

        // Record winning row and send to client to highlight
        winningRowCoords = new int[CONNECT_NUM][2];
    }

    class Player extends Thread {
        private String playerChip;
        private PrintWriter out;
        private BufferedReader in;

        private String playerName;
        private Player opponent;
        private int chipSelection;
        private String chipAssignMessage;

        Player(Socket socket) {
            playerCount++;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                setupPlayer();
                sendGameDetails();

                System.out.println(playerName + " has successfully connected to the game!");

                // Waiting for opponent
                if(players[1][0]==null) {
                    String message = "WAITING FOR OPPONENT TO CONNECT!";
                    out.println(message);
                }
            } catch (IOException e) {
                System.out.println("ERROR: \n" + e);
            }
        }

        private void sendGameDetails() {
            // Send playing board dimensions
            out.println(BOARD_HEIGHT);
            out.println(BOARD_WIDTH);
            out.println(CONNECT_NUM);
        }

        private void setupPlayer() throws IOException {
            // Read player name
            playerName = in.readLine().toUpperCase();

            // Read chip preference selection
            chipSelection = Integer.parseInt(in.readLine());

            //Is this a valid chip and is it available i.e. (Did the first player already choose it)
            chipAssignMessage = assignChips(playerName, chipSelection);
            // Send the player a message to tell them what chip they have.
            out.println(chipAssignMessage);
            out.println(players[playerCount-1][1]);
            playerChip = players[playerCount-1][1];
        }

        public void run() {
            out.println(String.format("GAME_ON YOUR OPPONENT IS %s", this.opponent.playerName.toUpperCase()));
            try {
                play();
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        private void play() throws Exception {
            boolean gameInProgress = true;
            while(gameInProgress) {
                // Who's move is it
                if(this.equals(activePlayer)) {
                    out.println("MAKE_A_MOVE");
                    int selectedColumn = Integer.parseInt(in.readLine())-1;

                    if (isValidMove(selectedColumn)) {
                        int[] dropCoords = getDropCoords(selectedColumn);

                        // Update servers board
                        updateBoard(dropCoords, this.playerChip);
                        displayBoard();

                        if (isWinningMove(dropCoords, this.playerChip)) {
                            // Player has won the game
                            out.println("WINNER " + dropCoords[0] + " " +  dropCoords[1] + " " + playerName);
                            // Inform opponent
                            opponent.out.println("WINNER " + dropCoords[0] + " " + dropCoords[1] + " " + playerName);

                            break;
                        } else {
                            // Move was valid, send back coords to client
                            out.println("VALID_MOVE");
                            out.println(dropCoords[0]);
                            out.println(dropCoords[1]);

                            // Alert opponent to new move
                            opponent.out.println("OPPONENT_MOVED " + dropCoords[0] + " " + dropCoords[1]);


                            // Opponents turn has begun
                            activePlayer = this.opponent;
                        }
                    } else {
                        // Selected column is already full
                        out.println("INVALID_MOVE");
                    }

                    if(!gameInProgress) {
                        out.println("GAME_OVER");
                    }
                } else {
                    out.println("NOT_YOUR_TURN");
                }
            }
        }

        void setOpponent(Player opponent) {
            this.opponent = opponent;
        }
    }

    /* CONNECT_FIVE METHODS */

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

    private boolean isWinningMove(int[] dropCoords, String playerChip) {
        int x = dropCoords[0];
        int y = dropCoords[1];
        return checkVertical(x, y, playerChip) || checkHorizontal(x, playerChip) ||
                checkForwardDiagonal(x, y, playerChip) || checkBackwardDiagonal(x, y, playerChip);
    }

    private boolean checkBackwardDiagonal(int x, int y, String playerChip) {
        int rowCount = 1;
        int x1 = x;
        int y1 = y;
        for(int i = 0; i < CONNECT_NUM; i++) {
            // Forward & Up
            try {
                String chip = String.valueOf(board[++x][--y].charAt(1));
                if (!chip.equals(playerChip)) {
                    break;
                }
                rowCount++; // Found one of my chips, add it to count and check the next slot
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        for(int i = 0; i < CONNECT_NUM; i++) {
            // Backwards & Down
            try {
                String chip = String.valueOf(board[--x1][++y1].charAt(1));
                if (!chip.equals(playerChip)) {
                    break;
                }
                rowCount++; // Found one of my chips, add it to count and check the next slot
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        return (rowCount == CONNECT_NUM);
    }

    private boolean checkForwardDiagonal(int x, int y, String playerChip) {
        int rowCount = 1;
        int x1 = x;
        int y1 = y;
        for(int i = 0; i < CONNECT_NUM; i++) {
            // Backwards & Up
            try {
                String chip = String.valueOf(board[--x][--y].charAt(1));
                if (!chip.equals(playerChip)) {
                    break;
                }
                rowCount++; // Found one of my chips, add it to count and check the next slot
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        for(int i = 0; i < CONNECT_NUM; i++) {
            // Forward & Down
            try {
                String chip = String.valueOf(board[++x1][++y1].charAt(1));
                if (!chip.equals(playerChip)) {
                    break;
                }
                rowCount++; // Found one of my chips, add it to count and check the next slot
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        return (rowCount == CONNECT_NUM);
    }

    private boolean checkHorizontal(int x, String playerChip) {
        // Check the entire row for enough consecutive chips for a win
        int rowCount = 0;
        for(int i = 0; i < BOARD_WIDTH; i++) {
            try {
                String chip = String.valueOf(board[x][i].charAt(1));
                if(!chip.equals(playerChip)) {
                    rowCount = 0;
                    continue;
                }
                rowCount++;
                if (rowCount == CONNECT_NUM)
                    return true;
            } catch(IndexOutOfBoundsException e) {
                break;
            }
        }
        return (rowCount == CONNECT_NUM);
    }

    private boolean checkVertical(int x, int y, String playerChip) {
        // Minimum height x must be for vertical win
        if(x <= (BOARD_HEIGHT - CONNECT_NUM)) {
            for(int i = 0; i < CONNECT_NUM; i++) {
                try {
                    String str = board[++x][y].substring(1, 2);
                    if (!str.equals(playerChip))
                        return false; // Found an opponents chip
                     winningRowCoords[i] = new int[]{x,y};
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }
            return true;
        }
        return false; // Dont waste time checking, as not enough counters for win
    }

    private int[] getDropCoords(int selectedColumn) {
        int[] lastDropForColumn = lastDropCoords[selectedColumn];
        int[] newDropCoords = {(lastDropForColumn[0]-1), lastDropForColumn[1]};

        System.out.println("Last drop: (" + lastDropCoords[selectedColumn][0] + ", " + lastDropCoords[selectedColumn][1] + ")");
        System.out.println("New drop: (" + (lastDropForColumn[0]-1) + ", " + lastDropForColumn[1] + ")");

        lastDropCoords[selectedColumn] = newDropCoords;
        return newDropCoords;
    }

    private boolean isValidMove(int selectedColumn) {
        if(selectedColumn < 0 || selectedColumn > BOARD_WIDTH-1) {
            // Value is out of bounds
            return false;
        }
        int[] dropCoords = lastDropCoords[selectedColumn];
        // Column is full
        return (dropCoords[0] != 0);
    }

    private String assignChips(String playerName, int chipSelection) {
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
