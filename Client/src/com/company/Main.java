package com.company;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        String ip = "127.0.0.1";
        int port = 1337;

        /*Server server = new Server();
        try {
            server.start(port);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Client client  = new Client();
        client.startConnection(ip,port);
        userMenu(client);
    }

    public static void userMenu(Client client) throws IOException {
        boolean show = true;
        String userInput = "";
        String userName = "";
        String message = "";
        showMenu();

        while (show){
            userInput = readString();
            switch (userInput) {
                case "1" -> {
                    System.out.print("Please enter your username: >> ");
                    userName = readString();
                    try {
                        client.connectWithUserName(userName);
                    }   catch (IllegalArgumentException iae) {
                        System.err.println(iae.getMessage());
                        System.out.println();
                    }
                }
                case "2" -> {
                    System.out.print("Please enter your message: >> ");
                    message = readString();
                    try {
                        client.sendBroadcastMessage(message);
                    }   catch (IllegalArgumentException | IllegalStateException e) {
                        System.err.println(e.getMessage());
                        System.out.println();
                    }
                }
                case "3" -> System.out.println("To be implemented!");
                case "4" -> {
                    try {
                        client.getMessages();
                    } catch (IllegalArgumentException | IllegalStateException e){
                        System.err.println(e.getMessage());
                    }
                }
                case "0" -> {
                    try {
                        client.stopConnection();
                        show = false;
                    } catch (IllegalStateException ise) {
                        System.err.println(ise.getMessage());
                        System.out.println();
                    }
                }
                case "?" -> showMenu();
                default -> System.err.println("Error, wrong input!");
            }
        }
    }

    public static void showMenu() {
        System.out.println("Please select one of the commands below: ");
        System.out.println("1) CONN: Connect to the server. (username) as a argument");
        System.out.println("2) BCST: Send broadcast message to all of the connected users. (message as a argument)");
        System.out.println("3) PONG: To be implemented later on...");
        System.out.println("4) Read messages");
        System.out.println("0) QUIT: Disconnect form the server");
        System.out.println("?) Show this menu");
        System.out.println();
        System.out.print("Enter your choice: ");
    }

    public static String readString() {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
