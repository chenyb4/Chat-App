package com.company;

import org.junit.Assert;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        /*Server server = new Server();
        try {
            server.start(1337);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Client client  = new Client();
        String ip = "127.0.0.1";
        int port = 1337;

        client.startConnection(ip,port);

       /* try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        userMenu(client);
    }

    public static void userMenu(Client client) throws IOException {
        boolean menu = true;
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
                    System.out.print("Please enter your username: >> ");
                    userName = readString();
                    try {
                        client.connectWithUserName(userName);
                    } catch (IllegalArgumentException iae) {
                        System.err.println(iae.getMessage());
                        System.out.println();
                    }
                }
                case 2 -> {
                    System.out.print("Please enter your message: >> ");
                    message = readString();
                    try {
                        client.sendBroadcastMessage(message);
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        System.err.println(e.getMessage());
                        System.out.println();
                    }
                }
                case 3 -> System.out.println("To be implemented!");
                case 4 -> client.getMessages();
                case 0 -> {
                    try {
                        client.stopConnection();
                        menu = false;
                    } catch (IllegalStateException ise) {
                        System.err.println(ise.getMessage());
                        System.out.println();
                    }
                }
                default -> System.err.println("Error, wrong input!");
            }

        } while (menu);

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
