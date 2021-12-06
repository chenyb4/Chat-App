package Server;

import java.net.*;
import java.io.*;
public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started with port "+port);
        while (true){
            clientSocket = serverSocket.accept();
            //Start message processing thread for every connected client
            messageProcessingThread();
            out.println("Welcome to the chat room!");
            out.flush();
            //For sending ping for each client
            //pingThread();
        }
    }

    public void messageProcessingThread() {
        Thread t1 = new Thread(() -> {
            try {
                out = new PrintWriter(clientSocket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pingThread() {
        Thread t2 = new Thread(() -> {

        });
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
}
