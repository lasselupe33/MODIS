package Exercise3;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class ReliableUDPClient {
    public static int currIndex = 0;

    public static void main(String[] args) {
        String msg = "Hardcoded string";

        send(msg, "localhost", 1338);
    }

    public static void send(String string, String destIp, int destPort) {
        try {
            DatagramSocket socket = new DatagramSocket(7007);
            InetAddress hostIp = InetAddress.getByName(destIp);

            // Send initial handshake to server
            String initialMsg = "";
            int clientIndex = currIndex++;

            byte[] initialBytes = Utils.embedOverhead(initialMsg.getBytes(), clientIndex, -1, false);
            DatagramPacket initialPacket = new DatagramPacket(initialBytes, initialBytes.length, hostIp, destPort);
            socket.send(initialPacket);

            // Wait for response from server
            byte[] buffer = new byte[263];
            DatagramPacket packetBuffer = new DatagramPacket(buffer, buffer.length);
            socket.receive(packetBuffer);

            // Verify response
            int[] overhead = Utils.extractOverhead(packetBuffer.getData());

            if (overhead[0] != clientIndex + 1 || overhead[2] == 1) {

                // Error handling (Send packet indicating that everything failed, and start over)

                return;
            }

            // Send msg to server
            int clientAckIndex = overhead[0];
            int serverAckIndex = overhead[1] + 1;
            byte[] actualMessage = Utils.embedOverhead(string.getBytes(), clientAckIndex, serverAckIndex, false);
            DatagramPacket dataPacket = new DatagramPacket(actualMessage, actualMessage.length, packetBuffer.getAddress(), packetBuffer.getPort());
            socket.send(dataPacket);

            // Verify that message was received and all is good :-)
            buffer = new byte[263];
            packetBuffer = new DatagramPacket(buffer, buffer.length);
            socket.receive(packetBuffer);

            if (new String(packetBuffer.getData()).equals("success")) {
                // error handling
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
