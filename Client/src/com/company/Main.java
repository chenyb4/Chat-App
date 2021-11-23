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
        //userMenu(client);
        chat(client);
    }

    /*public static void userMenu(Client client) throws IOException {
        boolean show = true;
        String userInput = "";
        String userName = "";
        String message = "";
        showMenu();



        while (show){


            if(isLoggedIn==true){
                if(client.getIn().readLine().equals("PING")){
                    client.sendPong();
                }
            }


            userInput = readString();
            switch (userInput) {
                case "1" -> {
                    System.out.print("Please enter your username: >> ");
                    userName = readString();
                    try {
                        client.connectWithUserName(userName);
                        isLoggedIn=true;
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
    }*/

    public static void showMenu() {
        System.out.println("Please enter your message and hit enter to send the message to other people!");
        System.out.println("If you want to quit, please enter Q.");
        System.out.println("If you want to send PONG, please enter P.");
        System.out.println("If you want to see the menu again, please enter ?.");

    }

    public static void chat(Client client) throws IOException {
        boolean show = true;
        String userInput = "";
        String userName = "";

        System.out.print("Please enter your username: >> ");
        try {
            userName = readString();
            client.connectWithUserName(userName);
        }   catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.out.println();
        }

        System.out.println("Welcome to the chat room, "+userName+"!");
        showMenu();

        while (show) {
            Thread t1 = new Thread(() -> {
                while (true){
                    boolean messageReceivedFromTheServer = false;
                    try {
                        messageReceivedFromTheServer = client.getIn().ready();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (messageReceivedFromTheServer){
                        try {
                            if(client.getIn().readLine().equals("PING")){
                                client.sendPong();
                            } else {
                                System.out.println(client.getIn().readLine());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t1.start();

            userInput = readString();

            switch (userInput){
                case "Q" -> {
                    try {
                        client.stopConnection();
                        show = false;
                    } catch (IllegalStateException ise) {
                        System.err.println(ise.getMessage());
                        System.out.println();
                    }
                }
                case "P" -> client.sendPong();
                case "?" -> showMenu();
                default -> {
                    try {
                        client.sendBroadcastMessage(userInput);
                    }   catch (IllegalArgumentException | IllegalStateException e) {
                        System.err.println(e.getMessage());
                        System.out.println();
                    }
                }
            }
        }
    }

    public static String readString() {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
