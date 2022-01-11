package Server.Model;

public class Message {

    private String type = "";
    private String payload = "";

    //Contractors
    public Message(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public Message(String type) {
        this.type = type;
    }

    /**
     * Parse the command to the desired format
     * @param command to be parsed
     * @return a new Message object
     */

    //Methods
    public static Message parseCommand (String command) {
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

    //Getters
    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }
}
