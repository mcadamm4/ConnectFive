package com.mark;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
public class CF_ClientTest {
    private CF_Client client;
    private String BASE_PATH = "C:\\Users\\Marky\\IdeaProjects\\ConnectFive\\src\\com\\mark\\Tests";

    private Scanner inputFromConsole;
    private BufferedReader inputFromServer;

    private PrintWriter outputToConsole;
    private PrintWriter outputToServer;

    // UTILITY METHODS
    private void setupClientParams(String mockUserInputFile, String outputToUserFile, String mockServerInputFile, String outputToServerFile) {
        try {
            inputFromConsole = new Scanner(new File(BASE_PATH + mockUserInputFile));
            File output = new File(BASE_PATH + outputToUserFile);
            output.getParentFile().mkdirs();
            outputToConsole = new PrintWriter(output);
            inputFromServer = new BufferedReader(new FileReader(BASE_PATH + mockServerInputFile));
            File serverOutput = new File(BASE_PATH + outputToServerFile);
            output.getParentFile().mkdirs();
            outputToServer = new PrintWriter(serverOutput);
        } catch (FileNotFoundException fnfe) {
            System.out.println("Test file missing!!\n" + fnfe);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private String retrieveLastOutputToUser(String filePath) {
        String lastLine = "";
        try {
            Scanner programOutput = new Scanner(new File(BASE_PATH + filePath));

            while (programOutput.hasNextLine())
            {
                lastLine = programOutput.nextLine();
            }
        }  catch (FileNotFoundException fnfe) {
            System.out.println("Output file missing!!\n" + fnfe);
        }
        return lastLine;
    }

    // ----------------------------------------
    // Keep test cases isolated from one another and focus on one piece of functionality
    // ----------------------------------------

    @org.junit.Test
    public void ValidMoveAndBoardUpdateTest() {
        //Arrange
        String mockUserInputFile = "\\ValidMoveAndBoardUpdateTest\\Input\\MockInputFromUser.txt";
        String outputToUserFile = "\\ValidMoveAndBoardUpdateTest\\Output\\OutputToUser.txt";
        String mockServerInputFile = "\\ValidMoveAndBoardUpdateTest\\Input\\MockInputFromServer.txt";
        String outputToServerFile = "\\ValidMoveAndBoardUpdateTest\\Output\\OutputToServer.txt";
        setupClientParams(mockUserInputFile, outputToUserFile, mockServerInputFile, outputToServerFile);

        // Act
        client = new CF_Client(inputFromConsole, outputToConsole, inputFromServer, outputToServer);
        String[][] board = client.getBoard();
        // Hard coded coords that match with what would be returned from the server
        String checkDropLocation = String.valueOf(board[5][4].charAt(1));

        // Assert
        assertEquals("X", checkDropLocation);
    }

    @org.junit.Test
    public void InvalidMoveTest() {
        //Arrange
        String mockUserInputFile = "\\InvalidMoveTest\\Input\\MockInputFromUser.txt";
        String outputToUserFile = "\\InvalidMoveTest\\Output\\OutputToUser.txt";
        String mockServerInputFile = "\\InvalidMoveTest\\Input\\MockInputFromServer.txt";
        String outputToServerFile = "\\InvalidMoveTest\\Output\\OutputToServer.txt";
        setupClientParams(mockUserInputFile, outputToUserFile, mockServerInputFile, outputToServerFile);

        // Act
        client = new CF_Client(inputFromConsole, outputToConsole, inputFromServer, outputToServer);
        String lastLine = retrieveLastOutputToUser(outputToUserFile);

        // Assert - Make sure the counter has not been dropped & player was notified
        assertEquals("INVALID MOVE, PLEASE ENTER A NUMBER BETWEEN [1-9]!", lastLine);
    }

    @org.junit.Test
    public void youWinTest() {
        //Arrange
        String mockUserInputFile = "\\YouWinTest\\Input\\MockInputFromUser.txt";
        String outputToUserFile = "\\YouWinTest\\Output\\OutputToUser.txt";
        String mockServerInputFile = "\\YouWinTest\\Input\\MockInputFromServer.txt";
        String outputToServerFile = "\\YouWinTest\\Output\\OutputToServer.txt";
        setupClientParams(mockUserInputFile, outputToUserFile, mockServerInputFile, outputToServerFile);

        //Act
        client = new CF_Client(inputFromConsole, outputToConsole, inputFromServer, outputToServer);
        String lastLine = retrieveLastOutputToUser(outputToUserFile);

        //Assert
        boolean bool = lastLine.contains("CONGRATULATIONS");
        assertTrue(bool);
    }

    @org.junit.Test
    public void opponentMovedTest() {
        //Arrange
        String mockUserInputFile = "\\OpponentMovedTest\\Input\\MockInputFromUser.txt";
        String outputToUserFile = "\\OpponentMovedTest\\Output\\OutputToUser.txt";
        String mockServerInputFile = "\\OpponentMovedTest\\Input\\MockInputFromServer.txt";
        String outputToServerFile = "\\OpponentMovedTest\\Output\\OutputToServer.txt";
        setupClientParams(mockUserInputFile, outputToUserFile, mockServerInputFile, outputToServerFile);

        //Act
        client = new CF_Client(inputFromConsole, outputToConsole, inputFromServer, outputToServer);
        String[][] board = client.getBoard();
        String opponentMove = String.valueOf(board[3][4].charAt(1));

        //Assert
        assertEquals("O", opponentMove);
    }

    @org.junit.Test
    public void youLoseTest() {
        //Arrange
        String mockUserInputFile = "\\YouLoseTest\\Input\\MockInputFromUser.txt";
        String outputToUserFile = "\\YouLoseTest\\Output\\OutputToUser.txt";
        String mockServerInputFile = "\\YouLoseTest\\Input\\MockInputFromServer.txt";
        String outputToServerFile = "\\YouLoseTest\\Output\\OutputToServer.txt";
        setupClientParams(mockUserInputFile, outputToUserFile, mockServerInputFile, outputToServerFile);

        //Act
        client = new CF_Client(inputFromConsole, outputToConsole, inputFromServer, outputToServer);
        String[][] board = client.getBoard();
        String opponentMove = String.valueOf(board[3][4].charAt(1));
        String lastLine = retrieveLastOutputToUser(outputToUserFile);
        boolean bool = lastLine.contains("YOU LOSE!!");

        //Assert
        assertEquals("O", opponentMove);
        assertTrue(bool);
    }
    @org.junit.Test
    public void opponentQuitTest() {
        //Arrange
        String mockUserInputFile = "\\OpponentQuitTest\\Input\\MockInputFromUser.txt";
        String outputToUserFile = "\\OpponentQuitTest\\Output\\OutputToUser.txt";
        String mockServerInputFile = "\\OpponentQuitTest\\Input\\MockInputFromServer.txt";
        String outputToServerFile = "\\OpponentQuitTest\\Output\\OutputToServer.txt";
        setupClientParams(mockUserInputFile, outputToUserFile, mockServerInputFile, outputToServerFile);

        //Act
        client = new CF_Client(inputFromConsole, outputToConsole, inputFromServer, outputToServer);

        String lastLine = retrieveLastOutputToUser(outputToUserFile);
        boolean bool = lastLine.contains("Your opponent has quit the game!!");

        //Assert
        assertTrue(bool);
    }
    @org.junit.Test
    public void gameOverTest() {
        //Arrange
        String mockUserInputFile = "\\GameOverTest\\Input\\MockInputFromUser.txt";
        String outputToUserFile = "\\GameOverTest\\Output\\OutputToUser.txt";
        String mockServerInputFile = "\\GameOverTest\\Input\\MockInputFromServer.txt";
        String outputToServerFile = "\\GameOverTest\\Output\\OutputToServer.txt";
        setupClientParams(mockUserInputFile, outputToUserFile, mockServerInputFile, outputToServerFile);

        //Act
        client = new CF_Client(inputFromConsole, outputToConsole, inputFromServer, outputToServer);

        String lastLine = retrieveLastOutputToUser(outputToUserFile);
        boolean bool = lastLine.contains("You have quit the game!!");

        //Assert
        assertTrue(bool);
    }
    @org.junit.Test
    public void stalemateTest() {
        //Arrange
        String mockUserInputFile = "\\StalemateTest\\Input\\MockInputFromUser.txt";
        String outputToUserFile = "\\StalemateTest\\Output\\OutputToUser.txt";
        String mockServerInputFile = "\\StalemateTest\\Input\\MockInputFromServer.txt";
        String outputToServerFile = "\\StalemateTest\\Output\\OutputToServer.txt";
        setupClientParams(mockUserInputFile, outputToUserFile, mockServerInputFile, outputToServerFile);

        //Act
        client = new CF_Client(inputFromConsole, outputToConsole, inputFromServer, outputToServer);

        String lastLine = retrieveLastOutputToUser(outputToUserFile);
        boolean bool = lastLine.contains("The board is full!! Nobody Wins!!");

        //Assert
        assertTrue(bool);
    }

    @After
    public void tearDown() throws Exception {
        inputFromConsole.close();
        outputToConsole.close();

        inputFromServer.close();
        outputToServer.close();
    }
}