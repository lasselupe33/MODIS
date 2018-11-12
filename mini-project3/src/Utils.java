import Models.SimpleNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Utils {
    // An array that contains a mapping for all possible values in a hashed string to an index in the array
    public static List<Character> charMapping = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

    /** Test method */
    public static void main(String[] args) throws UnknownHostException {
        String testString = "Hello world";

        String hashed = hashString(testString);
        System.out.println("Hashed value: " + hashed);

        ArrayList<Integer> location = convertHashToLocation(hashed);
        System.out.println("Location of value: " + location);

        String reversedHash = convertLocationToHash(location);
        System.out.println("Location to hash: " + reversedHash);


        // "Tests" for getNearestIndexWithNode
        ArrayList<SimpleNode> testArray = new ArrayList<>();
        SimpleNode testNode = new SimpleNode(InetAddress.getLocalHost(), 2000);

        for (int i = 0; i < 20; i++) {
            testArray.add(null);
        }

        testArray.set(5, testNode);
        testArray.set(9, testNode);

        System.out.println(testArray);
        System.out.println(getNearestIndexWithNode(testArray, 6));
        System.out.println(getNearestIndexWithNode(testArray, 10));
        System.out.println(getNearestIndexWithNode(testArray, 7));
        System.out.println(getNearestIndexWithNode(testArray, 18));
        System.out.println(getNearestIndexWithNode(testArray, 1));

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
     * Helper method that converts a hash value to a location represented in an ArrayList, wherein the first value
     * represents a node in the first layer, the second value a node in the second layer, and so on..
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

    /**
     * Helper that returns the index of an array that is nearest to the returned index, but actually contains a value
     *
     * NB: If two indicies are equally close, then the lower index will be returned
     */
    public static int getNearestIndexWithNode(ArrayList<SimpleNode> array, int i) {
        // Store the highest and lowest index we've visited so far
        int minIndex = array.size() - 1;
        int maxIndex = 0;

        // Specify whether we should check a lower or higher index
        boolean checkLower = true;


        int currTestIndex;
        int j = 0; // Helps specifying what index we should currently be testing


        while (minIndex != 0 || maxIndex != array.size() - 1) {
            // If there's still a lower index and we're currently visiting lower indexes, or if we've reached maxIndex,
            // then check a lower index
            if (maxIndex == array.size() - 1 || (checkLower && minIndex != 0)) {
                currTestIndex = Math.max(0, i - 1 - (j / 2));
            } else {
                // ... else check a higher index than base
                currTestIndex = Math.min(array.size(), i + 1 + (j / 2));
            }

            // If we reach a testIndex that has a node, return the found index!
            if (array.get(currTestIndex) != null) {
                return currTestIndex;
            }

            // Bump up j.
            j++;

            // If we've reached either top or bottom, increment again in order to ensure that the checked index will
            // increase every time
            if (minIndex == 0 || maxIndex == array.size() - 1) {
                j++;
            }

            // Update variables
            minIndex = Math.min(minIndex, currTestIndex);
            maxIndex = Math.max(maxIndex, currTestIndex);
            checkLower = !checkLower;
        }

        // If we get here, then there exists no node in the network, return -1
        return -1;
    }
}
