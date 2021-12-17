package Server.Model;

import Server.PasswordHasher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public Client(String userName, String password) {
        this.userName = userName;
        this.password = password;
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

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    //Methods
    public void initializeStreams() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    public void stopStreams () {
        try {
            out.close();
            in.close();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    public String isAuthenticated () {
        if (isAuth) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public String toString() {
        return userName + " " + isAuthenticated() + ",";
    }
}
