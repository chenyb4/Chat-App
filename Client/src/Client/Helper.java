package Client;

public class Helper {

    public static String convertMessage(String m){
        //String fullMessage="";
        String[] lineParts=m.split(" ");
        if(m.contains("INFO")) {
            return "Welcome to the chat server!";
        } else if (lineParts[0].equals("BCST")) {
            //received broadcast message
            String name=lineParts[1];
            String message="";
            for (int i = 2; i < lineParts.length; i++) {
               message +=" "+lineParts[i];
            }
            return name+" says:"+message;
        } else if (lineParts[0].equals("OK")) {
            if (lineParts[1].equals("BCST")) {
                //sent broadcast message
                return "(message sent)";
            } else if (lineParts[1].equals("Goodbye")) {
                //termination
                return "You have exited the chat room.";
            } else {
                //logged in
                String name=lineParts[1];
                return "You have successfully logged in to the chat room, "+name+". Now you can start chatting!";
            }
        } else if (lineParts[0].equals("ER01")) {
            return "This user name is already used. Please choose a different username!";
        } else if (lineParts[0].equals("ER02")) {
            return ("The username you enter has an invalid format. Only characters, numbers and underscores are allowed!");
        } else if (lineParts[0].equals("ER03")) {
            return "Please log in first!";
        } else if (lineParts[0].equals("DSCN")) {
            return "You have disconnected from the server due to inactivity.";
        } else if (lineParts[0].equals("MSG")) {
            return "You entered an invalid message.";
        } else if (lineParts[0].equals("ER00")) {
            return "There is no such command. Please enter '?' to see the manual.";
        }
        return "";
    }
}
