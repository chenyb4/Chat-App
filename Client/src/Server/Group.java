package Server;

import Client.Client;

import java.util.ArrayList;

public class Group {
    private String name = "";
    private ArrayList<Client> clients = new ArrayList<>();

    public Group(String groupName, ArrayList<Client> allClients) {
        this.name = groupName;
        this.clients = allClients;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }
}
