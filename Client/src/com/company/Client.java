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
    private boolean isActive = false;

    public void startConnection (String ip, int port) throws IOException {
        System.out.println();
        //System.out.println("Connection started with ip: "+ip+ " to port: "+port);
        clientSocket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream());
        //System.out.println(in.readLine()); // Read the response from the server
        System.out.println();
    }

    /**
     * Send a message to a server with a particular command
     * @param msg to be sent to the server
     */

    private void sendMessage (String msg) {
        //Thread for sending messages
        /*Thread thread = new Thread(() -> {

        });
        thread.start();
        try {
            thread.join(); //Wait the thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        out.println(msg);
        out.flush(); // The flush method sends the messages from the print writer buffer to client.
        //readMessages(); // Get the response from the server
    }

    /**
     * Read the messages from the server
     */

    /*private void readMessages() {
        //Thread for reading messages from the server
        Thread thread = new Thread(() -> {
            try {
                System.out.println(in.readLine()); // Read the response from the server
                System.out.println();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        });
        thread.start();
        try {
            thread.join(); //Wait the thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Connect to the server with a specific username, cannot be null or empty
     * @param userName of the client
     */

    public void connectWithUserName (String userName) {
        if (userName != null && !userName.equals("")) {
            this.userName = userName;
            isActive = true;
            sendMessage("CONN "+userName+"\n");
        }
        else {
            throw new IllegalArgumentException("Invalid username!");
        }
    }

    /**
     * send a broadcast message to the server (to all available users)
     * but the message should not be null and the user is logged in
     * @param msg to be sent
     */

    public void sendBroadcastMessage (String msg) {
        if (userName.equals("")) {
            throw new IllegalStateException("Login first!");
        }
        else if (msg == null || msg.equals("")) {
            throw new IllegalArgumentException("Invalid message!");
        }
        else {
            sendMessage("BCST "+msg+"\n");
        }
    }

    /**
     * Close the connection to the server along with PrintWriter and BufferedReader
     * @throws IOException caused by failed or interrupted io operations
     */

    public void stopConnection () throws IOException {
        isActive = false;
        sendMessage("QUIT\n");
        in.close();
        out.close();
        clientSocket.close();
    }

    /**
     * Check if the user is logged in first and then if there are messages,
     * if yes then system will print it to console
     * @throws IOException caused by failed or interrupted io operations
     */

    /*public void getMessages () throws IOException {
        boolean temp = in.ready();
        if (userName.equals("")) {
            throw new IllegalStateException("Login first!");
        }
        else if (!temp) {
            System.out.println("No messages!");
        }
        else if (temp){
            System.out.println(in.readLine());
        }
    }*/

   /* public boolean isPingReceived () throws IOException {
        boolean temp = in.ready();
        if (temp){

        }
        return false;
    }*/


    /**
     * sends pong to the server
     */
    public void sendPong() {
        //no condition to check, only logged-in users will receive ping from server
        sendMessage("PONG"+"\n");
    }

    public BufferedReader getIn() {
        return in;
    }

    public boolean isActive() {
        return isActive;
    }

}
