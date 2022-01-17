package Server;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class MessageEncryptor {

    /**
     * Encrypt the message with receiver's public key
     * @param publicKey of teh receiver
     * @param message to be encrypted
     * @return the encrypted message
     */

    public static String encrypt (PublicKey publicKey, String message) {
        try {
            byte[] messageToBytes = message.getBytes();
            //Algorithm/mode/padding
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,publicKey);
            byte[] encryptedBytes = cipher.doFinal(messageToBytes);
            return encode(encryptedBytes);
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * Decrypt the message with the receiver private key
     * @param privateKey of the receiver
     * @param encryptedMessage to be decrypted
     * @return decrypted message
     */

    public static String decrypt (PrivateKey privateKey, String encryptedMessage) {
        try {
            byte[] encryptedBytes = decode(encryptedMessage);
            //Algorithm/mode/padding
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE,privateKey);
            byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
            return new String(decryptedMessage, StandardCharsets.UTF_8);
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * @param data to be encoded
     * @return the encoded data
     */

    private static String encode (byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * @param data to be decoded
     * @return the decoded data
     */

    private static byte[] decode (String data) {
        return Base64.getDecoder().decode(data);
    }
}
