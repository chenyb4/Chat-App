package Client;

import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) throws IOException {
        String ip = "127.0.0.1";
        int port = 1337;
        /*Server server = new Server();
        try {
            server.start(port);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Client client  = new Client();
        client.startConnection(ip,port);
        chat(client);
    }



    public static void chat(Client client) {
        ClientHandler clientHandler = new ClientHandler(client);
        //Print the welcome message from the server once the connection has been established
        clientHandler.welcomeMessage();
        //Login to the server
        clientHandler.login();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Thread for reading messages from the server
        clientHandler.startThreadForReadingMessages();
        //Check the server status, if its down, than the program will shut down.
        clientHandler.checkForServerErrors();
        //Menu for the user
        UserInterface.menu();
        //Thread for sending Messages to the server
        clientHandler.startThreadForSendingMessages();
    }
}
