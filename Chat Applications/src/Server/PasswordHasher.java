package Server;

import Server.Data.DataProvider;
import Server.Model.Client;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PasswordHasher {

    //Store user's salt in a hashmap to be identified with
    final static Map<String,byte[]> salts = new HashMap<>();

    /**
     * @param password to be hashed
     * @param salt rounds
     * @param iterations rounds
     * @param keyLength of the password
     * @return the bytes which is encoded
     */

    private static byte[] hashPassword (final char[] password, final byte[] salt, final int iterations, final int keyLength) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            return key.getEncoded();
        } catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * Convert from bytes to hex
     * @param array of bytes to be converted from
     * @return hex string
     */

    private static String toHex (byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    /**
     * Hash the password
     * @param password to be hashed
     * @return String as a hash
     */

    public static String toHash(String username,String password) {
        salts.putIfAbsent(username,getNextSalt());
        byte[] nextSalt = salts.get(username);
        int iterations = 65536;
        int keyLength = 128;
        byte[] hashedBytes = hashPassword(password.toCharArray(), nextSalt, iterations, keyLength);
        return toHex(hashedBytes);
    }

    /**
     * Compare the passwords
     * @param password old password
     * @param inputtedPassword new password
     * @return true if it does match, otherwise false
     */

    public static boolean comparePassword (String password, String username,String inputtedPassword) {
        //Password is already hashed, inputted password is not
        return password.equals(toHash(username,inputtedPassword));
    }

    /**
     * Generate random salt per user
     * @return salt byte[]
     */

    public static byte[] getNextSalt() {
        //Unique salt per user
        Random secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
