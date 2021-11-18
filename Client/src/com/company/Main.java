package com.company;

import org.junit.Test;

import java.io.IOException;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class Main {

    public static void main(String[] args) throws IOException {
        /*Server server = new Server();
        try {
            server.start(1337);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Client client  = new Client();
        client.startConnection("127.0.0.1",1337);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        userInterface(client);
    }

    public static void userInterface (Client client) throws IOException {
        int userInput = -1;
        String userName = "";
        String message = "";

        do {
            System.out.println("Please select one of the commands below: ");
            System.out.println("1) Connect with username");
            System.out.println("2) Send BCST message");
            System.out.println("3) Pong");
            System.out.println("4) Read messages if available");
            System.out.println("0) Quit");
            System.out.println();
            System.out.print("Enter your choice: ");
            userInput = readInt();

            switch (userInput) {
                case 1 -> {
                    System.out.print("Please enter your username: ");
                    userName = readString();
                    client.connectWithUserName(userName);
                    System.out.println();
                }
                case 2 -> {
                    System.out.print("Please enter your message: ");
                    message = readString();
                    client.sendBroadcastMessage(message);
                    System.out.println();
                }
                case 4 -> client.getMessages();
                case 0 -> client.stopConnection();
                default -> System.err.println("Error, wrong input!");
            }

        } while (userInput != 0);

    }

    public static int readInt() {
        Scanner sc = new Scanner(System.in);
        return sc.nextInt();
    }

    public static String readString() {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
