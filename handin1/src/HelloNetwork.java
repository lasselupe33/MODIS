import java.io.IOException;
import java.net.*;
import java.sql.SQLOutput;

public class HelloNetwork {
    private static int serverPort = 7008;

    public static void main(String[] args) {
        DatagramSocket socket = null;

        while (true) {
            try {
                socket = new DatagramSocket(serverPort);

                /** Wait for packet from user */
                byte[] buffer = new byte[1000];
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                socket.receive(received);
                System.out.println(new String(received.getData()).trim());

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
    }
}
