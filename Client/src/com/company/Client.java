package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName = "";
    //private volatile boolean active = true;
    //private List <String> allMessages = new ArrayList<>();

    public void startConnection (String ip, int port) throws IOException {
        System.out.println();
        System.out.println("Connection started with ip: "+ip+ " to port: "+port);
        clientSocket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream());
        System.out.println("<< "+in.readLine()); // Read the response from the server
        System.out.println();
    }

    protected void sendMessage (String msg) {
        //Thread for sending messages
        Thread thread = new Thread(() -> {
            out.println(msg);
            out.flush(); // The flush method sends the messages from the print writer buffer to client.
            //while (active) Thread.onSpinWait();
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        readMessages(); // Get the response from the server
    }

    protected void readMessages() {
        //Thread for reading messages from the server
        Thread thread = new Thread(() -> {
            try {
                System.out.println("<< " + in.readLine()); // Read the response from the server
                System.out.println();
                //while (active) Thread.onSpinWait();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connectWithUserName (String userName) {
        if (userName != null && !userName.equals("")) {
            this.userName = userName;
            sendMessage("CONN "+userName+"\n");
        } else {
            throw new IllegalArgumentException("Invalid username!");
        }
    }

    public void sendBroadcastMessage (String msg) {
        if (msg == null || msg.equals("")) {
            throw new IllegalArgumentException("Invalid message!");
        } else if (userName.equals("")) {
            throw new IllegalStateException("Not logged in!");
        } else {
            sendMessage("BCST "+msg+"\n");
        }
    }

    public void stopConnection () throws IOException {
        if (userName.equals("")){
            throw new IllegalStateException("Please login first!");
        } else {
            sendMessage("QUIT\n");
            //active = false;
            in.close();
            out.close();
            clientSocket.close();
        }
    }

    //This will get the broadcast message
    //Todo: fix errors when there is no message
    public void getMessages () throws IOException {
        if (in.readLine() != null && !in.readLine().equals("")){
            System.out.println("<< "+in.readLine());
            System.out.println();
        } else {
            System.out.println("No messages!");
        }
    }
}
