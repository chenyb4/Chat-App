package Server.Model;

public class Message {
    private String type = "";
    private String payload = "";

    public Message(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public Message(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }
}
