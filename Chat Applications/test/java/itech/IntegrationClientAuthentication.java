package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationClientAuthentication {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationClientAuthentication.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("TC1.8 - authenticateMySelf")
    void authenticateMySelf() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Lukman");
        out.flush();
        receiveLineWithTimeout(in); //OK mym
        out.println("AUTH 123456");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK AUTH 123456", serverResponse);
    }

    @Test
    @DisplayName("TC2.9 - alreadyAuthenticated")
    void alreadyAuthenticated() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN Yibing");
        out.flush();
        receiveLineWithTimeout(in); //OK Yibing
        out.println("AUTH 123456Yc");
        out.flush();
        receiveLineWithTimeout(in); //OK AUTH 123456Yc
        out.println("AUTH 123456Yc");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER11"), "User already authenticated: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.1.6 - invalidPassword")
    void invalidPassword() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN John");
        out.flush();
        receiveLineWithTimeout(in); //OK Yibing
        out.println("AUTH 123456Ycccccc");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER18"), "Invalid password: "+serverResponse);
    }

    @Test
    @DisplayName("TC2.1.7 - cannotAuthenticate")
    void cannotAuthenticate() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN hello");
        out.flush();
        receiveLineWithTimeout(in); //OK Yibing
        out.println("AUTH 123456Ycccccc");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER19"), "Cannot authenticate cause your credentials were not found: "+serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}