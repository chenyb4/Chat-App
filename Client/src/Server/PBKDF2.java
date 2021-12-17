package Server;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class PBKDF2 {

    public static byte[] hashPassword (final char[] password, final byte[] salt, final int iterations, final int keyLength) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
            PBEKeySpec spec = new PBEKeySpec( password, salt, iterations, keyLength );
            SecretKey key = skf.generateSecret(spec);
            return key.getEncoded();
        } catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static String toHex (byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    public static boolean comparePassword (String password, String inputtedPassword) {
        String temp1 = initializeVariables(password);
        String temp2 = initializeVariables(inputtedPassword);
        return temp1.equals(temp2);
    }

    public static String initializeVariables(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        int iterations = 65536;
        int keyLength = 128;
        byte[] hashedBytes = PBKDF2.hashPassword(password.toCharArray(),salt,iterations,keyLength);
        password = PBKDF2.toHex(hashedBytes);
        return password;
    }
}
