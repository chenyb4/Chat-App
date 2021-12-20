package Server.Model;

import java.util.LinkedList;
import java.util.List;

public class Group {
    private String name = "";
    private List<Client> clients;

    public Group(String groupName) {
        this.name = groupName;
        this.clients = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public boolean checkClientInGroup (String username,String groupName) {
        boolean result = false;
        for (Client c: clients) {
            if (c.getUserName().equals(username) && groupName.equals(name)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void joinClientToGroup (Client client) {
        clients.add(client);
    }

    public void sendMessageToGroupMembersWhenJoined(Client client){
        for (Client c:clients) {
            if (!c.getUserName().equals(client.getUserName())) {
                c.out.println("JG " + client.getUserName() + " " + client.isAuthenticated() + " " + name);
                c.out.flush();
            }
        }
    }

    public void sendMessageToGroupMembersWhenLeft(Client client){
        for (Client c:clients) {
            if (!c.getUserName().equals(client.getUserName())) {
                c.out.println("LG " + client.getUserName() + " " + client.isAuthenticated() + " " + name);
                c.out.flush();
            }
        }
    }

    public void sendMessageToGroupMembers (Client client,String message) {
        for (Client c:clients) {
            if (!c.getUserName().equals(client.getUserName())) {
                c.out.println("BCSTG " + client.getUserName() + " " + client.isAuthenticated() + " " + name + " " + message);
                c.out.flush();
            }
        }
    }

    public void removeClientFromGroup (Client client) {
        clients.remove(client);
    }

    @Override
    public String toString() {
        return name + ",";
    }
}
