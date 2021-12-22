package Client.Model;

import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import Client.MessageConverter;

public class ClientHandler{

    private boolean pingReceived = true;
    private Client client;

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
            e.printStackTrace();
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
                client.connectWithUserName(userName);
            }   catch (IllegalArgumentException | IOException e) {
                System.err.println(e.getMessage());
                System.out.println();
            }
        }
    }

    public void startThreadForReadingMessages() {
        //Thread for reading the messages from the server
        Thread messageHandler = new Thread(() -> {
            while (client.isConnected()){
                //This will read and print the messages from the server.
                isMessageReceived();
            }
        });
        messageHandler.start();
    }

    public void isMessageReceived() {
        try {
            String temp = "";
            boolean messageReceivedFromTheServer = client.getIn().ready();
            if (messageReceivedFromTheServer){
                temp = client.getIn().readLine();
                if (temp != null){
                    if(temp.equals("PING")){
                        //Once PING is received, PONG will be sent automatically
                        client.sendPong();
                        pingReceived = true;
                    } else {
                        System.out.println(MessageConverter.convertMessage(temp));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This will check for any server unexpected errors every 20 seconds.
     */

    public void checkForServerErrors () {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (pingReceived){
                    pingReceived = false;
                } else {
                    System.err.println("Server is disconnected!");
                    client.setConnected(false);
                    System.exit(500);
                }
            }
        },0,20000);
    }

    public void startThreadForSendingMessages () {
        UserInterface userInterface = new UserInterface(client);
        //Thread for sending messages to the server
        Thread SendingMessageHandler = new Thread(() -> {
            while (client.isConnected()){
                userInterface.userInterface();
            }
        });
        SendingMessageHandler.start();
    }

    public String readString() {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
