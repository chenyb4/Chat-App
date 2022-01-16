package Server.Model;

import java.util.LinkedList;
import java.util.List;

public class Group {

    private String name = "";
    private List<Client> clients;
    private List<Message> messages;

    //Constructor
    public Group (String groupName) {
        this.name = groupName;
        this.clients = new LinkedList<>();
        this.messages = new LinkedList<>();
    }

    /**
     * Check if the client exist in a particular group
     * @param username of teh client
     * @param groupName that needs to be checked
     * @return true if teh client exist in that group, otherwise false
     */

    //Methods
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

    /**
     * @param client that joined the group
     */

    public void sendMessageToGroupMembersWhenJoined (Client client) {
        for (Client c:clients) {
            if (!c.getUserName().equals(client.getUserName())) {
                c.out.println("JG " + client.getUserName() + " " + client.isAuthenticated() + " " + name);
                c.out.flush();
            }
        }
        //All messages send to this group can be viewed by this client
        /*for (Message m:messages) {
            client.out.println(m.getType());
            client.out.flush();
        }*/
    }

    /**
     * @param client that want to leave a group
     */

    public void sendMessageToGroupMembersWhenLeft (Client client) {
        for (Client c:clients) {
            if (!c.getUserName().equals(client.getUserName())) {
                c.out.println("LG " + client.getUserName() + " " + client.isAuthenticated() + " " + name);
                c.out.flush();
            }
        }
    }

    /**
     * Send a message to group members
     * @param client who is teh sender if the message
     * @param message to be sent
     */

    public void sendMessageToGroupMembers (Client client,String message) {
        for (Client c:clients) {
            if (!c.getUserName().equals(client.getUserName())) {
                c.out.println("BCSTG " + client.getUserName() + " " + client.isAuthenticated() + " " + name + " " + message);
                c.out.flush();
            }
        }
        //todo: if the client wants to view the messages before the client joined the group
        //messages.add(new Message("BCSTG " + client.getUserName() + " " + client.isAuthenticated() + " " + name + " " + message));
    }

    public void addClientToGroup(Client client) {
        clients.add(client);
    }

    public void removeClientFromGroup (Client client) {
        clients.remove(client);
    }

    //Getter
    public String getName() {
        return name;
    }

    //To String
    @Override
    public String toString() {
        return name + ",";
    }
}
