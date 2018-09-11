package Exercise3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class ReliableUDPServer {
    private static DatagramSocket socket;
    private static int startIndex = 0;
    private static int serverPort = 1338;

    private static boolean hasPrintedCurrentMessage = false;

    private static byte[] buffer;
    private static DatagramPacket packetBuffer;

    public static void main(String[] args) {
        try {
            socket = new DatagramSocket(serverPort);

            while (true) {
                awaitConnection();
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void awaitConnection() throws IOException {
        // Wait for handshake
        buffer = new byte[255 + 15];
        packetBuffer = new DatagramPacket(buffer, buffer.length);
        socket.receive(packetBuffer);

        HashMap<String, Object> overhead = Utils.extractOverhead(packetBuffer.getData());

        if ((boolean) overhead.get("newTransmission")) {
            hasPrintedCurrentMessage = false;
        } else if (hasPrintedCurrentMessage) {
            closeConnection();
        }

        // Send server handshake once a connection has been established
        sendServerHandshake(packetBuffer);
    }

    private static void sendServerHandshake(DatagramPacket clientHandshake) throws IOException {
        // Send acknowledgement + handshake back
        HashMap<String, Object> overhead = Utils.extractOverhead(clientHandshake.getData());
        int ackIndex = (int) overhead.get("clientIndex") + 1;
        int serverIndex = startIndex;
        startIndex += 10;

        String initialMsg = "";
        byte[] initialMsgBytes = Utils.embedOverhead(initialMsg, ackIndex, serverIndex, false, false, false);
        DatagramPacket initialPacket = new DatagramPacket(initialMsgBytes, initialMsgBytes.length, packetBuffer.getAddress(), packetBuffer.getPort());
        socket.send(initialPacket);

        // Prepare receiving actual data
        receiveData(ackIndex, serverIndex);
    }

    private static void receiveData(int clientAckIndex, int serverIndex) throws IOException {
        // If message already has been printed, then don't attempt to reprint
        if (hasPrintedCurrentMessage) {
            closeConnection();

            return;
        }

        // Wait for message, verify overhead information
        buffer = new byte[255 + 15];
        packetBuffer = new DatagramPacket(buffer, buffer.length);
        socket.receive(packetBuffer);

        HashMap<String, Object> overhead = Utils.extractOverhead(packetBuffer.getData());

        if ((int) overhead.get("clientIndex") != clientAckIndex || (int) overhead.get("serverIndex") != serverIndex + 1) {
            // Error happened, abort! (Client will resend message at a later point)
            restartClient();

            return;
        }

        String actualMsg = Utils.extractMessage(packetBuffer.getData());

        if ((int) overhead.get("hashedString") != actualMsg.hashCode()) {
            // If string doesn't match the hashCode embedded into the overhead, then something must have been corrupted,
            // restart client in hopes things get better
            restartClient();

            return;
        }

        System.out.println("Message: " + actualMsg);
        hasPrintedCurrentMessage = true;

        // Now that we're done receiving data, close the connection
        closeConnection();
    }

    private static void closeConnection() throws IOException {
        // Notify client that everything went well
        byte[] msg = Utils.embedOverhead("", -1, -1, false, true, false);
        DatagramPacket successPacket = new DatagramPacket(msg, msg.length, packetBuffer.getAddress(), packetBuffer.getPort());
        socket.send(successPacket);
    }

    private static void restartClient() throws IOException {
        // Notify client that everything went well
        byte[] msg = Utils.embedOverhead("", -1, -1, false, false, true);
        DatagramPacket retryPacket = new DatagramPacket(msg, msg.length, packetBuffer.getAddress(), packetBuffer.getPort());
        socket.send(retryPacket);
    }


    // NOTES:

    // Client-to-server handshake seq x
    // Server-to-client handshake ack seq x+1, seq y
    // Client-to-server (with data) seq x+1, seq y+1
}
