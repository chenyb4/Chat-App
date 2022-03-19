package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationLineEndings {

    private static Properties props = new Properties();

    private static Socket s;
    private static BufferedReader in;
    private static PrintWriter out;

    private final static int max_delta_allowed_ms = 1000;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationLineEndings.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("TC1.3 - windowsLineEndingIsAllowed")
    void windowsLineEndingIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.print("CONN myname\r\nBCST a\r\n");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK myname", serverResponse);
        serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK BCST a", serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC1.4 - linuxLineEndingIsAllowed")
    void linuxLineEndingIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.print("CONN mynamee\nBCST a\n");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK mynamee", serverResponse);
        serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK BCST a", serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.18 - sendUnknownCommand")
    void sendUnknownCommand() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Lukman");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK Lukman", serverResponse);
        out.println("dsadsadas");
        serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER00"), "Unknown command: "+serverResponse);
        out.println("QUIT");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}