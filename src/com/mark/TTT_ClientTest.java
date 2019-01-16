package com.mark;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.*;

public class TTT_ClientTest {

    @Before
    public void setUp() {
        try {
            Scanner InputFromConsole = new Scanner(new File("com\\mark\\Tests\\TestOne\\MockInputFromUser.txt"));

            File output = new File("com\\mark\\Tests\\TestOne\\OutputToUser.txt");
            output.getParentFile().mkdirs();
            PrintWriter outputToConsole = new PrintWriter(output);

            BufferedReader inputFromServer = new BufferedReader(new FileReader("com\\mark\\Tests\\TestOne\\MockInputFromServer.txt"));
            File serverOutput = new File("com\\mark\\Tests\\TestOne\\OutputToServer");
            output.getParentFile().mkdirs();
            PrintWriter outputToServer = new PrintWriter(serverOutput);

            TTT_Client client = new TTT_Client(InputFromConsole, outputToConsole, inputFromServer, outputToServer);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @org.junit.Test
    public void main() {
        assertEquals(1, 1);
    }
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void name() {
    }
}