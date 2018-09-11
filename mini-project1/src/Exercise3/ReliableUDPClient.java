package Exercise3;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class ReliableUDPClient {
    private static DatagramSocket socket;
    private static String msg;
    private static String destIp;
    private static int destPort;

    public static void main(String[] args) {
        msg = args[0];
        destIp = args[1];
        destPort = Integer.parseInt(args[2]);

        try {
            // Attempt to setup socket
            socket = new DatagramSocket(7007);
            socket.setSoTimeout(5000);

            // Send the message!
            send(false);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void send(boolean shouldRetry) {
        try {
            boolean isNewConnection = !shouldRetry;

            sendClientHandshake(isNewConnection);
        } catch (SocketTimeoutException e) {
            // Retry if our socket times out at any given time
            resend("Socket timeout");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendClientHandshake(boolean isNewConnection) throws IOException {
        // Send initial handshake to server
        String initialMsg = "";
        int clientIndex = 0;
        InetAddress hostIp = InetAddress.getByName(destIp);

        byte[] initialBytes = Utils.embedOverhead(initialMsg, clientIndex, -1, isNewConnection, false, false);
        DatagramPacket initialPacket = new DatagramPacket(initialBytes, initialBytes.length, hostIp, destPort);
        socket.send(initialPacket);

        // Now, prepare receiving server handshake
        receiveServerHandshake(clientIndex);
    }

    private static void receiveServerHandshake(int clientIndex) throws IOException {
        // Wait for response from server
        byte[] buffer = new byte[255 + 15];
        DatagramPacket packetBuffer = new DatagramPacket(buffer, buffer.length);
        socket.receive(packetBuffer);

        // Verify response
        HashMap<String, Object> overhead = Utils.extractOverhead(packetBuffer.getData());

        if((boolean) overhead.get("isFinished")) {
            // If we somehow already finished this transmission, don't attempt to resend
            return;
        }

        if ((boolean) overhead.get("shouldRestart")) {
            resend("Server has failed somewhere along the line, resend message..");

            return;
        }

        if ((int) overhead.get("clientIndex") != clientIndex + 1) {
            // We encountered an error, retry!
            resend("Server didn't return proper acknowledgement, something must have gone wrong");

            return;
        }

        // Nothing abnormal happened, continue to sending data
        sendData(packetBuffer, overhead);
    }

    private static void sendData(DatagramPacket serverHandshake, HashMap<String, Object> serverHandshakeOverhead) throws IOException {
        // Send msg to server
        int clientAckIndex = (int) serverHandshakeOverhead.get("clientIndex");
        int serverAckIndex = (int) serverHandshakeOverhead.get("serverIndex") + 1;
        byte[] actualMessage = Utils.embedOverhead(msg, clientAckIndex, serverAckIndex, false, false, false);
        DatagramPacket dataPacket = new DatagramPacket(actualMessage, actualMessage.length, serverHandshake.getAddress(), serverHandshake.getPort());
        socket.send(dataPacket);

        // Begin verifying that data was properly printed
        verifyCompletion();
    }

    private static void verifyCompletion() throws IOException {
        // Verify that message was received and all is good :-)
        byte[] buffer = new byte[255 + 15];
        DatagramPacket packetBuffer = new DatagramPacket(buffer, buffer.length);
        socket.receive(packetBuffer);

        HashMap<String, Object> overhead = Utils.extractOverhead(packetBuffer.getData());

        if ((boolean) overhead.get("shouldRestart")) {
            resend("Server indicates that transmission should restart.. Perhaps msg got corrupted?");

            return;
        }

        // If everything didn't go as expected, retry
        if (!(boolean) overhead.get("isFinished")) {
            // If we somehow didn't manage to send the message attempt once more
            resend("Server never indicated that transmission finished");
        }
    }

    private static void resend(String errorMessage) {
        try {
            System.out.println("Error occurred, resending..");
            System.out.println(errorMessage);

            // Wait a while before retrying
            Thread.sleep(1000);
            send(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
