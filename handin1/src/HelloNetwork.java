import java.io.IOException;
import java.net.*;

public class HelloNetwork {
    private static int serverPort = 7007;

    public static void main(String[] args) {
        DatagramSocket socket = null;

        while (true) {
            try {
                socket = new DatagramSocket(7007);

                /** Wait for packet from user */
                byte[] buffer = new byte[1000];
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                socket.receive(received);

                /** Create response packet */
                String responseMsg = new String(received.getData());
                InetAddress address = received.getAddress();
                int port = received.getPort();

                /** Send response to user */
                DatagramPacket response = new DatagramPacket(responseMsg.getBytes(), responseMsg.length(), address, port);
                socket.send(response);
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
