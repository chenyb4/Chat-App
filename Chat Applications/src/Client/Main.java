package Client;

import Client.Model.Client;
import Client.Model.ClientHandler;
import Client.Model.UserInterface;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Client client  = new Client();
        String ip = "127.0.0.1";
        int port = 1337;
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
        //clientHandler.checkForServerErrors();
        //Menu for the user
       // UserInterface.menu();
        //Thread for sending Messages to the server
        clientHandler.startThreadForSendingMessages();
    }
}
