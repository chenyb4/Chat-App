package Client;

public class Helper {

    public static String convertMessage(String m){
        //String fullMessage="";
        String[] lineParts=m.split(" ");

        switch (lineParts[0]){
            case "BCST"->{
                return handleBCST(lineParts);
            }
            case "OK"->{
                return handleServerResponseMessage(lineParts);
            }
            case "ER01"->{return "This user name is already used. Please choose a different username!";}
            case "ER02"->{return "The username you enter has an invalid format. Only characters, numbers and underscores are allowed!";}
            case "ER03"->{return "Please log in first!";}
            case "DSCN"->{return "You have disconnected from the server due to inactivity.";}
            case "MSG"->{return "You entered an invalid message.";}
            case "ER00"->{ return "There is no such command. Please enter '?' to see the manual.";}
            default -> {}
        }

        if (m.contains("INFO")){
            return "Welcome to the chat server!";
        }

        if(m==null){
            return "(message not sent)";
        }

        return "";


    }



    public static String handleBCST(String[] lineParts){
        //received broadcast message
        String name=lineParts[1];
        String message="";
        for (int i = 2; i < lineParts.length; i++) {
            message +=" "+lineParts[i];
        }
        return name+" says:"+message;
    }

    public static String handleServerResponseMessage(String[] lineParts){
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
    }
}
