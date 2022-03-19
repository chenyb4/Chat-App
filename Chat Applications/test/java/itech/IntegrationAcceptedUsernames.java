package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationAcceptedUsernames {

    private static Properties props = new Properties();

    private static Socket s;
    private static BufferedReader in;
    private static PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationAcceptedUsernames.class.getResourceAsStream("testconfig.properties");
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
        //s.close();
    }

    @AfterAll
    static void closeAll() throws IOException {
        s.close();
        in.close();
        out.close();
    }

    @Test
    @DisplayName("TC1.1 - threeCharactersIsAllowed")
    void threeCharactersIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN mym");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK mym", serverResponse);
    }

    @Test
    @DisplayName("TC2.8 - userAlreadyLoggedIn")
    void userAlreadyLoggedIn() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN mym");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER01"), "User is already logged in: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.1 - invalidUsernameFormat")
    void invalidUserNameFormat() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN my");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Too short username accepted: "+serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC1.2 - fourteenCharactersIsAllowed")
    void fourteenCharactersIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN abcdefghijklmn");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK abcdefghijklmn", serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.2 - slashIsNotAllowed")
    void slashIsNotAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN a\rlmn");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Wrong character accepted");
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.3 - bracketIsNotAllowed")
    void bracketIsNotAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN a)lmn");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Wrong character accepted");
        out.println("QUIT");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}