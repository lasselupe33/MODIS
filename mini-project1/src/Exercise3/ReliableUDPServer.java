package Exercise3;

public class ReliableUDPServer {
    public static void main(String[] args) {
        // Wait for handshake

        // Send acknowledgement + handshake back

        // Wait for message, verify overhead information
    }



    // NOTES:

    // Client-to-server handshake seq x
    // Server-to-client handshake ack seq x+1, seq y
    // Client-to-server (with data) seq x+1, seq y+1
}
