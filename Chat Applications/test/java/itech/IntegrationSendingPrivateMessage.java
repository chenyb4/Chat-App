package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationSendingPrivateMessage {

    private static Properties props = new Properties();

    private static Socket socketUser1;
    private static Socket socketUser2;
    private static BufferedReader inUser1,inUser2;
    private static PrintWriter outUser1,outUser2;

    private final static int max_delta_allowed_ms = 500;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationSendingPrivateMessage.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("TC2.11 - notLoggedIn")
    void authenticateMySelf() {
        receiveLineWithTimeout(inUser1); //info message
        outUser1.println("fsdaf");
        String serverResponse = receiveLineWithTimeout(inUser1);
        assertTrue(serverResponse.startsWith("ER03"), "User is not logged in: "+serverResponse);
    }

    @Test
    @DisplayName("TC1.9 - loginUser")
    void loginUser() {
        receiveLineWithTimeout(inUser1); //info message
        outUser1.println("CONN Lukman");
        String serverResponse = receiveLineWithTimeout(inUser1);
        assertEquals("OK Lukman", serverResponse);
    }

    @Test
    @DisplayName("TC2.12 - sendEmptyMessage")
    void sendEmptyMessage() {
        receiveLineWithTimeout(inUser1); //info message
        outUser1.println("CONN Yibing");
        receiveLineWithTimeout(inUser1); //OK Yibing
        outUser1.println("PM Lukman    ");
        String serverResponse = receiveLineWithTimeout(inUser1);
        assertTrue(serverResponse.startsWith("ER00"), "User send an empty message: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.13 - sendMessageToYourSelf")
    void sendMessageToYourSelf() {
        receiveLineWithTimeout(inUser1); //info message
        outUser1.println("CONN John");
        receiveLineWithTimeout(inUser1); //OK John
        outUser1.println("PM John Hi");
        String serverResponse = receiveLineWithTimeout(inUser1);
        assertTrue(serverResponse.startsWith("ER13"), "Cannot send message to yourself: "+serverResponse);
    }

    @Test
    @DisplayName("TC1.19 - checkIfUserReceivedPrivateMessage")
    void checkIfUserReceivedPrivateMessage() {
        receiveLineWithTimeout(inUser1); //info message
        outUser1.println("CONN John");
        receiveLineWithTimeout(inUser1); //OK John

        receiveLineWithTimeout(inUser2); //info message
        outUser2.println("CONN Doe");
        receiveLineWithTimeout(inUser2); //OK John

        outUser1.println("PM Doe Hi");
        receiveLineWithTimeout(inUser1); //OK PM Doe Hi
        String serverResponse = receiveLineWithTimeout(inUser2);
        assertEquals("PM John 0 Hi", serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}