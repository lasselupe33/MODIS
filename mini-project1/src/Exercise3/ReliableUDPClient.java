package Exercise3;

import java.util.Scanner;

public class ReliableUDPClient {
    public static void main(String[] args) {
        String msg = "Hardcoded string";

        send(msg, "localhost", 7020);
    }

    public static void send(String string, String destIp, int destPort) {
        // Send initial handshake to server


        // Wait for response from server and verify


        // Send msg to server
    }
}
