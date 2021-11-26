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
        chat(client);
    }

    public static void menu() {
        System.out.println("Please enter your message and hit enter to send the message to other people!");
        System.out.println("If you want to quit, please enter \"-Q\".");
        System.out.println("If you want to see the menu again, please enter \"-?\".");
    }

    public static void chat(Client client) {

        try {
            String welcomeMessage = client.getIn().readLine();
            System.out.println(Helper.convertMessage(welcomeMessage));
        } catch (IOException e) {
            e.printStackTrace();
        }

        login(client);

        //Thread for reading the messages from the server
        Thread t1 = new Thread(() -> {
            while (client.isActive()){
                try {
                    String temp = "";
                    boolean messageReceivedFromTheServer = client.getIn().ready();
                    if (messageReceivedFromTheServer){
                        temp = client.getIn().readLine();
                        if (temp != null){
                            if(temp.equals("PING")){
                                client.sendPong();
                            } else {
                                System.out.println(Helper.convertMessage(temp));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        menu();

        //Thread for sending messages to the server
        Thread t2 = new Thread(() -> {
            while (client.isActive()){
                String userInput = readString();
                switch (userInput){
                    case "-Q" -> {
                        try {
                            client.stopConnection();
                            System.out.println("You have exited the chat room.");
                        } catch (IllegalStateException | IOException ise) {
                            System.err.println(ise.getMessage());
                            System.out.println();
                        }
                    }
                    case "-?" -> menu();
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
        });
        t2.start();
    }

    public static void login (Client client) {
        String userName = "";
        while (!client.isActive()){
            System.out.print("Please enter your username: >> ");
            try {
                userName = readString();
                client.connectWithUserName(userName);
                //System.out.println(client.getIn().readLine());
            }   catch (IllegalArgumentException iae) {
                System.err.println(iae.getMessage());
                System.out.println();
            }
        }
    }

    public static String readString() {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
