package Server;

import Server.Model.Server;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        int port = 1337;
        Server server = new Server();
        try {
            server.start(port);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
