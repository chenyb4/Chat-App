package itech;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationJoiningAGroup {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

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



    @Test
    @DisplayName("TC2.6 - groupNameDoesNotExist")
    void groupNameDoesNotExist() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN john");
        out.flush(); //Login first
        receiveLineWithTimeout(in); //OK john
        out.println("JG saxion");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER07"), "Group name does not exist: "+serverResponse);
    }

    @Test
    @DisplayName("TC1.6 - creatingAGroup")
    void creatingAGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN jjj");
        out.flush(); //Login first
        receiveLineWithTimeout(in); //OK john
        out.println("CG saxion");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK CG saxion", serverResponse);
    }

    @Test
    @DisplayName("TC1.7 - joiningAGroup")
    void joiningAGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN ddgdf");
        out.flush(); //Login first
        receiveLineWithTimeout(in); //OK ddgdf
        out.println("JG saxion");
        out.flush();
        //When you create a group you already in the group
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK JG saxion", serverResponse);
    }

    @Test
    @DisplayName("TC2.7 - userAlreadyJoinedTheGroup")
    void userAlreadyJoinedTheGroup() {
        receiveLineWithTimeout(in);//info message
        out.println("CONN ddgdff");
        out.flush(); //Login first
        receiveLineWithTimeout(in); //OK ddgdff
        out.println("CG uni");
        out.flush();
        receiveLineWithTimeout(in); //OK CG saxion
        out.println("JG uni");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER09"), "User already joined this group: "+serverResponse);
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}