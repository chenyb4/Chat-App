package Client.Model;

import Client.Model.Client;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Handler;

public class UserInterface {

    private Client client;

    public UserInterface (Client client) {
        this.client = client;
    }

    public void userInterface () {
        String userInput = readString();
        switch (userInput){
            case "-C" -> {
                try {
                    client.viewAllClients();

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            case "-D" -> {
                String username;
                String message;
                System.out.println("Please enter the username of the user you want to send the message to: >> ");
                username=readString();
                if(username.equals(ClientHandler.myOwnUsername)){
                    System.err.println("You cannot send a message to yourself.");
                }else{
                    System.out.println("Please enter the message you want to send to "+username+": >>");
                    message=readString();
                    try {
                        client.sendPrivateMessage(username,message);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }

            }
            case "-G" -> {
                String groupName;
                System.out.println("Please enter the name of the group you want to create: >>");
                groupName=readString();
                try {
                    client.createGroup(groupName);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            case "-JG" ->{
                String groupName;
                System.out.println("Please enter the name of the group you want to join: >>");
                groupName=readString();
                try {
                    client.joinGroup(groupName);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            case "-EG" -> {
                try {
                    client.viewExistingGroups();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            case "-SG" ->{
                String groupName;
                System.out.println("Please enter the name of the group that you want to send the message to: >>");
                groupName=readString();
                System.out.println("Please enter the message you want to send to the group: >>");
                String msg=readString();
                try {
                    client.sendMessageToGroup(groupName,msg);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            case "-LG" -> {
                String groupName;
                System.out.println("Pleae enter the name of the group you want to leave: >>");
                groupName=readString();
                try {
                    client.leaveGroup(groupName);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            case "-Q" -> {
                try {
                    client.stopConnection();
                    System.out.println("You have exited the chat room.");
                } catch (IllegalStateException | IOException ise) {
                    System.err.println(ise.getMessage());
                    System.out.println();
                }
            }
            case "-AU"->{
                String password;
                System.out.println("Please enter your password: >> ");
                password=readString();
                try {
                    client.authenticate(password);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
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
                    try {
                        client.sendFileRequest(receiverUsername,filePath);
                    } catch (Exception e){
                        System.err.println(e.getMessage());
                    }
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
                    try {
                        client.sendEncryptedPrivateMessage(username,message);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
            case "-?" -> menu();
            default -> {
                try {
                    client.sendBroadcastMessage(userInput);
                }   catch (IllegalArgumentException | IllegalStateException e) {
                    System.err.println(e.getMessage());
                    System.out.println();
                }
            }
        }
    }

    public static void menu () {
        System.out.println("Please enter your message and hit enter to send the message to other people!\n\n"  + "Or enter one of the menu items below:\n" +
                        "\"-?\": to see this menu again.\n"+
                "\"-C\": to see all the connected clients.\n" +
                "\"-D\": to send a private message to a certain user.\n" +
                "\"-P\": to send an encrypted private message to a certain user.\n" +
                "\"-G\": to create a group.\n" +
                "\"-JG\": to join a group.\n" +
                "\"-EG\": to view existing groups.\n" +
                "\"-SG\": to send a message to everyone in a certain group.\n" +
                "\"-LG\": to leave a group.\n" +
                "\"-AU\": to authenticate yourself.\n" +
                        "\"-FT\": to Send a file to a certain user.\n" +
                //Here where the user either accept or reject the file
                        "\"-FL\": to either accept or reject a file request.\n" +
                "\"-Q\": to quit the chat app.\n\n"
                );
    }

    public String readString () {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
