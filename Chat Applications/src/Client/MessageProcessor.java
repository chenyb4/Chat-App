package Client;

import java.util.ArrayList;
import java.util.HashMap;

//the static methods in this class are being called by message converter to convert messages
public class MessageProcessor {

    public static String convertNameToIncludeAuthInfo(String[] lineParts){
        String nameWithStar="";
        if(Integer.parseInt(lineParts[2])==1){
            nameWithStar="*"+lineParts[1];
        }else{
            nameWithStar=lineParts[1];
        }
        return nameWithStar;
    }

    /**
     * @param lineParts
     * @return
     */

    public static String processBCST(String[] lineParts){
        //received broadcast message
        //e.g. BCST <username> <1> <message>
        String nameWithStar=convertNameToIncludeAuthInfo(lineParts);
        String message="";
        for (int i = 3; i < lineParts.length; i++) {
            message +=" "+lineParts[i];
        }
        return nameWithStar+" says:"+message;
    }

    public static String processBCSTG(String[] lineParts){
        //received broadcast message for a group
        //e.g. BCSTG <username> <1> <group name> <message>
        String nameWithStar=convertNameToIncludeAuthInfo(lineParts);
        String groupName=lineParts[3];
        String message="";
        for (int i = 4; i < lineParts.length; i++) {
            message +=" "+lineParts[i];
        }
        return nameWithStar+" says to everyone in group "+groupName+": "+message;
    }

    /**
     * @param lineParts
     * @return
     */

    public static String processPM(String[] lineParts){
        //e.g. PM <username of the sender> <1> <message>
        String nameWithStar=convertNameToIncludeAuthInfo(lineParts);
        String fullMessage="";
        for (int i = 3; i <lineParts.length ; i++) {
            fullMessage+=lineParts[i]+" ";
        }

        return nameWithStar+" says to you secretly: "+fullMessage;
    }

    /**
     * Process the message to the receiver
     * @param lineParts The line of message seperated by space and stored in array
     * @return the message returned to and handled by method convertMessage
     */

    public static String processPME(String[] lineParts){
        //e.g. PME <username of the sender> <1> <encrypted message>
        String nameWithStar=convertNameToIncludeAuthInfo(lineParts);
        String fullMessage="";
        for (int i = 3; i <lineParts.length ; i++) {
            fullMessage+=lineParts[i]+" ";
        }
        return nameWithStar+" has sent an encrypted message which says: "+fullMessage;
    }

    /**
     * Client receives a file request
     * @param lineParts The line of message seperated by space and stored in array
     * @return the message returned to and handled by method convertMessage
     */

    public static String processAAFT (String[] lineParts) {
        //e.g. AAFT <C1 username> <1> <file name> <transfer id>
        String nameWithStar=convertNameToIncludeAuthInfo(lineParts);
        String fileName = lineParts[3];
        String transferId = lineParts[4];
        return nameWithStar+" has sent you a file request with name: " + fileName + " and id: " + transferId ;
    }

    /**
     * Client accepts the file request
     * @param lineParts The line of message seperated by space and stored in array
     * @return the message returned to and handled by method convertMessage
     */

    public static String processRAFTA (String[] lineParts) {
        //e.g. RAFTA <C2 username> <1> <file name> <transfer id>
        String nameWithStar=convertNameToIncludeAuthInfo(lineParts);
        String fileName = lineParts[3];
        String transferId = lineParts[4];
        return nameWithStar+" has accepted your file: " + fileName + " with id: " + transferId ;
    }

    /**
     * Client rejects the file request
     * @param lineParts The line of message seperated by space and stored in array
     * @return the message returned to and handled by method convertMessage
     */

    public static String processRAFTR (String[] lineParts) {
        //e.g. RAFTR <C2 username> <1> <file name> <transfer id>
        String nameWithStar=convertNameToIncludeAuthInfo(lineParts);
        String fileName = lineParts[3];
        String transferId = lineParts[4];
        return nameWithStar+" has rejected your file: " + fileName + " with id: " + transferId ;
    }

