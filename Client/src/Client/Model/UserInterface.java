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
                System.out.println("Please enter the message you want to send to "+username+": >>");
                message=readString();
                try {
                    client.sendPrivateMessage(username,message);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
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
       /* System.out.println("Please enter your message and hit enter to send the message to other people!");
        System.out.println("If you want to see all the connected clients, please enter \"-C\".");
        System.out.println("If you want to send a private message to a certain user, please enter \"-D\".");
        System.out.println("If you want to create a group, please enter \"-G\".");
        System.out.println("If you want to join a group, please enter \"-JG\".");
        System.out.println("If you want to view existing groups, please enter \"-EG\".");
        System.out.println("If you want to send message to a group, please enter \"-SG\".");
        System.out.println("If you want to leave a group, please enter \"-LG\".");
        System.out.println("If you want to quit, please enter \"-Q\".");
        System.out.println("If you want to see the menu again, please enter \"-?\".");*/
        System.out.println("Please enter your message and hit enter to send the message to other people!\n\n"  +
                "Or enter one of the menu items below:\n" +
                "\"-C\": to see all the connected clients.\n" +
                "\"-D\": to send a private message to a certain user.\n" +
                "\"-G\": to create a group.\n" +
                "\"-JG\": to join a group.\n" +
                "\"-EG\": to view existing groups.\n" +
                "\"-SG\": to send a message to everyone in a certain group.\n" +
                "\"-LG\": to leave a group.\n" +
                "\"-Q\": to quit the chat app.\n" +
                "\"-?\": to see this menu again.\n\n");
    }

    public String readString () {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }
}
