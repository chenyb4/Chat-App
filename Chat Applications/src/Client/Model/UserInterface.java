package Client.Model;

import Client.Model.Client;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Handler;

public class UserInterface {

    private final Client client;

    public UserInterface (Client client) {
        this.client = client;
    }

    public void userInterface () {
        String userInput = readString();
        try {
            switch (userInput){
                case "-C" -> client.viewAllClients();
                case "-D" -> {
                    String username;
                    String message;
                    System.out.println("Please enter the username of the user you want to send the message to: >> ");
                    username=readString();
                    if(username.equals(ClientHandler.myOwnUsername)){
                        System.err.println("You cannot send a message to yourself.");
                    }else{
                        System.out.println("Please enter the message you want to send to "+username+": >> ");
                        message=readString();
                        client.sendPrivateMessage(username,message);
                    }
                }
                case "-G" -> {
                    String groupName;
                    System.out.println("Please enter the name of the group you want to create: >> ");
                    groupName=readString();
                    client.createGroup(groupName);
                }
                case "-JG" ->{
                    String groupName;
                    System.out.println("Please enter the name of the group you want to join: >> ");
                    groupName=readString();
                    client.joinGroup(groupName);
                }
                case "-EG" -> client.viewExistingGroups();
                case "-SG" ->{
                    String groupName;
                    System.out.println("Please enter the name of the group that you want to send the message to: >> ");
                    groupName=readString();
                    System.out.println("Please enter the message you want to send to the group: >> ");
                    String msg=readString();
                    client.sendMessageToGroup(groupName,msg);
                }
                case "-LG" -> {
                    String groupName;
                    System.out.println("Please enter the name of the group you want to leave: >> ");
                    groupName=readString();
                    client.leaveGroup(groupName);
                }
                case "-Q" -> {
                    client.stopConnection();
                    System.exit(0);
                }
                case "-AU"->{
                    String password;
                    System.out.println("Please enter your password: >> ");
                    password=readString();
                    client.authenticate(password);
                }
                case "-FT" -> {
                    String receiverUsername;
                    String filePath;
                    System.out.println("Please enter the receiver's username: >> ");
                    receiverUsername = readString();
                    if (receiverUsername.equals(ClientHandler.myOwnUsername)){
                        System.err.println("You cannot send the file to your self");
                    } else {
                        System.out.println("please enter the file path: >> ");
                        filePath = readString();
                        client.sendFileRequest(receiverUsername,filePath);
                    }
                }
                case "-FL" -> {
                    String id;
                    String choice;
                    System.out.println("Please enter the transfer id: >> ");
                    id = readString();
                    System.out.println("Do you want to accept the file or reject it? (y/n)");
                    choice = readString();
                    if (choice.equals("y")){
                        client.acceptFileRequest(id);
                    } else if (choice.equals("n")){
                        client.rejectFileRequest(id);
                    } else {
                        System.err.println("Invalid input for choice");
                    }
                }
                case "-P" -> {
                    String username;
                    String message;
                    System.out.println("Please enter the username of the user you want to send the encrypted message to: >> ");
                    username=readString();
                    if(username.equals(ClientHandler.myOwnUsername)){
                        System.err.println("You cannot send a message to yourself.");
                    }else{
                        System.out.println("Please enter the encrypted message you want to send to "+username+": >>");
                        message=readString();
                        client.sendEncryptedPrivateMessage(username,message);
                    }
                }
                case "-?" -> menu();
                default -> {
                    client.sendBroadcastMessage(userInput);
                }
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    public static void menu () {
        //Here where the user either accept or reject the file
        System.out.println("""
                        Please enter your message and hit enter to send the message to other people!
                        Or enter one of the menu items below:
                        "-?": to see this menu again.
                        "-C": to see all the connected clients.
                        "-D": to send a private message to a certain user.
                        "-P": to send an encrypted private message to a certain user.
                        "-G": to create a group.
                        "-JG": to join a group.
                        "-EG": to view existing groups.
                        "-SG": to send a message to everyone in a certain group.
                        "-LG": to leave a group.
                        "-AU": to authenticate yourself.
                        "-FT": to Send a file to a certain user.
                        "-FL": to either accept or reject a file request.
                        "-Q": to quit the chat app.

                        """
                );
    }

    public String readString () {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
