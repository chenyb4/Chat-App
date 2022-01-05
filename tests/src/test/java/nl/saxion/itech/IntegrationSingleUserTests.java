package nl.saxion.itech;

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
    @DisplayName("RQ-S100 - receiveInfoMessage")
    void receiveInfoMessage() {
        String firstLine = receiveLineWithTimeout(in);
        assertTrue(firstLine.matches("INFO Welcome to the server 1\\..*"));
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
    @DisplayName("RQ-U100 - Bad Weather - loginEmptyNameWithER02")
    void loginEmptyNameWithER02() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("ER02 Username has an invalid format (only characters, numbers and underscores are allowed)", serverResponse);
    }

    @Test
    @DisplayName("RQ-U100 - Bad Weather - loginInvalidCharactersWithER02")
    void loginInvalidCharactersWithER02(){
        receiveLineWithTimeout(in); //info message
        out.println("CONN *a*");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("ER02 Username has an invalid format (only characters, numbers and underscores are allowed)", serverResponse);
    }

    @Test
    @DisplayName("RQ-S100 - pingShouldBeReceivedOnCorrectTime")
    void pingShouldBeReceivedOnCorrectTime(TestReporter testReporter) {
        receiveLineWithTimeout(in); //info message
        out.println("CONN myname");
        out.flush();
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