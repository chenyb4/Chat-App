package Server;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileChecker {

    /**
     * Convert the file to hex and perform a checksum
     * @param filePath to file
     * @return String containing hex of the file
     * @throws Exception wrong algorithm or io exception
     */

    //This is correct
    public static String getFileChecksum (String filePath) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        // file hashing with DigestInputStream
        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesSize;
            while ((bytesSize = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesSize);
            }
        }
        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * @param checksum of the file before transmission
     * @param checksumToBeComparedWith of the file during transmission
     * @return true if the checksum is the same otherwise false
     */

    public static boolean compareChecksum (String checksum,String checksumToBeComparedWith) {
        return checksum.equals(checksumToBeComparedWith);
    }

}
