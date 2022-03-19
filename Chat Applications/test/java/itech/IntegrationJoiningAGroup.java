package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationJoiningAGroup {

    private static Properties props = new Properties();

    private static Socket s;
    private static BufferedReader in;
    private static PrintWriter out;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationJoiningAGroup.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("TC2.6 - groupNameDoesNotExist")
    void groupNameDoesNotExist() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN john");
        receiveLineWithTimeout(in); //OK john
        out.println("JG saxion55");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER07"), "Group name does not exist: "+serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC1.6 - creatingAGroup")
    void creatingAGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN jjj");
        receiveLineWithTimeout(in); //OK john
        out.println("CG saxion");
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK CG saxion", serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC1.7 - joiningAGroup")
    void joiningAGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN ddgdf");
        receiveLineWithTimeout(in); //OK ddgdf
        out.println("JG saxion");
        //When you create a group you already in the group
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK JG saxion", serverResponse);
        out.println("QUIT");
    }

    @Test
    @DisplayName("TC2.7 - userAlreadyJoinedTheGroup")
    void userAlreadyJoinedTheGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN ddgdff");
        receiveLineWithTimeout(in); //OK ddgdff
        out.println("CG uni");
        receiveLineWithTimeout(in); //OK CG saxion
        out.println("JG uni");
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER09"), "User already joined this group: "+serverResponse);
        out.println("QUIT");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}