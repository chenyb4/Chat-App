package com.company;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName = "";

    public void startConnection(String ip, int port) throws IOException {
        System.out.println();
        System.out.println("Connection started with ip: "+ip+ " to port: "+port);
        clientSocket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        Thread threadOne = new Thread(() -> {
            try {
                System.out.println(in.readLine()); // Read the response from the server
                System.out.println();
                while (true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        threadOne.start();
    }

    protected void sendMessage(String msg) throws IOException {
        Thread threadTwo = new Thread(() -> {
            out.println(msg);
            out.flush(); // The flush method sends the messages from the print writer buffer to client.
            while (true);
        });
        threadTwo.start();
        System.out.println(in.readLine()); // Get the response from the server
    }

    public void connectWithUserName(String userName) throws IOException {
        if (userName != null && !userName.equals("")) {
            this.userName = userName;
            sendMessage("CONN "+userName+"\n");
        } else {
            throw new IllegalArgumentException("Invalid username!");
        }
    }

    public void sendBroadcastMessage(String msg) throws IOException {
        if (userName != null && !userName.equals("")){
            sendMessage("BCST "+msg+"\n");
        } else {
            throw new IllegalArgumentException("Invalid message!");
        }
    }

    public void stopConnection() throws IOException {
        if (userName.equals("")){
            throw new IllegalStateException("Please login first!");
        } else {
            sendMessage("QUIT\n");
            in.close();
            out.close();
            clientSocket.close();
            System.exit(200);
        }
    }

    //This will get the broadcast message
    //Todo: fix errors when there is no message
    public void getMessages () throws IOException {
        if (in.readLine() != null && !in.readLine().equals("")){
            System.out.println(in.readLine());
            System.out.println();
        } else {
            System.out.println("No messages!");
        }
    }
}
