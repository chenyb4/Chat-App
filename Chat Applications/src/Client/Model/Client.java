package Client.Model;

import Client.Exception.ClientException;
import Server.MessageEncryptor;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Client {

    //Fields
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName = "";
    private boolean isConnected = false;

    //Encryption
    private PublicKey publicKey;
    private PrivateKey privateKey;
    //<Username as key, Session key as value>
    private final Map<String,SecretKey> sessionKeys = new HashMap<>();

    //Constructor
    public Client() {
        try {
            //Generate public and private keys
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Check if the client is logged in
     */

    private void checkLogin(){
        if (this.userName.equals("")) {
            throw new ClientException("Please login first!");
        }
    }

    /**
     * @param ip address
     * @param port number
     */


    public void startConnection (String ip, int port) {
        System.out.println("Client started to port "+port);
        try {
            clientSocket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException io) {
            System.err.println("Couldn't connect!, check the server");
        }
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
                    throw new ClientException("Only characters and numbers are allowed. Space is not allowed!");
                }
            }
            this.userName = userName;
            isConnected = true;
            sendMessage("CONN " + userName);
        } else {
            throw new ClientException("Username has an invalid format, the length should be higher than 2");
        }
    }

    /**
     * send a broadcast message to the server (to all available users)
     * but the message should not be null and the user is logged in
     * @param msg to be sent
     */

    public void sendBroadcastMessage (String msg) {
        checkLogin();
        if (msg == null || msg.equals("")) {
            throw new ClientException("Cannot send empty message!");
        } else {
            sendMessage("BCST " + msg);
        }
    }

    /**
     * Close the connection to the server along with PrintWriter and BufferedReader
     * @throws IOException caused by failed or interrupted io operations
     */

    public void stopConnection () throws IOException {
        isConnected = false;
        sendMessage("QUIT");
        in.close();
        out.close();
        clientSocket.close();
    }

    /**
     * Send pong to the server
     */

    public void sendPong() {
        //no condition to check, only logged-in users will receive ping from server
        sendMessage("PONG");
    }

    /**
     * View all the connected clients, users with "*" are authenticated
     */

    public void viewAllClients () {
        checkLogin();
        sendMessage("VCC");
    }

    /**
     * @param username username of the message receiver
     * @param msg the private message
     */

    public void sendPrivateMessage (String username,String msg) {
        //check if the user is logged in
        checkLogin();
        if (username.equals("")) {
            //check if the username input is correct
            throw new ClientException("Invalid username!");
        } else if (msg == null || msg.equals("")) {
            //check if the message entered is correct
            throw new ClientException("Cannot send empty message!");
        } else {
            sendMessage("PM " + username + " " + msg);
        }
    }

    /**
     * Send to the other party my public key and request for a session key
     * @param username of the receiver
     * @return session key to encrypt the messages with
     */

    public SecretKey sendSessionKeyRequest (String username){
        checkLogin();
        if (username.equals("")) {
            //check if the username input is correct
            throw new ClientException("Invalid username!");
        } else {
            //Send my public key to the receiver to get a session key
            String publicKey = MessageEncryptor.encode(this.publicKey.getEncoded());
            sendMessage("RSS " + username + " " + publicKey);
            String sessionKey;
            try {
                //OK RSS <C2 username> <encrypted session key>
                String[] split = this.in.readLine().split(" ");
                if (split[0].equals("ER04")){
                    System.err.println("The user does not exist.");
                } else if (split[0].equals("ER13")) {
                    System.err.println("You cannot send a message to yourself.");
                } else {
                    //session key on the third index
                    sessionKey = split[3];
                    //Decrypt the session key with my private key
                    String decryptedSessionKey = MessageEncryptor.decrypt(this.privateKey,sessionKey);
                    byte[] decodedKey = MessageEncryptor.decode(decryptedSessionKey);
                    //Turn the String to its original form which is the secret key
                    return new SecretKeySpec(decodedKey, 0, 32, "AES");
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.err.println("Error in sending session key request");
            }
        }
        return null;
    }

    /**
     * Send encrypted messages to the other party
     * @param username of the receiver
     * @param msg to be encrypted
     */

    public void sendEncryptedPrivateMessage (String username,String msg) {
        if (!sessionKeys.containsKey(username)){
            //Allow requesting session key only once
            SecretKey key = sendSessionKeyRequest(username);
            //Add the session key to hashmap
            //Key can return null
            if(key != null){
                sessionKeys.put(username,key);
            }
        }
        if (sessionKeys.get(username) != null){
            //Username is already checked
            if (msg == null || msg.equals("")) {
                //check if the message entered is correct
                throw new ClientException("Cannot send empty message!");
            } else {
                //Encrypt the message with the gotten session key
                String message = MessageEncryptor.encrypt(sessionKeys.get(username),msg);
                sendMessage("PME " + username + " " + message);
            }
        }
    }

    /**
     * Send a file request to a certain client
     * @param username of the receiver
     * @param filePath to be sent to the client
     */

    public void sendFileRequest (String username,String filePath) {
        //check if the user is logged in
        checkLogin();
        if (username.equals("")){
            //check if the username input is correct
            throw new ClientException("Invalid username!");
        } else if (filePath.equals("")) {
            //check if the file path input is correct
            throw new ClientException("The file path is not allowed to be an empty string");
        } else {
            sendMessage("AAFT " + username + " " + filePath);
        }
    }

    /**
     * Accept a file request
     * @param id of the file
     */

    public void acceptFileRequest (String id) {
        //check if the user is logged in
        checkLogin();
        if (id.equals("")){
            //check if the username input is correct
            throw new ClientException("The id is not allowed to be an empty string");
        } else {
            sendMessage("RAFTA " + id);
        }
    }

    /**
     * Reject a file request
     * @param id of the file
     */

    public void rejectFileRequest (String id) {
        //check if the user is logged in
        checkLogin();
        if (id.equals("")){
            //check if the username input is correct
            throw new ClientException("The id is not allowed to be an empty string");
        } else {
            sendMessage("RAFTR " + id);
        }
    }

    /**
     * create a group with provide group name
     * @param groupName the name of the group we want to create
     */

    public void createGroup (String groupName) {
        //check if the user is logged in
        checkLogin();
        if (groupName.equals("")) {
            //check if the group name input is correct
            throw new ClientException("The group's name is not allowed to be an empty string");
        } else {
            sendMessage("CG " + groupName);
        }

    }

    /**
     *  to join a group by group name
     * @param groupName the name of the group to join
     */

    public void joinGroup (String groupName) {
        //check if the user is logged in
        checkLogin();
        if (groupName.equals("")) {
            //check if the group name input is correct
            throw new ClientException("The group's name is not allowed to be an empty string");
        } else {
            sendMessage("JG " + groupName);
        }
    }

    /**
     * see all the groups that are already created
     */

    public void viewExistingGroups () {
        //check if the user is logged in
        checkLogin();
        sendMessage("VEG");
    }

    /**
     * send a message to all clients in a group
     * @param groupName the name of the group to send message to
     * @param msg the message to send to the group
     */

    public void sendMessageToGroup (String groupName,String msg) {
        checkLogin();
        if (groupName.equals("")) {
            //check if the group name input is correct
            throw new ClientException("The group's name is not allowed to be an empty string");
        } else {
            sendMessage("BCSTG " + groupName + " " + msg);
        }
    }

    /**
     * Leave from a certain group
     * @param groupName to be left
     */

    public void leaveGroup(String groupName) {
        //check if the user is logged in
        checkLogin();
        if (groupName.equals("")) {
            //check if the group name input is correct
            throw new ClientException("The group's name is not allowed to be an empty string");
        } else {
            sendMessage("LG " + groupName);
        }
    }

    /**
     * Authenticate my self with a password
     * @param password that is to be authenticated wth
     */

    public void authenticate(String password){
        //check if the user is logged in
        checkLogin();
        sendMessage("AUTH " + password);
    }

    //Getters
    public BufferedReader getIn() {
        return in;
    }

    public boolean isConnected() {
        return isConnected;
    }

    //Setters
    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
