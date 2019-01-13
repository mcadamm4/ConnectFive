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
    private final BufferedReader in;
    private final PrintWriter out;
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    // Define the playing board
    private String[][] board;


    //    Construct a client and connects to the server
    public TTT_Client(String playerName) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int chipSelection;

        // Setup connection to server
        try (Socket socket = new Socket(HOST, PORT)) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connection status: " + socket.isConnected());

            // Send User name to server
            out.println(playerName);

            chipSelection = getChipSelection();
            // Send user chip selection to server
            out.println(chipSelection);

            // Output players assigned chip
            String chipResponse = in.readLine();
            System.out.println(chipResponse);


            // Setup the playing board
            int boardHeight = Integer.parseInt(in.readLine());
            int boardWidth = Integer.parseInt(in.readLine());
            setupBoard(boardHeight, boardWidth);

            while(true) {
                String response = in.readLine();
                if(response.startsWith("WAITING"))
                    System.out.println(response);
                else if(response.startsWith("GAME ON")) {
                    System.out.println(response);
                    while(true) {
                        // Prompt for move
                        Scanner scanner1 = new Scanner(System.in);
                        int move = scanner1.nextInt();
                        out.println(move);
                        // Display new board

                        // Wait for opponent
                        // Display new board
                        // if (win | draw | lose) -> break x2
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
        String playerChip = "";

        System.out.println("\n## RULES ##");
        System.out.println("## To win the game you must get FIVE counters in a continuous row. ##\n" +
                "## Rows can be horizontal, vertical, or diagonal ##\n" + "## ENJOY! ##\n");
        System.out.println("Please Enter your NAME: ");
        String playerName = scanner.nextLine();

        TTT_Client client = new TTT_Client(playerName);
    }
}