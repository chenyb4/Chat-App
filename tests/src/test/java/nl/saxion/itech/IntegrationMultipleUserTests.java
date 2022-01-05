package nl.saxion.itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IntegrationMultipleUserTests {

    private static Properties props = new Properties();

    private Socket socketUser1,socketUser2;
    private BufferedReader inUser1,inUser2;
    private PrintWriter outUser1,outUser2;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationMultipleUserTests.class.getResourceAsStream("testconfig.properties");
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

    @Test
    @DisplayName("RQ-U101 - BCSTMessage")
    void BCSTMessage() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        outUser1.println("CONN user1");
        outUser1.flush();
        String resUser1 = receiveLineWithTimeout(inUser1); //server 200 response
        assumeTrue(resUser1.startsWith("OK"));

        // Connect user 2
        outUser2.println("CONN user2");
        outUser2.flush();
        String resUser2 = receiveLineWithTimeout(inUser2); //server 200 response
        assumeTrue(resUser2.startsWith("OK"));

        //send BCST from USER1
        outUser1.println("BCST messagefromuser1");
        outUser1.flush();
        String fromUser1 = receiveLineWithTimeout(inUser1); //server 200 response
        assertEquals("OK BCST messagefromuser1", fromUser1);

        String fromUser2 = receiveLineWithTimeout(inUser2); //BCST from user1
        assertEquals("BCST user1 messagefromuser1", fromUser2);

        //send BCST from USER2
        outUser2.println("BCST messagefromuser2");
        outUser2.flush();
        fromUser2 = receiveLineWithTimeout(inUser2); //server 200 response
        assertEquals("OK BCST messagefromuser2", fromUser2);

        fromUser1 = receiveLineWithTimeout(inUser1); //BCST from user2
        assertEquals("BCST user2 messagefromuser2", fromUser1);
    }

    @Test
    @DisplayName("RQ-S100 - Bad Weather - userAlreadyLoggedIn")
    void userAlreadyLoggedIn(){
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        outUser1.println("CONN user1");
        outUser1.flush();
        String resUser1 = receiveLineWithTimeout(inUser1); //server 200 response
        assumeTrue(resUser1.startsWith("OK"));

        // Connect using same username
        outUser2.println("CONN user1");
        outUser2.flush();
        String resUser2 = receiveLineWithTimeout(inUser2);
        assertEquals("ER01 User already logged in", resUser2);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}