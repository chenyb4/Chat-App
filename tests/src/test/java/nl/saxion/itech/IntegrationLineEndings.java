package nl.saxion.itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationLineEndings {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

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

    @Test
    @DisplayName("RQ-B202 - windowsLineEndingIsAllowed")
    void windowsLineEndingIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.print("CONN myname\r\nBCST a\r\n");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK myname", serverResponse);
        serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK BCST a", serverResponse);
    }

    @Test
    @DisplayName("RQ-B202 - linuxLineEndingIsAllowed")
    void linuxLineEndingIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.print("CONN myname\nBCST a\n");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK myname", serverResponse);
        serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK BCST a", serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}