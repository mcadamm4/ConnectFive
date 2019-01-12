package com.mark;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class TTT_Client {
    private final BufferedReader in;
    private final PrintWriter out;
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    private static final int WIDTH = 9;
    private static final int HEIGHT = 6;

    // Define the playing board
    static String[][] board = new String[HEIGHT][WIDTH];


    //    Construct a client and connects to the server
    public TTT_Client(String playerName, String playerChip) throws Exception {
        // Setup connection to server
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("Connection status: " + socket.isConnected());

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            while(true) {
                out.println(playerName);
                out.println(playerChip);

                System.out.println(in.readLine());
                Scanner scanner = new Scanner(System.in);
                String blah = scanner.nextLine();
            }
            // Response can be "All players connected" OR "WAITING FRO ANOTHER PLAYER"
        }
    }

    private void setupBoard() {
        for(int i = 0; i<HEIGHT; i++) {
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
                "## Rows can be horizontal, vertical, or diagonal ##\n" + "## ENJOY! ##\n\n");
        System.out.println("Please Enter your NAME: ");
        String playerName = scanner.nextLine();
        System.out.println("You have entered " + playerName + "\n");

        System.out.println("Please choose X's or O's: ");
        playerChip = scanner.nextLine();

        System.out.println("You have entered " + playerChip + "\n");
        TTT_Client client = new TTT_Client(playerName, playerChip);
    }
}