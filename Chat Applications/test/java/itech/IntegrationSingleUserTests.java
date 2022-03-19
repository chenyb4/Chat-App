package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationSingleUserTests {

    private static Properties props = new Properties();
    private static int ping_time_ms;
    private static int ping_time_ms_delta_allowed;
    private final static int max_delta_allowed_ms = 100;

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationSingleUserTests.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();

        ping_time_ms = Integer.parseInt(props.getProperty("ping_time_ms", "10000"));
        ping_time_ms_delta_allowed = Integer.parseInt(props.getProperty("ping_time_ms_delta_allowed", "100"));
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
    @DisplayName("TC1.1.6 - receiveInfoMessage")
    void receiveInfoMessage() {
        String firstLine = receiveLineWithTimeout(in);
        assertEquals("INFO welcome to chat room",firstLine);
    }

    @Test
    @DisplayName("TC1.1.7 - loginSucceedsWithOK")
    void loginSucceedsWithOK() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN myname");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK myname", serverResponse);
    }

    @Test
    @DisplayName("2.1.9 - loginEmptyNameWithER02")
    void loginEmptyNameWithER02() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN vv");
        ;
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("ER02 Username has an invalid format (only characters and numbers are allowed. Space is not allowed)", serverResponse);
    }

    @Test
    @DisplayName("2.2.1 - loginInvalidCharactersWithER02")
    void loginInvalidCharactersWithER02(){
        receiveLineWithTimeout(in); //info message
        out.println("CONN *a*");
        ;
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("ER02 Username has an invalid format (only characters and numbers are allowed. Space is not allowed)", serverResponse);
    }

    @Test
    @DisplayName("TC1.1.8 - pingShouldBeReceivedOnCorrectTime")
    void pingShouldBeReceivedOnCorrectTime(TestReporter testReporter) {
        receiveLineWithTimeout(in); //info message
        out.println("CONN myname");
        receiveLineWithTimeout(in); //server 200 response
        //Make sure the test does not hang when no response is received by using assertTimeoutPreemptively
        assertTimeoutPreemptively(ofMillis(ping_time_ms + ping_time_ms_delta_allowed), () -> {
            Instant start = Instant.now();
            String ping = in.readLine();
            Instant finish = Instant.now();
            // Make sure the correct response is received
            assertEquals("PING", ping);
            // Also make sure the response is not received too early
            long timeElapsed = Duration.between(start, finish).toMillis();
            testReporter.publishEntry("timeElapsed", ""+timeElapsed);
            assertTrue(timeElapsed > ping_time_ms - ping_time_ms_delta_allowed);
        });
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}