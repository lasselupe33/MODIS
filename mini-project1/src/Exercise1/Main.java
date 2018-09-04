package Exercise1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws IOException {
        QuestionableDatagramSocket socket = new QuestionableDatagramSocket();

        String msg = "Hej";

        DatagramPacket p = new DatagramPacket(msg.getBytes(), msg.length());
        socket.send(p);
    }
}
