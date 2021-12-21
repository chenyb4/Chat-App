package Server.Model;

import Server.PasswordHasher;
import Server.FileChecker;
import java.io.*;
import java.net.Socket;

public class Client {

    private Socket fileTransferSocket;
    private Socket clientSocket;
    public PrintWriter out;
    public BufferedReader in;
    private String userName = "";
    private String password = "";
    private boolean isAuth = false;
    private boolean isConnected = false;
    private boolean receivedPong = false;
    //Sending files
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

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

    public String getPassword() {
        return password;
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

    public void setFileTransferSocket(Socket fileTransferSocket) {
        this.fileTransferSocket = fileTransferSocket;
    }

    //Methods
    public void initializeStreams() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            dataInputStream = new DataInputStream(fileTransferSocket.getInputStream());
            dataOutputStream = new DataOutputStream(fileTransferSocket.getOutputStream());
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

    public void sendFile(String path) {
        int bytes = 0;
        try {
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            // send file size
            dataOutputStream.writeLong(file.length());
            byte[] buffer = new byte[4*1024];
            while ((bytes=fileInputStream.read(buffer))!=-1){
                dataOutputStream.write(buffer,0,bytes);
                dataOutputStream.flush();
            }
            fileInputStream.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void receiveFile(String fileName){
        int bytes = 0;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            if (FileChecker.checkFile(new FileInputStream(fileName)) == null){
                System.err.println("Something is wrong with the file");
            } else {

                long size = dataInputStream.readLong(); // read file size
                byte[] buffer = new byte[4*1024];
                while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                    fileOutputStream.write(buffer,0,bytes);
                    size -= bytes;  // read upto file size
                }
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
