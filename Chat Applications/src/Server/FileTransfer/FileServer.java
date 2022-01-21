package Server.FileTransfer;

import Server.Model.Client;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class FileServer {

    //Change console color to distinguish the difference between file server and chat server
    public static final String COLOR_RESET = "\u001B[0m";
    public static final String COLOR_GREEN = "\u001B[32m";

    private ServerSocket fileServerSocket;
    private Socket fileClientSocket;
    private final List<Transfer> transfers = new LinkedList<>();

    /**
     * Run the file server with certain port number
     */

    //Methods
    public void startFileServer (int port) throws IOException {
        try {
            fileServerSocket = new ServerSocket(port);
            System.out.println(COLOR_GREEN + "Starting file server version 1.2 with port: "+port + "." + COLOR_RESET);
            while (true){
                fileClientSocket = fileServerSocket.accept();
            }
        } catch (IOException e) {
            System.err.println(COLOR_GREEN +e.getMessage()+ COLOR_RESET);
            System.err.println(COLOR_GREEN +"Error in starting file server"+ COLOR_RESET);
        } finally {
            fileServerSocket.close();
            fileServerSocket.close();
        }
    }

    /**
     * @param sender of the file
     * @param receiver of the file
     */

    public Transfer addTransfer(Client sender, Client receiver, File file) {
        Transfer transfer = new Transfer(sender,receiver,file);
        transfers.add(transfer);
        return transfer;
    }

    //Getters
    public Socket getFileClientSocket() {
        return fileClientSocket;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }
}
