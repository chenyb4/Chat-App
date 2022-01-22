package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationSendingFileRequest {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

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

    @Test
    @DisplayName("TC1.9 - loginUser")
    void loginUser() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Lukman");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK Lukman", serverResponse);
    }

    @Test
    @DisplayName("TC2.1.4 - userDoesNotExist")
    void userDoesNotExist() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Yibing");
        out.flush();
        receiveLineWithTimeout(in); //OK Yibing
        out.println("AAFT jsfsd test/resources/itech/test.txt");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER04"), "User does not exist: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.1.5 - fileDoesNotExist")
    void fileDoesNotExist() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN ggg");
        out.flush();
        receiveLineWithTimeout(in); //OK ggg
        out.println("AAFT Yibing skjddjdsfsd");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER14"), "File does not exist: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.1.6 - sendFileToMyself")
    void sendFileToMyself() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN gggg");
        out.flush();
        receiveLineWithTimeout(in); //OK gggg
        out.println("AAFT gggg test/resources/itech/test.txt");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER15"), "File does not exist: "+serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}