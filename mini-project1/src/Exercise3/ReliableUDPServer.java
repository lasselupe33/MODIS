package Exercise3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ReliableUDPServer {
    public static int currentIndex = 0;
    public static int serverPort = 1338;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(serverPort);

            // Wait for handshake
            byte[] buffer = new byte[263];
            DatagramPacket packetBuffer = new DatagramPacket(buffer, buffer.length);
            socket.receive(packetBuffer);

            // Send acknowledgement + handshake back
            int[] overhead = Utils.extractOverhead(packetBuffer.getData());
            int ackIndex = overhead[0] + 1;
            int serverIndex = currentIndex++;
            String initialMsg = "";

            byte[] initialMsgBytes = Utils.embedOverhead(initialMsg.getBytes(), ackIndex, serverIndex);
            DatagramPacket initialPacket = new DatagramPacket(initialMsgBytes, initialMsgBytes.length, packetBuffer.getAddress(), packetBuffer.getPort());
            socket.send(initialPacket);

            // Wait for message, verify overhead information
            buffer = new byte[263];
            packetBuffer = new DatagramPacket(buffer, buffer.length);
            socket.receive(packetBuffer);

            overhead = Utils.extractOverhead(packetBuffer.getData());

            if (overhead[0] != ackIndex || overhead[1] != serverIndex + 1) {

                // så kan du skrive en kommentar herinde: "Error handling"
            }

            String actualMsg = Utils.extractMessage(packetBuffer.getData());
            System.out.println("Client index: " + overhead[0] + " Server index: " + overhead[1] + "\n Message: " + actualMsg);

            // Notify client that everything went well
            String success = "success";
            DatagramPacket succesPacket = new DatagramPacket(success.getBytes(), success.length(), packetBuffer.getAddress(), packetBuffer.getPort());
            socket.send(succesPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // NOTES:

    // Client-to-server handshake seq x
    // Server-to-client handshake ack seq x+1, seq y
    // Client-to-server (with data) seq x+1, seq y+1
}
