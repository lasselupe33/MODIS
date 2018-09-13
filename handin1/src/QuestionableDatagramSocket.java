
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

    public QuestionableDatagramSocket(int port) throws SocketException {
        super(port);
    }

    @Override
    public void send(DatagramPacket p) throws IOException {
        // If shouldReorder send this and previous packet interchanged
        if (shouldReorder) {
            try {
                sendReorder(p);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }

        // Pick random number between 0 and 3 (both included) to determine what to do with the received datagrams
        Random randomizer = new Random();
        int outcome = randomizer.nextInt(4);

        // Choose way to send packet based on outcome
        switch (outcome) {
            // Discard
            case 0:
                discard();
                break;

            // Duplicate
            case 1:
                try {
                    sendDuplicate(p);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;

            // Reorder
            case 2:
                reorder(p);
                break;

            // Send
            case 3:
                sendOriginal(p);
                break;
        }
    }

    // Do nothing
    private void discard() {
        System.out.println("Discard");
        return;
    }

    // Send the message twice
    private void sendDuplicate(DatagramPacket p) throws IOException, InterruptedException {
        System.out.println("Duplicate");

        super.send(p);

        // Send packet again
        Thread.sleep(500);
        super.send(p);
    }

    // Save packet so it can be interchanged with the next message to be send
    private void reorder(DatagramPacket p) throws IOException {
        currentPacket = p;
        shouldReorder = true;
    }

    // Send packet and then send previous packet
    private void sendReorder(DatagramPacket p) throws IOException, InterruptedException {
        System.out.println("Reorder");

        // Send package
        super.send(p);

        // Send previous package
        Thread.sleep(500);
        super.send(currentPacket);

        // Reset fields
        shouldReorder = false;
        currentPacket = null;
    }

    // Send the original message back without altering it
    private void sendOriginal(DatagramPacket p) throws IOException {
        System.out.println("Send");
        super.send(p);
    }

}
