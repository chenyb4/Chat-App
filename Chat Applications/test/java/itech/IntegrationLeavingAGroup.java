package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationLeavingAGroup {

    private static Properties props = new Properties();

    private static Socket s;
    private static BufferedReader in;
    private static PrintWriter out;

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
    @DisplayName("RQ-U100 - loginSucceedsWithOK")
    void loginSucceedsWithOK() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN myname");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK myname", serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC1.13 - LeavingAGroup")
    void leavingAGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN john1");
        receiveLineWithTimeout(in); //OK john1
        out.println("CG saxion1");
        receiveLineWithTimeout(in); //OK CG saxion1
        out.println("LG saxion1");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK LG saxion1", serverResponse);
        out.println("QUIT");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}