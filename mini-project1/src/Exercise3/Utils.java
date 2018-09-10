package Exercise3;

import java.nio.ByteBuffer;

public class Utils {
    public static byte[] embedOverhead(byte[] msg, int clientIndex, int serverIndex, boolean didError) {
        byte[] completeMsg = new byte[msg.length + 4 * 2 + 1];
        byte[] clientBytes = intToBytes(clientIndex);
        byte[] serverBytes = intToBytes(serverIndex);

        // Store msg
        for (int i = 0; i < completeMsg.length; i++) {
            if (i < 4) {
                completeMsg[i] = clientBytes[i];
            } else if (i < 8) {
                completeMsg[i] = serverBytes[i - 4];
            } else if (i == 8) {
                completeMsg[i] = didError ? (byte) 1 : (byte) 0;
            } else {
                completeMsg[i] = msg[i - 9];
            }
        }

        return completeMsg;
    }

    public static byte[] intToBytes(int i) {
        {
            byte[] result = new byte[4];

            result[0] = (byte) (i >> 24);
            result[1] = (byte) (i >> 16);
            result[2] = (byte) (i >> 8);
            result[3] = (byte) (i);

            return result;
        }
    }

    public static int[] extractOverhead(byte[] msg) {
        byte[] clientBytes = new byte[4];
        byte[] serverBytes = new byte[4];
        int didError = msg[8];

        for(int i = 0; i < 8; i++) {
            if (i < 4) {
                clientBytes[i] = msg[i];
            } else {
                serverBytes[i - 4] = msg[i];
            }
        }

        int clientIndex = fromByteArray(clientBytes);
        int serverIndex = fromByteArray(serverBytes);

        return new int[] {clientIndex, serverIndex, didError };
    }

    public static String extractMessage(byte[] msg) {
        byte[] msgBytes = new byte[msg.length - 9];

        for (int i = 9; i < msg.length; i++) {
            msgBytes[i - 9] = msg[i];
        }

        return new String(msgBytes);
    }

    private static int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
