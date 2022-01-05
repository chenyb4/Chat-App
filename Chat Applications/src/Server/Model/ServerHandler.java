package Server.Model;

import java.util.ArrayList;
import java.util.List;

public class ServerHandler {

    /**
     * @return an arraylist of connected clients (logged in clients).
     */

    public ArrayList<Client> connectedClientsList (List<Client> clients) {
        ArrayList<Client> connectedClients = new ArrayList<>();
        for (Client c: clients) {
            if (c.isConnected()){
                connectedClients.add(c);
            }
        }
        return connectedClients;
    }

    /**
     * @param groupName to be checked in the group list
     * @return true if the group exists otherwise return false
     */

    public boolean groupExists (String groupName,List<Group> groups) {
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
     * @param groupName to be found
     * @return the group by its name if found, otherwise return null
     */

    public Group findGroupByName (String groupName,List<Group> groups) {
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

    public Client findClientByUsername (String username,List<Client> clients) {
        for (Client c:clients) {
            if (c.getUserName().equals(username)){
                return c;
            }
        }
        return null;
    }

    /**
     * @param username who has to be checked
     * @param groupName to be checked
     * @return true if the user exist in certain group
     */

    public boolean checkUserInGroup (String username,String groupName,List<Group> groups) {
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
     * @param username to be checked
     * @return true if the user exists in the list
     */

    public boolean userExists (String username,List<Client> clients) {
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
     * @return true if format is correct, otherwise return false
     */

    public boolean validFormat(String name) {
        boolean result = false;
        for (int i = 0; i < name.length(); i++) {
            result = Character.isLetterOrDigit(name.charAt(i)) && name.length() > 2 && !name.contains(" ") && !name.contains(",");
        }
        return result;
    }
}