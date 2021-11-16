package com.company;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Main {

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start(1337);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect() throws IOException {
        Client client = new Client();
        client.startConnection("127.0.0.1", 1337);
        String response = client.sendMessage("hello server");
        assertEquals("hello client", response);
    }
}
