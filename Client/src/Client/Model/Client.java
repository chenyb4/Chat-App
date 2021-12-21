package Client.Model;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName = "undefined";
    private boolean isAuth = false;
    private boolean isConnected = false;
    private boolean receivedPong = false;

    /**
     * @param ip address
     * @param port number
     * @throws IOException caused by Stream either input or output
     */

    public void startConnection (String ip, int port) throws IOException {
        System.out.println("Client started to port "+port);
        clientSocket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream());
    }

    /**
     * Send a message to a server with a particular command
     * @param msg to be sent to the server
     */

    private void sendMessage (String msg) {
        out.println(msg);
        out.flush(); // The flush method sends the messages from the print writer buffer to client.
    }

    /**
     * Connect to the server with a specific username, cannot be null or empty
     * @param userName of the client
     */

    public void connectWithUserName (String userName) throws IOException {
        if (userName != null && userName.length() > 2) {
            for (int i = 0; i < userName.length(); i++) {
                if (!Character.isLetterOrDigit(userName.charAt(i))){
                    throw new IllegalArgumentException("Only characters and numbers are allowed. Space is not allowed!");
                }
            }
            this.userName = userName;
            isConnected = true;
            sendMessage("CONN "+userName+"\n");
        }
        else {
            throw new IllegalArgumentException("Username has an invalid format, the length should be higher than 2");
        }
    }

    /**
     * send a broadcast message to the server (to all available users)
     * but the message should not be null and the user is logged in
     * @param msg to be sent
     */

    public void sendBroadcastMessage (String msg) {
        if (userName.equals("")) {
            throw new IllegalStateException("Please login first");
        }
        //This is not necessary
        else if (msg == null || msg.equals("")) {
            throw new IllegalArgumentException("Cannot send empty message!");
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
        isConnected = false;
        sendMessage("QUIT\n");
        in.close();
        out.close();
        clientSocket.close();
    }

    /**
     * Send pong to the server
     */

    public void sendPong() {
        //no condition to check, only logged-in users will receive ping from server
        sendMessage("PONG"+"\n");
    }

    /**
     * View all the connected clients, users with "*" are authenticated
     */

    public void viewAllClients () {
        sendMessage("VCC");
    }

    /**
     *
     * @param username username of the message receiver
     * @param msg the private message
     */
    public void sendPrivateMessage (String username,String msg) {
        //check if the user is logged in
        if (userName.equals("")) {
            throw new IllegalStateException("Please login first");
        }


        if (username.equals("")) {
            //check if the username input is correct
            throw new IllegalStateException("the username is not allowed to be an empty string");
        } else if (msg == null || msg.equals("")) {
            //check if the message entered is correct
            throw new IllegalArgumentException("Cannot send empty message!");
        }
        else {
            sendMessage("PM "+username+" "+msg+"\n");
        }
    }

    /**
     * create a group with provide group name
     * @param groupName the name of the group we want to create
     */
    public void createGroup (String groupName) {
        //check if the user is logged in
        if (userName.equals("")) {
            throw new IllegalStateException("Please login first");
        }

        if (groupName.equals("")) {
            //check if the username input is correct
            throw new IllegalStateException("the group's name is not allowed to be an empty string");
        } else {
            sendMessage("CG " + groupName + "\n");
        }

    }

    public void joinGroup (String groupName) {

    }

    /**
     * View all existing groups
     */

    public void viewExistingGroups () {

    }

    /**
     * Send a message to a certain group
     */

    public void sendMessageToGroup () {

    }

    /**
     * Leave a group
     */

    public void leaveGroup() {

    }


    //Getters
    public BufferedReader getIn() {
        return in;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public boolean isReceivedPong() {
        return receivedPong;
    }

    //Setters
    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public void setReceivedPong(boolean receivedPong) {
        this.receivedPong = receivedPong;
    }
}
