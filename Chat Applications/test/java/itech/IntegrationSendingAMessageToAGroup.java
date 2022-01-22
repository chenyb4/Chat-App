package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationSendingAMessageToAGroup {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationSendingAMessageToAGroup.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        s = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        s.close();
    }

    @Test
    @DisplayName("TC1.9 - loginUser")
    void loginUser() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Lukman");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK Lukman", serverResponse);
    }

    @Test
    @DisplayName("TC1.6 - creatingAGroup")
    void creatingAGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN jjj");
        out.flush(); //Login first
        receiveLineWithTimeout(in); //OK jjj
        out.println("CG saxion");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK CG saxion", serverResponse);
    }

    @Test
    @DisplayName("TC1.1.1 - sendMessageToGroupWithoutJoining")
    void sendMessageToGroupWithoutJoining() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN rgf");
        out.flush();
        receiveLineWithTimeout(in);//OK rgf
        out.println("BCSTG saxion hello");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER08"), "Join the group first: "+serverResponse);
    }

    @Test
    @DisplayName("TC1.1.1 - sendMessageToGroup")
    void sendMessageToYourSelf() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN juj");
        out.flush();
        receiveLineWithTimeout(in); //OK juj
        out.println("JG saxion");
        out.flush();
        receiveLineWithTimeout(in); //OK JG saxion
        out.println("BCSTG saxion hello");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK BCSTG saxion hello", serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}