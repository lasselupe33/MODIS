import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Utils {
    // An array that contains a mapping for all possible values in a hashed string to an index in the array
    public static List<Character> charMapping = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

    /** Test method */
    public static void main(String[] args) {
        String testString = "Hello world";

        String hashed = hashString(testString);
        System.out.println("Hashed value: " + hashed);

        ArrayList<Integer> location = convertHashToLocation(hashed);
        System.out.println("Location of value: " + location);

        String reversedHash = convertLocationToHash(location);
        System.out.println("Location to hash: " + reversedHash);
    }

    // SHA-256 Hashing - borrowed from https://stackoverflow.com/questions/2624192/good-hash-function-for-strings
    public static String hashString(String s) {
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(s.getBytes());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                sb.append(0);
                sb.append(hex.charAt(hex.length() - 1));
            } else {
                sb.append(hex.substring(hex.length() - 2));
            }
        }
        return sb.toString();
    }

    /**
     * Helper method that converts a hash value to a location represted in an ArrayList, wherein the first value
     * represents in the first layer, the second value a node on the second layer, and so on..
     */
    public static ArrayList<Integer> convertHashToLocation(String hash) {
        // Lowercase hash to ensure that it's at the correct format
        String preparedHash = hash.toLowerCase();
        ArrayList<Integer> location = new ArrayList<>();

        // Get the index of each character and add to location
        for (char c : preparedHash.toCharArray()) {
            location.add(convertCharToIndex(c));
        }

        return location;
    }

    /** Reverse of convertHashToLocation */
    public static String convertLocationToHash(ArrayList<Integer> location) {
        StringBuilder builder = new StringBuilder();

        for (int charIndex : location) {
            builder.append(convertIndexToChar(charIndex));
        }

        return builder.toString();
    }

    /** Helper that returns an index to the char that it represents in the network */
    public static char convertIndexToChar(int i) {
        return charMapping.get(i);
    }

    /** Helper that returns the character associated with a given index in the network */
    public static int convertCharToIndex(char c) {
        return charMapping.indexOf(c);
    }
}
