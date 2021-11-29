package com.company;

import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    static boolean pingReceived = true;

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

        /*Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    client.getClientSocket().getOutputStream().write(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Something went wrong with the server!");
                    try {
                        client.stopConnection();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        },0,3000);*/
    }

    public static void menu() {
        System.out.println("Please enter your message and hit enter to send the message to other people!");
        System.out.println("If you want to quit, please enter Q.");
        System.out.println("If you want to see the menu again, please enter ?.");
    }

    public static void chat(Client client) {

        try {
            String welcomeMessage = client.getIn().readLine();
            System.out.println(Helper.convertMessageAndPrint(welcomeMessage));
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
                                pingReceived = true;
                            } else {
                                System.out.println(Helper.convertMessageAndPrint(temp));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (pingReceived){
                    pingReceived = false;
                } else {
                    System.err.println("Server is disconnected!");
                    client.setActive(false);
                    System.exit(400);
                }
            }
        },0,20000);

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
                    case "Q" -> {
                        try {
                            client.stopConnection();
                            System.out.println("You have exited the chat room.");
                        } catch (IllegalStateException | IOException ise) {
                            System.err.println(ise.getMessage());
                            System.out.println();
                        }
                    }
                    case "?" -> menu();
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
