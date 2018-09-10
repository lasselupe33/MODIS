package Exercise1;

import java.io.IOException;
import java.net.*;

public class Client {
    private static int serverPort = 7008;

    public static void main (String[] args) {
        DatagramSocket socket = null;


            try {
                socket = new QuestionableDatagramSocket();
                InetAddress host = InetAddress.getByName("localhost");

                /** Send response to user */
                String msg1 = "Hello";
                String msg2 = "Goodbye";

                DatagramPacket packet1 = new DatagramPacket(msg1.getBytes(), msg1.length(), host, serverPort);
                DatagramPacket packet2 = new DatagramPacket(msg2.getBytes(), msg2.length(), host, serverPort);
                socket.send(packet1);

                Thread.sleep(1000);
                socket.send(packet2);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }

    }
}
