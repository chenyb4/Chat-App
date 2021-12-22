package Client;

import Server.Model.Message;

public class MessageConverter {



    public static String convertMessage(String m){
        //String fullMessage="";
        if(m == null || m.equals("")){
            //no message returned, the only situation is that the message is not sent
            return "(message not sent)";
        }

        // if it is not the case of message not sent, then we care be sure that
        //we can separate the message by space and analyze the parts
        String[] lineParts=m.split(" ");

        //the name is being converted in a format that it lets the users know whether the sender of
        //the message is authenticated
        String nameWithStar=MessageProcessor.convertNameToIncludeAuthInfo(lineParts);

        switch (lineParts[0]){
            case "BCST"->{
                //server tells me that someone sent a message to everyone
                //method for processing all broadcast messages
                return MessageProcessor.processBCST(lineParts);
            }
            case "OK"->{
                //server response message
                return MessageProcessor.processServerResponseMessage(lineParts);
            }
            case "PM"->{
                //server tells me that someone sent me a pm
                return MessageProcessor.processPM(lineParts);
            }
            case "CG"->{
                //server tells me that some mofo created a group
                // e.g. CG <username> <1> <group name>
                String groupName=lineParts[3];
                return nameWithStar+" created a group called "+groupName;
            }
            case "JG"->{
                //server tells me that someone join a group that I am a member of
                //e.g. JG <username> <1> <group name>
                String groupName=lineParts[3];
                return nameWithStar+" just entered group "+groupName+".";
            }
            case "BCSTG"->{
                //server tells me that someone broadcasted a message in a group that I am a member of
                //e.g. BCSTG <username> <1> <group name> <message>
                return MessageProcessor.processBCSTG(lineParts);
            }
            case "AUTH" ->{
                //server tells me that someone is autheticated
                //e.g. AUTH <username> <1>
                return nameWithStar+"is authenticated.";
            }
            case "LG"->{
                //server tells me that someone left a group that I am a member of
                //e.g. LG <username> <1> <group name>
                String groupName=lineParts[3];
                return nameWithStar+" left group "+groupName+".";
            }

            case "ER01" -> {return "This user name is already used. Please choose a different username!";}
            case "ER02" -> {return "The username you enter has an invalid format. Only characters, numbers and underscores are allowed!";}
            case "ER03" -> {return "Please log in first!";}
            case "ER04" -> {return "The user does not exist.";}
            case "ER05" -> {return "This group name is already in use.";}
            case "ER06" -> {return "The group name you entered has an invalid format. Your group name should not contain space.";}
            case "ER07" -> {return "The group you wanted to join does not exist.";}
            case "ER08" -> {return "You are currently not a member of this group. Please join the group first.";}
            case "ER09" -> {return "You are already in this group.";}
            case "ER10" -> {return "The password has an invalid format(the password should be between 6 - 20 characters)";}
            case "ER11" -> {return "You are already authenticated.";}
            case "ER12" -> {return "You cannot send an empty message.";}
            case "DSCN" -> {return "You have disconnected from the server due to inactivity.";}
            case "MSG" -> {return "You entered an invalid message.";}
            case "ER00" -> { return "There is no such command. Please enter '?' to see the manual.";}
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





}
