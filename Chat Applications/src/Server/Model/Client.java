package Server.Model;

import Server.FileTransfer.FileServer;
import Server.FileTransfer.Transfer;
import Server.FileChecker;
import java.io.*;

import Server.MessageEncryptor;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Client {

    private Socket clientSocket;
    public PrintWriter out;
    public BufferedReader in;
    private String userName = "";
    private String password = "";
    private boolean isAuth = false;
    private boolean isConnected = false;
    private boolean receivedPong = false;
    private boolean active = false;

    //Encryption
    private PrivateKey privateKey;
    private PublicKey publicKey;

    //Constructors
    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
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
                //File is clear
                System.out.println("\u001B[32m"+"File: [ "+ placeToStore +" ] was stored successfully"+"\u001B[0m");
                out.println("File is stored in: " + placeToStore);
                out.flush();
            } else {
                //File is corrupted
                System.out.println("\u001B[32m"+"File: [ "+ placeToStore +" ] was corrupted during transmission"+"\u001B[0m");
                out.println("File is corrupted");
                out.flush();
            }
        } catch (IOException io) {
            System.err.println(io.getMessage());
            System.err.println("Error in receiving file");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void decryptReceivedMessage(String message) {
        //Decrypt the message with my private key
        String decryptedMessage = MessageEncryptor.decrypt(privateKey,message);
        out.println(decryptedMessage);
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

    //Getters
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

    public String getPassword() {
        return password;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public boolean isActive() {
        return active;
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

    public void setActive(boolean active) {
        this.active = active;
    }

    //To String
    @Override
    public String toString() {
        return userName + " " + isAuthenticated() + ",";
    }
}
