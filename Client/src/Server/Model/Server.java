package Server.Model;



import Server.Model.Client;
import Server.Model.Group;
import Server.Model.Message;

import java.net.*;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    //Fields
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private List<Client> clients = new LinkedList<>();
    private List<Group> groups = new LinkedList<>();
    private final boolean SHOULD_PING = true;

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

    /**
     * @param port number
     * @throws IOException when input or output throws an error
     */

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Starting server version 1.2 with port " + port + ".");
        System.out.println("Press 'control-C' to quit the server.");
        try {
            while (true) {
                clientSocket = serverSocket.accept();
                //Start message processing thread for every connected client
                messageHandlerThread();
            }
        } finally {
            serverSocket.close();
        }
    }

    /**
     * Start a message thread for each connected client
     */

    public void messageHandlerThread() {
        new Thread(() -> {
            Client client = new Client(clientSocket);
            client.initializeStreams();
            clients.add(client);
            sendMessageToClient(client,"INFO welcome to chat room");
            while (true){
                try {
                    String temp;
                    while((temp = client.in.readLine()) != null && !temp.equals("")) {
                        clientInput(client,temp);
                    }
                } catch (IOException e) {
                    clientDisconnection(client);
                    break;
                }
            }
        }).start();
    }

    /**
     * Send a message to the client
     * @param client to send the message to
     * @param msg to be sent
     */

    public void sendMessageToClient(Client client,String msg) {
        if (clients.contains(client)){
            System.out.println(">> [" + client.getUserName() + " " +client.isAuthenticated() + "] " + msg);
            client.out.println(msg);
            client.out.flush();
        } else {
            System.out.println("Skipped send (" + msg + "): client not active any more");
        }
    }

    /**
     * @param client who is connected to the server
     * @param input from the client (Command)
     */

    public void clientInput (Client client,String input) {
        Message command = parseCommand(input);
        String[] lineParts = null;
        if (command.getPayload().contains(" ")){
            lineParts = command.getPayload().split(" ",2);
        }
        System.out.println("<< [" + client.getUserName() + " " + client.isAuthenticated() + "] " + command.getType() + " " + command.getPayload());
        switch (client.getUserName()) {
            case "" -> checkIfLoggedIn(command,client);
            //At this point, we know that the user is logged in
            default -> {
                switch (command.getType()){
                    case CMD_QUIT -> quit(client);
                    case CMD_BCST -> sendBroadcastMessage(client,command.getPayload());
                    case CMD_PONG -> client.setReceivedPong(true);
                    case CMD_CG -> createGroup(client,command.getPayload());
                    case CMD_JG -> joinGroup(client,command.getPayload());
                    case CMD_VEG -> viewExistingGroups(client);
                    case CMD_PM -> {
                        assert lineParts != null;
                        sendPrivateMessage(client,lineParts[0],lineParts[1]);
                    }
                    case CMD_VCC -> viewConnectedClients(client);
                    case CMD_LG -> leaveGroup(client,command.getPayload());
                    case CMD_AUTH -> authenticateClient(client,command.getPayload());
                    case CMD_BCSTG -> {
                        assert lineParts != null;
                        sendMessageToGroup(client,lineParts[0],lineParts[1]);
                    }
                    //Other commands than these are an error
                    default -> sendMessageToClient(client,"ER00 Unknown command");
                }
            }
        }

    }

    /**
     * Require the current status of the server.
     */

    public void stats () {
        System.out.println("Total number of clients: " + clients.size());
        System.out.println("Number of connected clients: " + connectedClients().size());
    }

    /**
     * @return an arraylist of connected clients (logged in clients).
     */

    public ArrayList<Client> connectedClients () {
        ArrayList<Client> connectedClients = new ArrayList<>();
        for (Client c: clients) {
            if (c.isConnected()){
                connectedClients.add(c);
            }
        }
        return connectedClients;
    }

    public void heartBeat (Client client) {
        System.out.println("~~ [" + client.getUserName() + " " + client.isAuthenticated() + "] Heartbeat initiated");
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.setReceivedPong(false);
            sendMessageToClient(client,"PING");
            //Wait for 4 seconds for response
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (client.isReceivedPong()){
                System.out.println("~~ [" + client.getUserName() + " " + client.isAuthenticated() + "] Heartbeat expired - SUCCESS");
                System.out.println("~~ [" + client.getUserName() + " " + client.isAuthenticated() + "] Heartbeat initiated");
            } else {
                System.out.println("~~ [" + client.getUserName() + " " + client.isAuthenticated() + "] Heartbeat expired - FAILED");
                sendMessageToClient(client,"DCSN");
                removeClient(client);
                executorService.shutdown();
            }
        },0,11,TimeUnit.SECONDS);
    }

    /**
     * Parse the command to the desired format
     * @param command to be parsed
     * @return a new Message object
     */

    public Message parseCommand (String command) {
        //Remove any extra space
        String payload = command.trim();
        //limit the split to 1, n-1
        String[] lineParts = payload.split(" ",2);
        //Return the type and payload when available
        if (lineParts.length > 1) {
            return new Message(lineParts[0],lineParts[1]);
        } else {
            return new Message(lineParts[0]);
        }
    }

    /**
     * Leave a certain group
     * @param client who wants to leave the group
     * @param groupName to be leave from
     */

    public void leaveGroup (Client client, String groupName) {
        if (!validFormat(groupName)){
            sendMessageToClient(client,"ER06 Group name has an invalid format");
        } else if (!groupExists(groupName)){
            sendMessageToClient(client,"ER07 Group name does not exist");
        } else {
            Group group = findGroupByName(groupName);
            if (!group.checkClientInGroup(client.getUserName(),groupName)){
                sendMessageToClient(client,"ER08 Please join the group first");
            } else {
                group.removeClientFromGroup(client);
                group.sendMessageToGroupMembersWhenLeft(client);
                sendMessageToClient(client,"OK " + CMD_LG + " " + groupName);
            }
        }
    }

    /**
     * Send a direct message to a certain user
     * @param client who wants to send the message
     * @param username for the receiving user
     * @param msg to be sent
     */

    public void sendPrivateMessage (Client client,String username,String msg) {
        Client receiver = findClientByUsername(username);
        if (receiver == null){
            sendMessageToClient(client, "ER04 User does not exist");
        } else {
            sendMessageToClient(client,"OK " + CMD_PM + " " + client.getUserName() + " " + msg);
            receiver.out.println("PM " + client.getUserName() + " " +client.isAuthenticated() + " " + msg);
            receiver.out.flush();
        }
    }

    /**
     * All if statements in this method are tested and its fully function
     * @param command which contains the type of the command and the payload
     * @param client who wants to log in
     */

    public void checkIfLoggedIn (Message command, Client client) {
        if (!command.getType().equals(CMD_CONN)){
            sendMessageToClient(client,"ER03 Please log in first");
        } else if (!validFormat(command.getPayload())){
            sendMessageToClient(client, "ER02 Username has an invalid format (only characters and numbers are allowed. Space is not allowed)");
        } else if (userExists(command.getPayload())){
            sendMessageToClient(client, "ER01 User already logged in");
        } else {
            //The user provided correct information
            setUser(client,command.getPayload());
        }
    }

    /**
     * @param groupName to be checked in the group list
     * @return true if the group exists otherwise return false
     */

    public boolean groupExists (String groupName) {
        boolean result = false;
        for (Group g:groups) {
            if (g.getName().equals(groupName)){
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * @param client that wants to view all connected clients
     */

    public void viewConnectedClients (Client client) {
        if (clients.size() < 1) {
            sendMessageToClient(client,"OK " + CMD_VCC + " " + "");
        } else {
            client.out.print("OK " + CMD_VCC + " ");
            client.out.flush();
            System.out.print(">> [" + client.getUserName() + "] OK " + CMD_VCC + " ");
            for (Client c:connectedClients()) {
                client.out.print(c);
                client.out.flush();
                System.out.print(c);
            }
            //Make a new line
            client.out.println("");
            client.out.flush();
            System.out.println();
        }
    }

    /**
     * @param client that wants to view all existing groups
     */

    public void viewExistingGroups(Client client) {
        if (groups.size() < 1){
            sendMessageToClient(client,"OK " + CMD_VEG + " " + "");
        } else {
            client.out.print("OK " + CMD_VEG + " ");
            client.out.flush();
            System.out.print(">> [" + client.getUserName() + "] OK " + CMD_VEG + " ");
            for (Group g:groups) {
                client.out.print(g);
                client.out.flush();
                System.out.print(g);
            }
            //Make a new line
            client.out.println("");
            client.out.flush();
            System.out.println();
        }
    }

    /**
     * @param groupName to be found
     * @return the group by its name if found, otherwise return null
     */

    public Group findGroupByName (String groupName) {
        for (Group g:groups) {
            if (g.getName().equals(groupName)){
                return g;
            }
        }
        return null;
    }

    /**
     * @param username to be found
     * @return the client by his/her name if found, otherwise return null
     */

    public Client findClientByUsername (String username) {
        for (Client c:clients) {
            if (c.getUserName().equals(username)){
                return c;
            }
        }
        return null;
    }

    /**
     * All if-statements are tested out in this method and its fully functional
     * @param client who wants to create the group
     * @param groupName to be created
     */

    public void createGroup (Client client, String groupName){
        if (groupExists(groupName)){
            sendMessageToClient(client,"ER05 Group name already exist");
        } else if (!validFormat(groupName)) {
            sendMessageToClient(client,"ER06 Group name has an invalid format");
        } else {
            Group group = new Group(groupName);
            groups.add(group);
            group.joinClientToGroup(client);
            sendMessageToClient(client,"OK " + CMD_CG + " " + groupName);
        }
    }

    /**
     * All if-statements are tested out and its fully functional
     * @param client who wants to join the group
     * @param groupName that the client want to join to the group
     */

    public void joinGroup (Client client,String groupName) {
        if (!validFormat(groupName)){
            sendMessageToClient(client,"ER06 Group name has an invalid format");
        } else if (!groupExists(groupName)){
            sendMessageToClient(client,"ER07 Group name does not exist");
        } else if (checkUserInGroup(client.getUserName(),groupName)){
            sendMessageToClient(client,"ER09 User already joined this group");
        } else {
            Group group = findGroupByName(groupName);
            group.sendMessageToGroupMembersWhenJoined(client);
            group.joinClientToGroup(client);
            sendMessageToClient(client,"OK " + CMD_JG + " " + groupName);
        }
    }

    /**
     * @param username who has to be checked
     * @param groupName to be checked
     * @return true if the user exist in certain group
     */

    public boolean checkUserInGroup (String username,String groupName) {
        boolean result = false;
        for (Group g:groups){
            if (g.checkClientInGroup(username,groupName)){
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Quit from the server
     * @param client who wants to quit
     */

    public void quit (Client client) {
        sendMessageToClient(client,"OK Goodbye");
        removeClient(client);
    }

    /**
     * Client sudden disconnection
     * @param client who disconnected
     */

    public void clientDisconnection(Client client){
        System.out.println("[" + client.getUserName() + " " + client.isAuthenticated() + "] is not active any more");
        removeClient(client);
    }


    /**
     * Remove the client from the groups and from the client list
     * @param client to be removed
     */

    public void removeClient (Client client) {
        clients.remove(client);
        for (Group g:groups) {
            g.sendMessageToGroupMembersWhenLeft(client);
            g.removeClientFromGroup(client);
        }
        client.stopStreams();
        stats();
        // TODO: 16-Dec-21 check this for possible errors
        /*try {
            clientSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
    }

    /**
     * @param client who wants to send a broadcast message
     * @param message to be sent to other connected clients
     */

    public void sendBroadcastMessage (Client client, String message) {
        for (Client c : connectedClients()) {
            if (!c.getUserName().equals(client.getUserName())){
                sendMessageToClient(c,CMD_BCST + " " + client.getUserName() + " " + client.isAuthenticated() + " " + message);
            }
        }
        sendMessageToClient(client,"OK " + CMD_BCST + " " + message);
    }

    public void sendMessageToGroup (Client client, String groupName, String message) {
        if (!validFormat(groupName)) {
            sendMessageToClient(client,"ER06 Group name has an invalid format");
        } else if (!groupExists(groupName)){
            sendMessageToClient(client, "ER07 Group name does not exist");
        } else {
            Group group = findGroupByName(groupName);
            if (!group.checkClientInGroup(client.getUserName(),groupName)) {
                sendMessageToClient(client,"ER08 Please join the group first");
            } else {
                group.sendMessageToGroupMembers(client,message);
                sendMessageToClient(client,"OK " + CMD_BCSTG + " " + groupName + " " + message);
            }
        }
    }

    /**
     * @param username to be checked
     * @return true if the user exists in the list
     */

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

    /**
     * Check the format of either group name or the username
     * @param name of the group or the user
     * @return true if format is correct, otherwise return flase
     */

    public boolean validFormat(String name) {
        boolean result = false;
        for (int i = 0; i < name.length(); i++) {
            if (Character.isLetterOrDigit(name.charAt(i)) && name.length() > 2 && !name.contains(" ") && !name.contains(",")) {
                result = true;
            }
        }
        return result;
    }

    /**
     * @param client to be set out
     * @param username to be set to the user
     */

    public void setUser (Client client,String username) {
        client.setUserName(username);
        client.setConnected(true);
        client.setReceivedPong(false);
        sendMessageToClient(client,"OK "+username);
        stats();
        if (SHOULD_PING){
            //todo: check if this is correct
            new Thread(() -> heartBeat(client)).start();
        }
    }

    public void authenticateClient (Client client, String password) {
        if (password.length() < 6 || password.contains(" ")) {
            sendMessageToClient(client, "ER10 Password has an invalid format (the password should be between 6 - 20 characters)");
        } else if (client.isAuth()) {
            sendMessageToClient(client,"ER11 User already authenticated");
        } else {
            client.setAuth(true);
            client.setPassword(password);
            for (Client c:connectedClients()) {
                if (!c.getUserName().equals(client.getUserName())) {
                    c.out.println("AUTH " + client.getUserName() + " " + client.isAuthenticated());
                    c.out.flush();
                }
            }
            sendMessageToClient(client,"OK " + CMD_AUTH + " " + password);
        }
    }
}
