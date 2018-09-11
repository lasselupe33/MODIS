package Exercise3;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class Utils {
    public static byte[] embedOverhead(String msg, int clientIndex, int serverIndex, boolean newTransmission, boolean isFinished, boolean shouldRestart) {
        byte[] msgBytes = msg.getBytes();
        byte[] completeMsg = new byte[msgBytes.length + 4 * 3 + 3]; // Overhead contains 3 ints and 3 booleans
        byte[] clientBytes = intToBytes(clientIndex);
        byte[] serverBytes = intToBytes(serverIndex);
        byte[] hashedString = intToBytes(msg.hashCode());

        // Store msg
        for (int i = 0; i < completeMsg.length; i++) {
            if (i < 4) {
                completeMsg[i] = clientBytes[i];
            } else if (i < 8) {
                completeMsg[i] = serverBytes[i - 4];
            } else if (i < 12) {
                completeMsg[i] = hashedString[i - 8];
            } else if (i == 12) {
                completeMsg[i] = newTransmission ? (byte) 1 : (byte) 0;
            } else if (i == 13) {
                completeMsg[i] = isFinished ? (byte) 1 : (byte) 0;
            } else if (i == 14) {
                completeMsg[i] = shouldRestart ? (byte) 1 : (byte) 0;
            } else {
                completeMsg[i] = msgBytes[i - 15];
            }
        }

        return completeMsg;
    }

    public static HashMap<String, Object> extractOverhead(byte[] msg) {
        byte[] clientBytes = new byte[4];
        byte[] serverBytes = new byte[4];
        byte[] hashedBytes = new byte[4];
        boolean newTransmission = msg[12] == 1 ? true : false;
        boolean isFinished = msg[13] == 1 ? true : false;
        boolean shouldRestart = msg[14] == 1 ? true : false;

        for(int i = 0; i < 12; i++) {
            if (i < 4) {
                clientBytes[i] = msg[i];
            } else if (i < 8) {
                serverBytes[i - 4] = msg[i];
            } else {
                hashedBytes[i - 8] = msg[i];
            }
        }

        int clientIndex = fromByteArray(clientBytes);
        int serverIndex = fromByteArray(serverBytes);
        int hashedString = fromByteArray(hashedBytes);

        HashMap<String, Object> overhead = new HashMap<>();
        overhead.put("clientIndex", clientIndex);
        overhead.put("serverIndex", serverIndex);
        overhead.put("hashedString", hashedString);
        overhead.put("newTransmission", newTransmission);
        overhead.put("isFinished", isFinished);
        overhead.put("shouldRestart", shouldRestart);

        return overhead;
    }

    public static String extractMessage(byte[] msg) {
        byte[] msgBytes = new byte[msg.length - 15];

        for (int i = 15; i < msg.length; i++) {
            msgBytes[i - 15] = msg[i];
        }

        return new String(msgBytes).trim();
    }

    private static byte[] intToBytes(int i) {
        {
            byte[] result = new byte[4];

            result[0] = (byte) (i >> 24);
            result[1] = (byte) (i >> 16);
            result[2] = (byte) (i >> 8);
            result[3] = (byte) (i);

            return result;
        }
    }

    private static int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
