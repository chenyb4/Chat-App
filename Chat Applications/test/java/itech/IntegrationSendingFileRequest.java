package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationSendingFileRequest {

    private static Properties props = new Properties();

    private static Socket s;
    private static BufferedReader in;
    private static PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationSendingFileRequest.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("TC1.9 - loginUser")
    void loginUser() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Lukman");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK Lukman", serverResponse);
    }

    @Test
    @DisplayName("TC2.14 - userDoesNotExist")
    void userDoesNotExist() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Yibing");
        receiveLineWithTimeout(in); //OK Yibing
        out.println("AAFT jsfsd test/resources/itech/test.txt");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER04"), "User does not exist: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.15 - fileDoesNotExist")
    void fileDoesNotExist() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN ggg");
        receiveLineWithTimeout(in); //OK ggg
        out.println("AAFT Yibing skjddjdsfsd");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER14"), "File does not exist: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.16 - sendFileToMyself")
    void sendFileToMyself() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN gggg");
        receiveLineWithTimeout(in); //OK gggg
        out.println("AAFT gggg test/resources/itech/test.txt");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER15"), "File does not exist: "+serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}