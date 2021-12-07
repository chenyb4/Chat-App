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
            out.println("INFO welcome to chat room");
            out.flush();
            //For sending ping for each client
            //pingThread();
        }
    }

    public void messageProcessingThread() {
        Thread clientHandler = new Thread(() -> {
            try {
                out = new PrintWriter(clientSocket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        clientHandler.start();
        try {
            clientHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pingThread() {
        Thread pingHandler = new Thread(() -> {

        });
        pingHandler.start();
        try {
            pingHandler.join();
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

    public void sendMessage (String msg) {
        out.println(msg);
        out.flush();
    }
}
