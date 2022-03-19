package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IntegrationBroadcastMessage {

    private static Properties props = new Properties();

    private static Socket socketUser1;
    private static Socket socketUser2;
    private static BufferedReader inUser1,inUser2;
    private static PrintWriter outUser1,outUser2;

    private final static int max_delta_allowed_ms = 200;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationBroadcastMessage.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();
    }

    @AfterAll
    static void closeAll() throws IOException {
        socketUser1.close();
        socketUser2.close();
        inUser1.close();
        outUser1.close();
        inUser2.close();
        outUser2.close();
    }

    @Test
    @DisplayName("TC1.18 - BCSTMessage")
    void BCSTMessage() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        outUser1.println("CONN user1");
        String resUser1 = receiveLineWithTimeout(inUser1); //server 200 response
        assumeTrue(resUser1.startsWith("OK"));

        // Connect user 2
        outUser2.println("CONN user2");
        String resUser2 = receiveLineWithTimeout(inUser2); //server 200 response
        assumeTrue(resUser2.startsWith("OK"));

        //send BCST from USER1
        outUser1.println("BCST messagefromuser1");
        String fromUser1 = receiveLineWithTimeout(inUser1); //server 200 response
        assertEquals("OK BCST messagefromuser1", fromUser1);

        String fromUser2 = receiveLineWithTimeout(inUser2); //BCST from user1
        assertEquals("BCST user1 0 messagefromuser1", fromUser2);

        //send BCST from USER2
        outUser2.println("BCST messagefromuser2");
        fromUser2 = receiveLineWithTimeout(inUser2); //server 200 response
        assertEquals("OK BCST messagefromuser2", fromUser2);

        fromUser1 = receiveLineWithTimeout(inUser1); //BCST from user2
        assertEquals("BCST user2 0 messagefromuser2", fromUser1);

        outUser1.println("QUIT");
        outUser2.println("QUIT");
    }

    @Test
    @DisplayName("RQ-S100 - userAlreadyLoggedIn")
    void userAlreadyLoggedIn(){
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        outUser1.println("CONN user1");
        String resUser1 = receiveLineWithTimeout(inUser1); //server 200 response
        assumeTrue(resUser1.startsWith("OK"));

        // Connect using same username
        outUser2.println("CONN user1");
        String resUser2 = receiveLineWithTimeout(inUser2);
        assertEquals("ER01 User already logged in", resUser2);
        outUser1.println("QUIT");
        outUser2.println("QUIT");

    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}