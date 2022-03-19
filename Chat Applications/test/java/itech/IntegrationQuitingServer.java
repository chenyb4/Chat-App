package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationQuitingServer {

    private static Properties props = new Properties();

    private static Socket s;
    private static BufferedReader in;
    private static PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationQuitingServer.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("TC1.15 - quitTheServer")
    void quitTheServer() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN myname");
        receiveLineWithTimeout(in); //OK myname
        out.println("QUIT");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK Goodbye", serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}