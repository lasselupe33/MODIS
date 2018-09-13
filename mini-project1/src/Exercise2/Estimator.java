package Exercise2;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Estimator {
    /** Keep an ordered list of the packets we receive */
    private static ArrayList<String> receivedPackets;
    private static DatagramSocket socket;

    public Estimator() {}

    public static void main(String[] args) {
        int size = Integer.parseInt(args[0]);
        int amount = Integer.parseInt(args[1]);
        int interval = Integer.parseInt(args[2]);
        String host = args[3];
        int destPort = Integer.parseInt(args[4]);

        estimate(size, amount, interval, host, destPort);
    }

    public static void estimate(int datagramSize, int amount, int interval, String host, int destPort) {
        try {
            receivedPackets = new ArrayList<>();

            // Setup socket. We use a timeout of 5 seconds
            socket = new DatagramSocket(7010);
            socket.setSoTimeout(5000);
            InetAddress destIp = InetAddress.getByName(host);

            // Send all packets with an overhead that indicates the index of the packet send (on separate thread)
            sendMessages(datagramSize, amount, interval, destIp, destPort);

            // Start receiving packets on new thread. Bail out after socket timeout exception is thrown
            receiveMessages(datagramSize, amount);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessages(int datagramSize, int amount, int interval, InetAddress destIp, int destPort) {
        new Thread(() -> {
            for (int i = 0; i < amount; i++) {
                String overhead = "" + i;
                byte[] msg = new byte[datagramSize];

                // Insert the overhead into the packet, without altering datagramSize
                System.arraycopy(overhead.getBytes(), 0, msg, datagramSize - overhead.length(), overhead.length());

                // Send the packets with required interval
                setTimeout(() -> {
                    DatagramPacket packet = new DatagramPacket(msg, msg.length, destIp, destPort);
                    try {
                        socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, interval * i);
            }
        }).run();
    }

    private static void receiveMessages(int datagramSize, int amount) {
        new Thread(() -> {
            try {
                while (true) {
                    byte[] buffer = new byte[datagramSize];
                    DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                    socket.receive(received);

                    // Add received packets to list in the order they're received
                    receivedPackets.add(new String(received.getData()));
                }
            } catch (SocketTimeoutException e) {
                // When we get here, then analyse messages received
                analyzeResult(amount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();
    }

    private static void analyzeResult(int amount) {
        HashSet<Integer> uniqueIds = new HashSet<>();
        int duplicates = 0;
        int reordered = 0;
        int prevIndex = -1;

        int index = 0;
        // Go through all received packets in order
        for (String overhead : receivedPackets) {
            int packetIndex = Integer.parseInt(overhead.trim());

            // If our unique ids already contain a packet index, that means we've hit a duplicate package
            if (uniqueIds.contains(packetIndex)) {
                duplicates++;
            } else {
                // ... else, this is a new packet, add it to our uniqueId's
                uniqueIds.add(packetIndex);
                // If packetIndex doesn't correspond to the packets index in the list, the packet isn't in the right place
                if (packetIndex != index) {
                    reordered++;
                }
                index++;
            }
        }

        int lost = amount - uniqueIds.size();

        System.out.println("Lost packets: " + lost + "/" + amount + ", " + (lost * 100) / amount + "%");
        System.out.println("Duplicate packets: " + duplicates + "/" + amount + ", " + (duplicates * 100) / amount + "%");
        System.out.println("Reordered packets: " + reordered);
    }

    private static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }
}
