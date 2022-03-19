package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationSendingAMessageToAGroup {

    private static Properties props = new Properties();

    private static Socket socketUser1;
    private static Socket socketUser2;
    private static BufferedReader inUser1,inUser2;
    private static PrintWriter outUser1,outUser2;

    private final static int max_delta_allowed_ms = 600;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationSendingAMessageToAGroup.class.getResourceAsStream("testconfig.properties");
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
        inUser1.close();
        outUser1.close();
        inUser2.close();
        outUser2.close();
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
    @DisplayName("TC1.6 - creatingAGroup")
    void creatingAGroup() {
        receiveLineWithTimeout(inUser1);//info message
        outUser1.println("CONN jjj");
        receiveLineWithTimeout(inUser1); //OK jjj
        outUser1.println("CG saxion10");;
        String serverResponse = receiveLineWithTimeout(inUser1);
        assertEquals("OK CG saxion10", serverResponse);
        outUser1.println("QUIT");
    }

    @Test
    @DisplayName("TC1.9 - loginUser")
    void loginUser() {
        receiveLineWithTimeout(inUser1); //info message
        outUser1.println("CONN Lukman");
        String serverResponse = receiveLineWithTimeout(inUser1);
        assertEquals("OK Lukman", serverResponse);
        outUser1.println("QUIT");
    }

    @Test
    @DisplayName("TC1.11 - sendMessageToGroup")
    void sendMessageToYourSelf() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2);

        outUser1.println("CONN juj");
        receiveLineWithTimeout(inUser1); //OK juj

        outUser2.println("CONN ksdfds");
        receiveLineWithTimeout(inUser2); //OK ksdfds

        //Join the previous created group
        outUser1.println("JG saxion10");
        receiveLineWithTimeout(inUser1); //OK JG saxion10
        outUser2.println("JG saxion10");
        receiveLineWithTimeout(inUser2); //OK JG saxion10

        //Send message to the group
        outUser1.println("BCSTG saxion10 Hello");
        receiveLineWithTimeout(inUser1); //OK BCSTG saxion10 Hello

        //Check if user 2 received it
        String serverResponse = receiveLineWithTimeout(inUser2);
        assertEquals("BCSTG juj 0 saxion10 Hello", serverResponse);

        outUser1.println("QUIT");
        outUser2.println("QUIT");
    }

    //@Test
    //@DisplayName("TC2.17 - sendMessageToGroupWithoutJoining")
    //void sendMessageToGroupWithoutJoining() {
    //    receiveLineWithTimeout(in); //info message
    //    out.println("CONN rgf");
    //    ;
    //    receiveLineWithTimeout(in);//OK rgf
    //    out.println("BCSTG saxion hello");
    //    ;
    //    String serverResponse = receiveLineWithTimeout(in);
    //    assertTrue(serverResponse.startsWith("ER08"), "Join the group first: "+serverResponse);
    //    out.println("QUIT");
    //}

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}