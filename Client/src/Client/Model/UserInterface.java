package Client.Model;

import Client.Model.Client;

import java.io.IOException;
import java.util.Scanner;

public class UserInterface {

    private Client client;

    public UserInterface (Client client) {
        this.client = client;
    }

    public void userInterface () {
        String userInput = readString();
        switch (userInput){
            case "-C" -> client.viewAllClients();
            case "-JG" -> client.joinGroup(null);
            case "-D" -> {
                String username;
                String message;
                System.out.println("Please enter the username of the user you want to send the message to: >> ");
                username=readString();
                System.out.println("Please enter the message you want to send to "+username+": >>");
                message=readString();
                client.sendPrivateMessage(username,message);
            }
            case "-G" -> {
                String groupName;
                System.out.println("Please enter the name of the group you want to create: >>");
                groupName=readString();
                client.createGroup(groupName);
            }
            case "-EG" -> client.viewExistingGroups();
            case "-SG" -> client.sendMessageToGroup();
            case "-LG" -> client.leaveGroup();
            case "-Q" -> {
                try {
                    client.stopConnection();
                    System.out.println("You have exited the chat room.");
                } catch (IllegalStateException | IOException ise) {
                    System.err.println(ise.getMessage());
                    System.out.println();
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
        System.out.println("Please enter your message and hit enter to send the message to other people!");
        System.out.println("If you want to see all the connected clients, please enter \"-C\".");
        System.out.println("If you want to send a private message to a certain user, please enter \"-D\".");
        System.out.println("If you want to create a group, please enter \"-G\".");
        System.out.println("If you want to join a group, please enter \"-JG\".");
        System.out.println("If you want to view existing groups, please enter \"-EG\".");
        System.out.println("If you want to send message to a group, please enter \"-SG\".");
        System.out.println("If you want to leave a group, please enter \"-LG\".");
        System.out.println("If you want to quit, please enter \"-Q\".");
        System.out.println("If you want to see the menu again, please enter \"-?\".");
    }

    public String readString () {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
