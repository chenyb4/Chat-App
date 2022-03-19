package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationAcceptedGroupName {

    private static Properties props = new Properties();

    private static Socket s;
    private static BufferedReader in;
    private static PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationAcceptedGroupName.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        s = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    @AfterAll
    static void closeAll() throws IOException {
        s.close();
        in.close();
        out.close();
    }

    @AfterEach
    void cleanup() throws IOException {
        s.close();
    }

    @Test
    @DisplayName("TC1.5 - threeCharactersAllowedInGroupName")
    void threeCharactersAllowedInGroupName() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN john5");
        receiveLineWithTimeout(in); //OK john5
        out.println("CG saxion5");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK CG saxion5", serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.4 - groupNameAlreadyExist")
    void groupNameAlreadyExist() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN jjj");
        receiveLineWithTimeout(in); //OK jjj
        out.println("CG saxion5");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER05"), "Group name already exist: "+serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.5 - groupNameInvalidFormat")
    void groupNameInvalidFormat() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN fdgd");
        receiveLineWithTimeout(in); //OK fdgd
        out.println("CG saxion,,");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER06"), "Group name has an invalid format: "+serverResponse);
        out.println("QUIT");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}