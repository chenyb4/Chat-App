package Server;

import Client.Client;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server {

    //Fields
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ArrayList<Client> clients = new ArrayList<>();
    private final boolean SHOULD_PING = false;

    //Commands
    private final String CMD_CONN = "CONN"; //Login
    private final String CMD_BCST = "BCST"; //Broadcast message
    private final String CMD_PONG = "PONG"; //Pong to server
    private final String CMD_QUIT = "QUIT"; //Quit from server
    private final String CMD_VCC = "VCC"; //View connected clients
    private final String CMD_PM = "PM"; //Send private message
    private final String CMD_CG = "CG"; //Create a group
    private final String CMD_JG = "JG"; //Join a group
    private final String CMD_VEG = "VEG"; //View existing groups
    private final String CMD_BCSTG = "BCSTG"; //Broadcast message to a group
    private final String CMD_AUTH = "AUTH"; //Authenticate with password
    private final String CMD_LG = "LG"; //Leave a group

    //Methods
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Starting server version 1.2 with port " + port + ".");
        System.out.println("Press 'control-C' to quit the server.");
        try {
            while (true) {
                clientSocket = serverSocket.accept();
                //Start message processing thread for every connected client
                messageHandlerThread();
                //Start pining thread for each client
                //pingThread();
            }
        } finally {
            serverSocket.close();
        }
    }

    public void messageHandlerThread() {
        new Thread(() -> {
            Client client = new Client();
            streamHandler();
            clients.add(client);
            sendMessageToClient(client,"INFO welcome to chat room");
            while (true){
                try {
                    String temp;
                    while((temp = in.readLine()) != null && !temp.equals("")) {
                        clientInput(client,temp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void streamHandler () {
        try {
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pingHandlerThread () {
        Thread pingHandler = new Thread(() -> {
            //sendMessage("PING");
        });
        pingHandler.start();
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public void sendMessageToClient(Client client,String msg) {
        if (clients.contains(client)){
            System.out.println(">> [" + client.getUserName() + "] " + msg);
            out.println(msg);
            out.flush();
        } else {
            System.out.println("Skipped send (" + msg + "): client not active any more");
            clients.remove(client);
        }
    }

    public void stats () {
        System.out.println("Total number of clients: " + clients.size());
        ArrayList<Client> connectedClients = new ArrayList<>();
        for (Client c: clients) {
            if (c.isConnected()){
                connectedClients.add(c);
            }
        }
        System.out.println("Number of connected clients: " + connectedClients.size());
    }

    public void heartBeat () {

    }

    public Message parseCommand (String command) {
        String payload = command.trim();
        String[] lineParts = command.split(" ");
        //Return the type and payload when available
        if (command.length() < 5){
            return new Message(lineParts[0]);
        } else {
            return new Message(lineParts[0],payload.substring(lineParts[0].length() + 1));
        }
    }

    public void clientInput (Client client,String input) {
        Message command = parseCommand(input);
        System.out.println("<< ["+client.getUserName() + "] " + command.getType() + " " +command.getPayload());
        switch (client.getUserName()) {
            case "undefined" -> {
                checkIfLoggedIn(command,client);
            }
            //At this point, we know that the user is logged in
            default -> {
                switch (command.getType()){
                    case CMD_QUIT -> quitCommand(client);
                    case CMD_BCST -> broadcastMessageCommand(client,command);
                    case CMD_PONG -> client.setReceivedPong(true);
                    //Other commands than these are an error
                    default -> sendMessageToClient(client,"ER00 Unknown command");
                }
            }
        }

    }

    public void checkIfLoggedIn (Message command, Client client) {
        if (!command.getType().equals(CMD_CONN)){
            sendMessageToClient(client,"ER03 Please log in first");
        } else if (!validUsernameFormat(command.getPayload())){
            sendMessageToClient(client, "ER02 Username has an invalid format (only characters and numbers are allowed. Space is not allowed)");
        } else if (userExists(command.getPayload())){
            sendMessageToClient(client, "ER01 User already logged in");
        } else {
            //The user provided correct information
            setUser(client,command);
        }
    }

    public void quitCommand (Client client) {
        sendMessageToClient(client,"OK Goodbye");
        clients.remove(client);
    }

    public void broadcastMessageCommand (Client client,Message command) {
        for (Client c:clients) {
            if (c.isConnected() && !c.getUserName().equals(client.getUserName())){
                sendMessageToClient(c,CMD_BCST + " " + client.getUserName() + " " + command.getPayload());
            }
        }
        sendMessageToClient(client,"OK " + CMD_BCST + " " + command.getPayload());
    }

    public boolean userExists (String username) {
        boolean result = false;
        for (Client c:clients) {
            if (c.getUserName().equals(username)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean validUsernameFormat (String username) {
        boolean result = false;
        for (int i = 0; i < username.length(); i++) {
            if (Character.isLetterOrDigit(username.charAt(i))) {
                result = true;
            }
        }
        return result;
    }

    public void setUser (Client client,Message command) {
        client.setUserName(command.getPayload());
        client.setConnected(true);
        client.setReceivedPong(false);
        sendMessageToClient(client,"OK "+command.getPayload());
        stats();
        if (SHOULD_PING){
            heartBeat();
        }
    }

}
