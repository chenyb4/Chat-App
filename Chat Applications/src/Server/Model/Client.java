package Server.Model;

import Server.FileTransfer.FileServer;
import Server.FileTransfer.Transfer;
import Server.FileChecker;
import java.io.*;

import Server.MessageEncryptor;
import Server.PasswordHasher;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private Socket clientSocket;
    public PrintWriter out;
    public BufferedReader in;
    private String userName = "";
    private String password = "";
    private int id = 0;
    private boolean isAuth = false;
    private boolean isConnected = false;
    private boolean receivedPong = false;
    private boolean active = false;

    //Encryption
    private PrivateKey privateKey;
    private PublicKey publicKey;
    //Client with their session key;
    private final Map<Client, SecretKey> sessionKeys = new HashMap<>();

    //Constructors
    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public Client(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     * Initialize PrintWriter and BufferedReader
     */

    //Methods
    public void initializeStreams() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    /**
     * Close the streams when the client disconnects.
     */

    public void stopStreams () {
        try {
            out.close();
            in.close();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    /**
     * Receive file from the file server
     * @param placeToStore where the file will be stored
     * Implementation of checksum will also be implemented here
     */

    public void receiveFile(String placeToStore, FileServer fileServer, Transfer transfer) {
        try {
            //This array should have the length of the file size
            byte[] contents = new byte[(int) transfer.getFile().length()];
            Socket s = fileServer.getFileClientSocket();
            InputStream is = s.getInputStream();
            FileOutputStream fo = new FileOutputStream(placeToStore);
            is.read(contents,0,contents.length);
            fo.write(contents,0, contents.length);
            String checksumToBeChecked = FileChecker.getFileChecksum(placeToStore);
            //Check the file for any corruption
            if (FileChecker.compareChecksum(transfer.getChecksum(),checksumToBeChecked)) {
                //File is checked
                System.out.println("\u001B[32m"+"File: [ "+ placeToStore +" ] was stored successfully"+"\u001B[0m");
                out.println("File is stored in: " + placeToStore);
                out.flush();
            } else {
                //File is corrupted
                System.out.println("\u001B[32m"+"File: [ "+ placeToStore +" ] was corrupted during transmission"+"\u001B[0m");
                out.println("File is corrupted");
                out.flush();
            }
            s.close();
            is.close();
            fo.close();
        } catch (IOException io) {
            System.err.println(io.getMessage());
            System.err.println("Error in receiving file");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public String sendSessionKey (Client receiver,String senderPublicKey) {
        try {
            if (sessionKeys.get(receiver) == null || receiver.sessionKeys.get(this) == null){
                initializeKeys(receiver);
            }
            //Get session key with receiver's private key and sender's public key
            byte[] publicBytes = Base64.getDecoder().decode(senderPublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKeyConverted = keyFactory.generatePublic(keySpec);
            SecretKey secretKey = generateSessionKey(receiver.privateKey, this.publicKey);
            System.out.println(MessageEncryptor.encode(secretKey.getEncoded()));
            //Store the session key
            sessionKeys.putIfAbsent(receiver,secretKey);
            receiver.sessionKeys.putIfAbsent(this,secretKey);
            //Encrypt the session key with sender's public key
            return MessageEncryptor.encrypt(publicKeyConverted,MessageEncryptor.encode(secretKey.getEncoded()));
        } catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("Error in sending session key");
        }
        return null;
    }

    public void initializeKeys (Client receiver) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("DH");
            AlgorithmParameterGenerator generatorParam = AlgorithmParameterGenerator.getInstance("DH");
            generatorParam.init(1024);
            // Generate the parameters
            AlgorithmParameters algoParams = generatorParam.generateParameters();
            DHParameterSpec dhSpec = algoParams.getParameterSpec(DHParameterSpec.class);
            generator.initialize(dhSpec);
            KeyPair pair = generator.generateKeyPair();
            KeyPair pair1 = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
            receiver.publicKey = pair1.getPublic();
            receiver.privateKey = pair1.getPrivate();
        } catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("Error in initializing the keys");
        }
    }

    public void decryptReceivedMessage (Client sender,String msg) {
        String decryptedMessage = MessageEncryptor.decrypt(sessionKeys.get(sender),msg);
        out.println("PME " + sender.getUserName() + " " + sender.isAuthenticated() + " " + decryptedMessage);
        out.flush();
    }


    /**
     * Check if the client is authenticated
     * @return 1 if authenticated, otherwise return 0
     */

    public String isAuthenticated () {
        if (isAuth) {
            return "1";
        } else {
            return "0";
        }
    }

    private SecretKey generateSessionKey (PrivateKey privateKey, PublicKey publicKey) {
        //Generate a session key using sender's public key and receiver's private key
        try {
            KeyAgreement ka = KeyAgreement.getInstance("DH");
            ka.init(privateKey);
            ka.doPhase(publicKey, true);
            byte[] encodedKey = ka.generateSecret();
            return new SecretKeySpec(encodedKey, 0, 32, "AES");
        } catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("Error in generating session key");
        }
        return null;
    }

    /**
     * @param clients that are connected to the socket
     * @param password to be stored
     */

    public void authenticateClient (List<Client> clients, String password) {
        Client client = ServerHandler.getClientFormDataProvider(this);
        if (client == null){
            sendMessageToThisClient("ER19 Credentials not found on the server and cannot authenticate");
        } else if (password.length() < 6 || password.contains(" ") || password.length() > 20 || password.contains(",")) {
            sendMessageToThisClient("ER10 Password has an invalid format (no comma, no space, the password should be between 6 - 20 characters)");
        } else if (this.isAuth) {
            sendMessageToThisClient("ER11 User already authenticated");
        }
        //Compare given password with the password provided
        else if (!PasswordHasher.comparePassword(client.password,client.userName,password)) {
            sendMessageToThisClient("ER18 Incorrect password");
        } else {
            this.isAuth = true;
            this.active = true;
            for (Client c: ServerHandler.connectedClientsList(clients)) {
                if (!c.userName.equals(this.userName)) {
                    c.out.println("AUTH " + this.userName + " " + isAuthenticated());
                    c.out.flush();
                }
            }
            sendMessageToThisClient("OK AUTH " + password);
        }
    }

    /**
     * @param groups to be removed from
     */

    public void checkClientIfIdle (List<Group> groups) {
        Client client = this;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            //2 min = 120 sec
            int temp = 120;
            @Override
            public void run() {
                if (active) {
                    //Reset to 2 min
                    temp = 120;
                } else {
                    temp--;
                }
                active = false;
                if (temp == 0){
                    for (Group g:groups) {
                        g.removeClientFromGroup(client);
                        g.sendMessageToGroupMembersWhenLeft(client);
                    }
                    sendMessageToThisClient("was removed from all groups for being idle");
                    timer.cancel();
                }
            }
            //Run every second
        },0,1000);
    }

    /**
     * @param username to be set to the user
     */

    public void setUser (String username) {
        this.userName = username;
        this.isConnected = true;
        this.receivedPong = false;
        this.active = true;
        sendMessageToThisClient("OK "+username);
    }

    public void sendMessageToThisClient(String msg) {
        System.out.println(">> [" + userName + " " + isAuthenticated() + "] " + msg);
        out.println(msg);
        out.flush();
    }

    //Getters
    public boolean isConnected() {
        return isConnected;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isReceivedPong() {
        return receivedPong;
    }

    public int getId() {
        return id;
    }

    //Setters
    public void setReceivedPong(boolean receivedPong) {
        this.receivedPong = receivedPong;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    //To String
    @Override
    public String toString() {
        return userName + " " + isAuthenticated() + ",";
    }
}
