package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationClientAuthentication {

    private static Properties props = new Properties();

    private static Socket s;
    private static BufferedReader in;
    private static PrintWriter out;

    private final static int max_delta_allowed_ms = 600;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationClientAuthentication.class.getResourceAsStream("testconfig.properties");
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

    @AfterAll
    static void closeAll() throws IOException {
        s.close();
        in.close();
        out.close();
    }

    @Test
    @DisplayName("TC1.8 - authenticateMySelf")
    void authenticateMySelf() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Lukman");
        receiveLineWithTimeout(in); //OK mym
        out.println("AUTH 123456");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK AUTH 123456", serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.9 - alreadyAuthenticated")
    void alreadyAuthenticated() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Yibing");
        receiveLineWithTimeout(in); //OK Yibing
        out.println("AUTH 123456Yc");
        receiveLineWithTimeout(in); //OK AUTH 123456Yc
        out.println("AUTH 123456Yc");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER11"), "User already authenticated: "+serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.16 - invalidPassword")
    void invalidPassword() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN John");
        receiveLineWithTimeout(in); //OK Yibing
        out.println("AUTH 123456Ycccccc");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER18"), "Invalid password: "+serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.17 - cannotAuthenticate")
    void cannotAuthenticate() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN hello");
        receiveLineWithTimeout(in); //OK Yibing
        out.println("AUTH 123456Ycccccc");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER19"), "Cannot authenticate cause your credentials were not found: "+serverResponse);
        out.println("QUIT");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}