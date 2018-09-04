package Exercise1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;

public class QuestionableDatagramSocket extends DatagramSocket {
    DatagramPacket currentPacket;
    boolean shouldReorder = false;

    public QuestionableDatagramSocket() throws SocketException {
        super();
    }

    @Override
    public void send(DatagramPacket p) throws IOException {
        Random randomizer = new Random();
        int outcome = randomizer.nextInt(4);

        System.out.println(outcome);

        switch (outcome) {
            // Duplicate
            // Send twice

            // Reorder
            case 2:
                currentPacket = p;
                shouldReorder = true;
                break;


            // Wait for another packet to arrive

            // Discard
            // Do nothing

            // return;
            // Send
        }
    }

    private void actualSend(DatagramPacket p) {
        //send
    }
}
