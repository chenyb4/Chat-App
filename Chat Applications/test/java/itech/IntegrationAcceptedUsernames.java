package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationAcceptedUsernames {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

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
        s.close();
    }

    @Test
    @DisplayName("TC1.1 - threeCharactersIsAllowed")
    void threeCharactersIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN mym");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK mym", serverResponse);
    }

    @Test
    @DisplayName("TC2.8 - userAlreadyLoggedIn")
    void userAlreadyLoggedIn() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN mym");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER01"), "User is already logged in: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.1 - invalidUsernameFormat")
    void invalidUserNameFormat() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN my");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Too short username accepted: "+serverResponse);
    }

    @Test
    @DisplayName("TC1.2 - fourteenCharactersIsAllowed")
    void fourteenCharactersIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN abcdefghijklmn");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK abcdefghijklmn", serverResponse);
    }

    @Test
    @DisplayName("TC2.2 - slashRIsNotAllowed")
    void slashRIsNotAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN a\rlmn");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Wrong character accepted");
    }

    @Test
    @DisplayName("TC2.3 - bracketIsNotAllowed")
    void bracketIsNotAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN a)lmn");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Wrong character accepted");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}