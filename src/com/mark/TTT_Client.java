package com.mark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TTT_Client {
    private BufferedReader in;
    private PrintWriter out;
    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private static int CONNECT_NUM;
    private final PrintWriter writer;
    private final Scanner scanner;

    // Define the playing board
    private String[][] board;

    private String playerChip;
    private String opponentChip;

    //    Construct a client and connects to the server
    TTT_Client(Scanner scanner, PrintWriter writer) throws Exception {
        this.scanner = scanner;
        this.writer = writer;
        getGameReady();
    }

    private void getGameReady() throws IOException {
        int chipSelection;
        writer.println("\n## RULES ##");
        writer.println("## To win the game you must get FIVE counters in a continuous row. ##\n" +
                "## Rows can be horizontal, vertical, or diagonal ##\n" + "## ENJOY! ##\n");
        writer.println("Please Enter your NAME: ");
        String playerName = scanner.nextLine();

        // Setup connection to server
        try (Socket socket = new Socket(HOST, PORT)) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            writer.println("Connection status: " + socket.isConnected());

            // Send User name to server
            out.println(playerName);

            // Send user chip preference to server - ([1 = X], [2 = O])
            chipSelection = getChipSelection();
            out.println(chipSelection);

            String chipResponse = in.readLine();
            writer.println(chipResponse);

            setupPlayersChips();

            // Setup the playing board with specified dimensions from server
            int boardHeight = Integer.parseInt(in.readLine());
            int boardWidth = Integer.parseInt(in.readLine());
            setupBoard(boardHeight, boardWidth);

            CONNECT_NUM = Integer.parseInt(in.readLine());

            waitForOpponent();
            displayBoard();
            playGame();
        } catch (Exception e) {
            writer.println("COULD NOT CONNECT TO SERVER");
        }
    }

    private void setupPlayersChips() throws IOException {
        // Read assigned chip
        playerChip = in.readLine();
        opponentChip = playerChip.equals("X") ? "O" : "X";
    }

    private void playGame() throws IOException {
        String instructions;
        while (true) {
            if (in.ready()) {
                instructions = in.readLine();

                if (instructions.startsWith("MAKE_A_MOVE")) {
                    try {
                        // Prompt for move
                        writer.println("IT IS YOUR TURN, ENTER A NUMBER BETWEEN [1-9]! - YOU ARE " + playerChip + "'s");
                        Scanner scanner1 = new Scanner(System.in);
                        int move = scanner1.nextInt();
                        out.println(move);

                        // Valid move, Invalid move or Winning move
                        String playerMoveResponse = in.readLine();

                        if (playerMoveResponse.startsWith("INVALID_MOVE")) {
                            writer.println("INVALID MOVE, PLEASE ENTER A NUMBER BETWEEN [1-9]!");
                            continue;
                        } else if (playerMoveResponse.startsWith("VALID_MOVE")) {
                            // Get back coords of move
                            int x = Integer.parseInt(in.readLine());
                            int y = Integer.parseInt(in.readLine());

                            updateBoard(x, y, playerChip);
                            displayBoard();
                            writer.println("Opponents move, please wait!\n");

                        } else if (playerMoveResponse.startsWith("WINNER")) {
                            String message = "\n  CONGRATULATIONS!! \nYou have won the game, %s!!";
                            declareWinnerLoser(playerMoveResponse, playerChip, message);
                            break;
                        }
                    } catch (Exception e) {
                        writer.println(e);
                    }

                } else if (instructions.startsWith("NOT_YOUR_TURN")) {
                    System.out.print("...");
                    System.out.print("\b\b\b");

                } else if (instructions.startsWith("OPPONENT_MOVED")) {
                    int x = Integer.parseInt(String.valueOf(instructions.charAt(15)));
                    int y = Integer.parseInt(String.valueOf(instructions.charAt(17)));

                    // Get back coords of opponents move
                    updateBoard(x, y, opponentChip);
                    displayBoard();

                } else if (instructions.startsWith("WINNER")) {
                    String message = "\n  YOU LOOOOOSE!! \n%s has won the game ya crumb!!";
                    declareWinnerLoser(instructions, opponentChip, message);
                    break;

                } else if (instructions.startsWith("GAME_OVER")) {
                    System.out.print(instructions);
                    break;
                }
            }
        }
    }

    private void declareWinnerLoser(String playerMoveResponse, String playerChip, String message) {
        int x = Integer.parseInt(String.valueOf(playerMoveResponse.charAt(7)));
        int y = Integer.parseInt(String.valueOf(playerMoveResponse.charAt(9)));
        String winnerName = playerMoveResponse.substring(11);

        updateBoard(x, y, playerChip);
        displayBoard();
        writer.println(String.format(message, winnerName));
    }

    private void waitForOpponent() {
        while(true) {
            try {
                String response = in.readLine();
                if (response.startsWith("WAITING"))
                    writer.println(response);
                else if (response.startsWith("GAME_ON")) {
                    writer.println(response.substring(8));
                    break;
                }
            } catch (NumberFormatException e) {
                writer.println("ERROR: " + e);
            } catch (Exception e) {
                writer.println("Error trying to find opponent...");
            }
        }
    }

    private void updateBoard(int x, int y, String playerChip) {
        board[x][y] = String.format("[%s]", playerChip);
    }

    private int getChipSelection() {
        int chipSelection;
        while (true) {
            try {
                writer.println("\n[ Enter number 1 to play as X's OR 2 to play as O's ]");

                Scanner scanner =new Scanner(System.in);
                chipSelection = scanner.nextInt();
                if (chipSelection == 1 || chipSelection == 2)
                    break;
                writer.println(chipSelection + " is an invalid number!");
            } catch (InputMismatchException e) {
                writer.println("Please enter a valid number!");
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
        writer.println();
        for(String[] arr : board) {
            for(String str : arr)
                writer.print(str + " ");
            writer.println();
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        PrintWriter writer = new PrintWriter(System.out, true);

        TTT_Client client = new TTT_Client(scanner, writer);
    }
}