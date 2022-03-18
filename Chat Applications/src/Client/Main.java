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
        try {
            chat(client);
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    public static void chat(Client client) throws InterruptedException{
        ClientHandler clientHandler = new ClientHandler(client);
        //Print the welcome message from the server once the connection has been established
        clientHandler.welcomeMessage();
        //Login to the server
        clientHandler.login();
        Thread.sleep(100);
        //Thread for reading messages from the server
        clientHandler.startThreadForReadingMessages();
        //Thread for sending Messages to the server
        clientHandler.startThreadForSendingMessages();
        Thread.sleep(100);
        System.out.println();
        UserInterface.menu();
    }
}