    /**
     * if the received message contains "OK", then that is a server response message
     * @param lineParts The line of message seperated by space and stored in array
     * @return the message returned to and handled by method convertMessage
     */

    public static String processServerResponseMessage(String[] lineParts){
        switch (lineParts[1]){
            case "VCC"->{
                //view connected clients
                // the line parts of the names are not in the correct format
                //becase they were seperated by space
                //here we need to combine everything from index 2 in line part
                // and then seperate them by comma, then by space to see who is authed
                //OK VCC Lukman 0,Yibing 0,
                //OK
                //VCC
                //Lukman
                //0,Yibing
                //0,
                //namesString is a string that contain all the names
                String namesString="";
                for (int i = 2; i <lineParts.length ; i++) {
                    namesString+=lineParts[i]+" ";
                }
                // System.out.println("nameString:"+namesString);
                //nameString
                //Lukman 0,Yibing 0,
                String[] namesWithAuthInfo=namesString.split(",");
                //Lukman 0 index 0
                //Yibing 0 index 1
                //          index 2
                //false for the boolean means unauthenticated
                HashMap<String, Boolean> authInfo=new HashMap<>();
                for (int i = 0; i < namesWithAuthInfo.length-1; i++) {
                    //i=0
                    String[] oneNameWithAuthInfo=namesWithAuthInfo[i].split(" ");
                    //Lukman index 0
                    // 0    index 1
                    if(Integer.parseInt(oneNameWithAuthInfo[1])==0){
                        //not authenticated
                        authInfo.put(oneNameWithAuthInfo[0],false);
                    }else{
                        authInfo.put(oneNameWithAuthInfo[0],true);
                    }
                }
                // now produce the name witht the * shape
                ArrayList<String> namesWithStars=new ArrayList<>();
                for (String i : authInfo.keySet()) {
                    if(authInfo.get(i)==true){
                        namesWithStars.add("*"+i);
                    }else{
                        namesWithStars.add(i);
                    }
                }
                //now make the message for returning
                String messageToReturn="The list of connected users are: \n";
                for (int i = 0; i < namesWithStars.size(); i++) {
                    if(i==namesWithStars.size()-1){
                        messageToReturn+=namesWithStars.get(i)+".";
                    }else{
                        messageToReturn+=namesWithStars.get(i)+",";
                    }
                }
                return messageToReturn;
            }
            case "PM", "BCST","BCSTG","PME" ->{
                //server tells me that the pm is sent;
                //or server tells me that the broadcast message was sent
                return "(message sent)";
            }
            case  "CG" -> {return "The group is created.";}
            case "JG"->{return "You have joined the group.";}
            case "VEG"->{
                if(lineParts[2].equals("")){
                    return "There is no group for now.";
                }else{
                    String temp="The groups are: ";
                    String[] groupNames=convertNameString(lineParts[2]);
                    for (int i = 0; i < groupNames.length; i++) {
                        if(i==0){
                            //first group name
                            temp+=groupNames[i];
                        }else{
                            //since the 2nd group name, there should be a comma in front for user friendliness puposes
                            temp+=","+groupNames[i];
                        }
                    }
                    return temp;
                }
            }
            case "AUTH"-> {return "You are now authenticated.";}
            //this space is for file transfer messages
            case "AAFT" -> {
                return "Your file request was sent to " + lineParts[2] + " ";
            }
            case "RAFTA" -> {return "You accepted the file request with id "+ lineParts[2];}
            case "RAFTR" -> {return "You Rejected the file request with id "+ lineParts[2];}
            case "LG"->{return "You have left the group.";}
            //termination
            case "Goodbye" -> {return "You have exited the chat room.";}
            default -> {
                //logged in
                String name=lineParts[1];
                return "You have successfully logged in to the chat room, "+name+". Now you can start chatting!";
            }
        }
    }

    /**
     *
     * @param nameString the string containing the names of the users or the names of the users or groups seperated by comma
     * @return Array of names
     */

    private static String[] convertNameString(String nameString){
        String[] lineParts=nameString.split(",");
        return lineParts;
    }
}
