package Client.Model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import Client.MessageConverter;

public class ClientHandler{

    private Client client;
    public static String myOwnUsername = "";

    public ClientHandler(Client client) {
        this.client = client;
    }

    /**
     * Welcome message
     */

    public void welcomeMessage() {
        try {
            String welcomeMessage = client.getIn().readLine();
            System.out.println(MessageConverter.convertMessage(welcomeMessage));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Login to the server
     */

    public void login () {
        String userName = "";
        while (!client.isConnected()){
            System.out.print("Please enter your username: >> ");
            try {
                userName = readString();
                myOwnUsername = userName;
                client.connectWithUserName(userName);
            }   catch (IllegalArgumentException | IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void startThreadForReadingMessages() {
        //Thread for reading the messages from the server
        new Thread(() -> {
            while (client.isConnected()){
                //This will read and print the messages from the server.
                try {
                    String message = client.getIn().readLine();
                    if (message != null){
                        MessageConverter.setClient(client);
                        String messageConverted = MessageConverter.convertMessage(message);
                        // Check if the message is empty strings
                        if (!messageConverted.equals("")) {
                            System.out.println(messageConverted);
                        }
                    }
                } catch (IOException e) {
                    //Check for server errors
                    System.err.println("Server got disconnected!, shutting down....");
                    System.exit(0);
                    break;
                }
            }
        }).start();
    }

    /**
     * Start a thread for sending messages to the server
     */

    public void startThreadForSendingMessages () {
        UserInterface userInterface = new UserInterface(client);
        //Thread for sending messages to the server
        new Thread(() -> {
            while (client.isConnected()){
                userInterface.userInterface();
            }
        }).start();
    }

    /**
     * Read the user input as a String
     * @return user input
     */

    private String readString() {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
