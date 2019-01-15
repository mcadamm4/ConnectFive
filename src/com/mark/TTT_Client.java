package com.mark;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TTT_Client {
    private final BufferedReader in;
    private final PrintWriter out;
    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private static int CONNECT_NUM;

    // Define the playing board
    private String[][] board;

    private String playerChip;
    private String opponentChip;

    //    Construct a client and connects to the server
    public TTT_Client(String playerName) throws Exception {
        int chipSelection;

        // Setup connection to server
        try (Socket socket = new Socket(HOST, PORT)) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connection status: " + socket.isConnected());

            // Send User name to server
            out.println(playerName);

            // Send user chip preference to server - ([1 = X], [2 = O])
            chipSelection = getChipSelection();
            out.println(chipSelection);

            // Output players assigned chip
            String chipResponse = in.readLine();
            playerChip = in.readLine();
            opponentChip = (playerChip.equals("X") ? "O" : "X");
            System.out.println(chipResponse);

            // Setup the playing board with specified dimensions from server
            int boardHeight = Integer.parseInt(in.readLine());
            int boardWidth = Integer.parseInt(in.readLine());
            // How many counters for winning row (Rules of the game can be easily changed via the server)
            CONNECT_NUM = Integer.parseInt(in.readLine());
            setupBoard(boardHeight, boardWidth);


            while(true) {
                try {
                    String response = in.readLine();
                    if (response.startsWith("WAITING"))
                        System.out.println(response);
                    else if (response.startsWith("GAME_ON")) {
                        System.out.println(response.substring(8));
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: " + e);
                } catch (Exception e) {
                    System.out.println("Error trying to find opponent...");
                }
            }

            String instructions = "";

            // Ready to begin game
            while (true) {
                if (in.ready()) {
                    instructions = in.readLine();

                    if (instructions.startsWith("MAKE_A_MOVE")) {
                        try {
                            displayBoard();

                            // Prompt for move
                            System.out.println("IT IS YOUR TURN, ENTER A NUMBER BETWEEN [1-9]!");
                            Scanner scanner1 = new Scanner(System.in);
                            int move = scanner1.nextInt();
                            out.println(move);

                            String valid_Move = in.readLine();

                            if (valid_Move.startsWith("INVALID_MOVE")) {
                                System.out.println("INVALID MOVE, PLEASE ENTER A NUMBER BETWEEN [1-9]!");
                                continue;
                            } else if (valid_Move.startsWith("VALID_MOVE")) {
                                // Get back coords of move
                                int x = Integer.parseInt(in.readLine());
                                int y = Integer.parseInt(in.readLine());

                                board[x][y] = String.format("[%s]", playerChip);

                                displayBoard();
                                System.out.println("Opponents move, please wait!");
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    } else if (instructions.startsWith("NOT_YOUR_TURN")) {
                        System.out.print("...");
                        System.out.print("\b\b\b");
                    } else if (instructions.startsWith("OPPONENT_MOVED")) {
                        int x = Integer.parseInt(String.valueOf(instructions.charAt(15)));
                        int y = Integer.parseInt(String.valueOf(instructions.charAt(17)));

                        // Get back coords of opponents move
                        System.out.println("Opponents move -> X: " + x + " Y: " + y);
                        board[x][y] = String.format("[%s]", opponentChip);
                    } else if (instructions.startsWith("WINNER")) {
                        System.out.println("WINNER FOUND");
                        break;
                    } else if (instructions.startsWith("GAME_OVER")) {
                        System.out.print(instructions);
                        break;
                    }
                }
            }
        }
    }

    private int getChipSelection() {
        int chipSelection;
        while (true) {
            try {
                System.out.println("\n[ Enter number 1 to play as X's OR 2 to play as O's ]");

                Scanner scanner =new Scanner(System.in);
                chipSelection = scanner.nextInt();
                if (chipSelection == 1 || chipSelection == 2)
                    break;
                System.out.println(chipSelection + " is an invalid number!");
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number!");
            }
        }
        return chipSelection;
    }

    private void setupBoard(int boardHeight, int boardWidth) {
        board = new String[boardHeight][boardWidth];
        for(int i = 0; i<boardHeight; i++) {
            Arrays.fill(board[i], "[_]");
        }
    }

    private void displayBoard() {
        for(String[] arr : board) {
            for(String str : arr)
                System.out.print(str + " ");
            System.out.println();
        }
    }

    private void updateBoard(int coords) {

    }

    private void playGame() {
        String response;
        try {
            response = in.readLine();

            // Waiting for another player?

            // All players connected
            // Your opponent is x
            // Display Empty grid
            // Enter [1- 9]
            // Display new grid
            // Opponents move, please wait
            // Display new grid
//            updateBoard(5);
            // Please make your move

        } catch(Exception e) {}
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n## RULES ##");
        System.out.println("## To win the game you must get FIVE counters in a continuous row. ##\n" +
                "## Rows can be horizontal, vertical, or diagonal ##\n" + "## ENJOY! ##\n");
        System.out.println("Please Enter your NAME: ");
        String playerName = scanner.nextLine();
        TTT_Client client = new TTT_Client(playerName);
    }
}