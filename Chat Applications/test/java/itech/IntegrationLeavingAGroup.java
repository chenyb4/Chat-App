package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationLeavingAGroup {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationLeavingAGroup.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("RQ-U100 - loginSucceedsWithOK")
    void loginSucceedsWithOK() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN myname");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK myname", serverResponse);
    }

    @Test
    @DisplayName("TC1.1.3 - LeavingAGroup")
    void leavingAGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN john");
        out.flush(); //Login first
        receiveLineWithTimeout(in); //OK john
        out.println("CG saxion");
        out.flush();
        receiveLineWithTimeout(in); //OK CG saxion
        out.println("LG saxion");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK LG saxion", serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}