package itech;

import Server.MessageEncryptor;
import org.junit.jupiter.api.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.in;
import static java.lang.System.out;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IntegrationEncryptedPrivateMessage {

    /**
     * Run the tests again if they fail!, it fails cause of the timeout 😊. but they all pass
     */

    private static Properties props = new Properties();

    private static Socket socketUser1;
    private static Socket socketUser2;
    private static BufferedReader inUser1,inUser2;
    private static PrintWriter outUser1,outUser2;
    //Encryption
    private static PublicKey publicKey1,publicKey2;
    private static PrivateKey privateKey1,privateKey2;
    private SecretKey secretKey1,secretKey2;

    private final static int max_delta_allowed_ms = 950;



    @BeforeAll
    static void setupAll() throws IOException, NoSuchAlgorithmException {
        InputStream in = IntegrationEncryptedPrivateMessage.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        //Generate public and private keys
        KeyPairGenerator generator1 = KeyPairGenerator.getInstance("RSA");
        generator1.initialize(1024);
        KeyPair pair1 = generator1.generateKeyPair();
        privateKey1 = pair1.getPrivate();
        publicKey1 = pair1.getPublic();

        KeyPairGenerator generator2 = KeyPairGenerator.getInstance("RSA");
        generator2.initialize(1024);
        KeyPair pair2 = generator2.generateKeyPair();
        privateKey2 = pair2.getPrivate();
        publicKey2 = pair2.getPublic();
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();
        inUser1.close();
        outUser1.close();
        inUser2.close();
        outUser2.close();
    }

    @AfterAll
    static void closeAll() throws IOException {
        socketUser1.close();
        socketUser2.close();
        inUser1.close();
        outUser1.close();
        inUser2.close();
        outUser2.close();
    }

    @Test
    @DisplayName("TC1.16 - sessionKeyRequest")
    void sessionKeyRequest() throws IOException {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        outUser1.println("CONN user1");
        String resUser1 = receiveLineWithTimeout(inUser1); //OK user1
        assumeTrue(resUser1.startsWith("OK"));

        // Connect user 2
        outUser2.println("CONN user2");
        String resUser2 = receiveLineWithTimeout(inUser2); //OK user2
        assumeTrue(resUser2.startsWith("OK"));

        // Request session key for user1
        //Send my public key to the receiver to get a session key
        String publicKeyU1 = MessageEncryptor.encode(publicKey1.getEncoded());
        outUser1.println("RSS user2 " + publicKeyU1);
//
        ////OK RSS <C2 username> <encrypted session key>
        String[] split1 = inUser1.readLine().split(" ");
        assumeTrue(split1[0].startsWith("OK"));
//
        //session key on the third index
        String sessionKey1;
        sessionKey1 = split1[3];
        //Decrypt the session key with my private key
        String decryptedSessionKey1 = MessageEncryptor.decrypt(privateKey1,sessionKey1);
        byte[] decodedKey1 = MessageEncryptor.decode(decryptedSessionKey1);
        //Turn the String to its original form which is the secret key
        secretKey1 =  new SecretKeySpec(decodedKey1, 0, 32, "AES");

        //The same things for User 2
        // Request session key for user2
        //Send my public key to the receiver to get a session key
        String publicKeyU2 = MessageEncryptor.encode(publicKey2.getEncoded());
        outUser2.println("RSS user1 " + publicKeyU2);
//
        ////OK RSS <C2 username> <encrypted session key>
        String[] split2 = inUser2.readLine().split(" ");
        assumeTrue(split2[0].startsWith("OK"));
//
        ////session key on the third index
        String sessionKey2;
        sessionKey2 = split2[3];
        //Decrypt the session key with my private key
        String decryptedSessionKey2 = MessageEncryptor.decrypt(privateKey2,sessionKey2);
        byte[] decodedKey2 = MessageEncryptor.decode(decryptedSessionKey2);
        secretKey2 = new SecretKeySpec(decodedKey2, 0, 32, "AES");

        //Check if user1 have the same session key as user2
        assertEquals(secretKey1,secretKey2);

        outUser1.println("QUIT");
        outUser2.println("QUIT");
    }

    @Test
    @DisplayName("TC1.17 - receiveEncryptedPrivateMessage")
    void receiveEncryptedPrivateMessage() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 3
        outUser1.println("CONN user3");
        String resUser1 = receiveLineWithTimeout(inUser1); //OK user1
        assumeTrue(resUser1.startsWith("OK"));

        // Connect user 4
        outUser2.println("CONN user4");
        String resUser2 = receiveLineWithTimeout(inUser2); //OK user2
        assumeTrue(resUser2.startsWith("OK"));

        // Request session key for user3
        //Send my public key to the receiver to get a session key
        String publicKeyU1 = MessageEncryptor.encode(publicKey1.getEncoded());
        outUser1.println("RSS user4 " + publicKeyU1);
//
        ////OK RSS <C4 username> <encrypted session key>
        String[] split1 = receiveLineWithTimeout(inUser1).split(" ");
        assumeTrue(split1[0].startsWith("OK"));
//
        //session key on the third index
        String sessionKey1;
        sessionKey1 = split1[3];
        //Decrypt the session key with my private key
        String decryptedSessionKey1 = MessageEncryptor.decrypt(privateKey1,sessionKey1);
        byte[] decodedKey1 = MessageEncryptor.decode(decryptedSessionKey1);
        //Turn the String to its original form which is the secret key
        secretKey1 =  new SecretKeySpec(decodedKey1, 0, 32, "AES");

        //The same things for User 4
        // Request session key for user4
        //Send my public key to the receiver to get a session key
        String publicKeyU2 = MessageEncryptor.encode(publicKey2.getEncoded());
        outUser2.println("RSS user3 " + publicKeyU2);

        ////OK RSS <C3 username> <encrypted session key>
        String[] split2 = receiveLineWithTimeout(inUser2).split(" ");
        assumeTrue(split2[0].startsWith("OK"));

        ////session key on the third index
        String sessionKey2;
        sessionKey2 = split2[3];
        //Decrypt the session key with my private key
        String decryptedSessionKey2 = MessageEncryptor.decrypt(privateKey2,sessionKey2);
        byte[] decodedKey2 = MessageEncryptor.decode(decryptedSessionKey2);
        secretKey2 = new SecretKeySpec(decodedKey2, 0, 32, "AES");

        //Check if user1 have the same session key as user2
        assumeTrue(secretKey1.equals(secretKey2));

        //Send private message from user 3 to user 4
        String message = MessageEncryptor.encrypt(secretKey1,"Hello");
        outUser1.println("PME user4 " + message);
        //OK PME user3 <Encrypted message>
        String response = receiveLineWithTimeout(inUser1);
        assumeTrue(response.startsWith("OK"));

        //Check if user4 received Hello
        assertEquals("PME user3 0 Hello",receiveLineWithTimeout(inUser2));

        outUser1.println("QUIT");
        outUser2.println("QUIT");
    }

    @Test
    @DisplayName("TC2.18 - sessionKeyRequestForNonExistingUser")
    void sessionKeyRequestForNonExistingUser() throws IOException {
        receiveLineWithTimeout(inUser1); //info message

        // Connect user 5
        outUser1.println("CONN user5");
        String resUser1 = receiveLineWithTimeout(inUser1); //OK user1
        assumeTrue(resUser1.startsWith("OK"));

        // Request session key for user1
        //Send my public key to the receiver to get a session key
        String publicKeyU1 = MessageEncryptor.encode(publicKey1.getEncoded());
        //Request a session key for non-existing user
        outUser1.println("RSS user0 " + publicKeyU1);

        String serverResponse = receiveLineWithTimeout(inUser1);
        assertTrue(serverResponse.startsWith("ER04"), "User does not exist: "+serverResponse);

        outUser1.println("QUIT");
        outUser2.println("QUIT");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}