package Server.Model;

import Server.FileTransfer.FileServer;
import Server.FileTransfer.Transfer;
import Server.PasswordHasher;
import Server.FileChecker;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private Socket clientSocket;
    public PrintWriter out;
    public BufferedReader in;
    private String userName = "";
    private String password = "";
    private boolean isAuth = false;
    private boolean isConnected = false;
    private boolean receivedPong = false;

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
        //This array should have the length of the file size
        byte[] contents = new byte[(int) transfer.getFile().length()];
        try {
            Socket s = fileServer.getFileClientSocket();
            InputStream is = s.getInputStream();
            FileOutputStream fo = new FileOutputStream(placeToStore);
            is.read(contents,0,contents.length);
            fo.write(contents,0, contents.length);
            String checksumToBeChecked = FileChecker.getFileChecksum(placeToStore);
            System.out.println(checksumToBeChecked);
            if (FileChecker.compareChecksum(transfer.getChecksum(),checksumToBeChecked)) {
                System.out.println("\u001B[32m"+"File: [ "+ placeToStore +" ] was stored successfully"+"\u001B[0m");
                out.println("File is stored in: " + placeToStore);
                out.flush();
            } else {
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

    public void setPassword(String password) {
        this.password = PasswordHasher.toHash(password);
    }

    //To String
    @Override
    public String toString() {
        return userName + " " + isAuthenticated() + ",";
    }
}
