package Server;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

public class MessageEncryptor {

    /**
     * Encrypt the message with receiver's public key
     * @param publicKey of the receiver
     * @param secretKey to be encrypted
     * @return the encrypted message
     */

    //Asymmetric encryption
    public static String encrypt (PublicKey publicKey, String secretKey) {
        try {
            byte[] messageToBytes = secretKey.getBytes();
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
     * @param secretKey to be decrypted
     * @return decrypted message
     */

    //Asymmetric decryption
    public static String decrypt (PrivateKey privateKey, String secretKey) {
        try {
            byte[] encryptedBytes = decode(secretKey);
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
     * Encrypt messages with symmetric key (session key)
     * @param secretKey which is the session key
     * @param message to be encrypted
     * @return the encrypted message
     */

    //Symmetric encryption
    public static String encrypt (SecretKey secretKey, String message) {
        try {
            byte[] messageToBytes = message.getBytes();
            //Algorithm/mode/padding
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            byte[] encryptedBytes = cipher.doFinal(messageToBytes);
            return encode(encryptedBytes);
        } catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("In encrypt");
        }
        return null;
    }

    /**
     * Decrypt messages with symmetric key (session key)
     * @param secretKey which is the session key
     * @param encryptedMessage to be decrypted
     * @return the decrypted message
     */

    //Symmetric decryption
    public static String decrypt (SecretKey secretKey,String encryptedMessage) {
        try {
            byte[] encryptedBytes = decode(encryptedMessage);
            //Algorithm/mode/padding
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE,secretKey);
            byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
            return new String(decryptedMessage, StandardCharsets.UTF_8);
        } catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("In decrypt");
        }
        return null;
    }

    /**
     * @param data to be encoded
     * @return the encoded data
     */

    public static String encode (byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * @param data to be decoded
     * @return the decoded data
     */

    public static byte[] decode (String data) {
        return Base64.getDecoder().decode(data);
    }
}
