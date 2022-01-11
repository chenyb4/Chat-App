package Server.FileTransfer;

import Server.Model.Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class Transfer {

    //Change console color to distinguish the difference between file server and chat server
    public static final String COLOR_RESET = "\u001B[0m";
    public static final String COLOR_GREEN = "\u001B[32m";

    private final Client sender;
    private final Client receiver;
    private String id;
    private File file;
    private static final AtomicInteger count = new AtomicInteger(0);

    //Constructor
    public Transfer(Client sender, Client receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.id = count.incrementAndGet()+"";
    }

    /**
     * Upload the file to the server
     * @param path of the file
     */

    //Methods
    public void uploadToServer (String path) {
        file = new File(path);
        byte[] contents = new byte[10000];
        try {
            Socket s = new Socket(InetAddress.getByName("localhost"),5000);
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = s.getOutputStream();
            fis.read(contents,0, contents.length);
            os.write(contents,0, contents.length);
            System.out.println(COLOR_GREEN + "File: [ "+path+" ] was uploaded to the server successfully" + COLOR_RESET);
        } catch (IOException io) {
            System.err.println(COLOR_GREEN +io.getMessage()+ COLOR_RESET);
            System.err.println(COLOR_GREEN +"Error in uploading files to server"+ COLOR_RESET);
        }
    }

    /**
     * Send a message to the sender client
     * @param msg to be sent
     */

    public void sendMessageToSender (String msg){
        sender.out.println(msg);
        sender.out.flush();
    }

    public Client getReceiver() {
        return receiver;
    }

    public Client getSender() {
        return sender;
    }

    public File getFile() {
        return file;
    }

    public String getId() {
        return id;
    }
}
