package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationSendingPrivateMessage {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationSendingPrivateMessage.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("TC2.1.1 - notLoggedIn")
    void authenticateMySelf() {
        receiveLineWithTimeout(in); //info message
        out.println("fsdaf");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER03"), "User is not logged in: "+serverResponse);
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
    @DisplayName("TC2.1.2 - sendEmptyMessage")
    void sendEmptyMessage() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Yibing");
        out.flush();
        receiveLineWithTimeout(in); //OK Yibing
        out.println("PM Lukman    ");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER00"), "User send an empty message: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.1.3 - sendMessageToYourSelf")
    void sendMessageToYourSelf() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN John");
        out.flush();
        receiveLineWithTimeout(in); //OK John
        out.println("PM John Hi");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER13"), "Invalid password: "+serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}