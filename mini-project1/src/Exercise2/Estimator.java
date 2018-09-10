package Exercise2;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Estimator {
    /** Keep an ordered list of the packets we receive */
    public static ArrayList<String> receivedPackets;

    public Estimator() {}

    public static void estimate(int datagramSize, int amount, int interval, String host, int destPort) {
        try {
            receivedPackets = new ArrayList<>();

            // Setup socket. We use a timeout of 5 seconds
            DatagramSocket socket = new DatagramSocket(7010);
            socket.setSoTimeout(5000);
            InetAddress destIp = InetAddress.getByName(host);

            // Send all packets with an overhead, indicating the index of the packet send
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


            // Receive packets. Bail out after socket timeout exception is thrown
            while (true) {
                byte[] buffer = new byte[datagramSize];
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                socket.receive(received);

                // Add received packets to list in the order they're received
                receivedPackets.add(new String(received.getData()));
            }
        } catch (SocketTimeoutException e) {
            // When we get here, then analyse messages received
            HashSet<Integer> uniqueIds = new HashSet<>();
            int duplicates = 0;
            int reordered = 0;
            int prevIndex = -1;

            // Go through all received packets in order
            for (String overhead : receivedPackets) {
                int packetIndex = Integer.parseInt(overhead.trim());

                // If our unique ids already contain a packet index, that means we've hit a duplicate package
                if (uniqueIds.contains(packetIndex)) {
                    duplicates++;
                } else {
                    // ... else, this is a new packet, add it to our uniqueId's
                    uniqueIds.add(packetIndex);

                    // If our current packet index is larger than the previous, that means our package order between the
                    // two have been flipped, hence we update the amount of reordered packets by two.
                    if (packetIndex < prevIndex) {
                        reordered += 2;
                    }

                    prevIndex = packetIndex;
                }
            }

            int lost = amount - uniqueIds.size();

            System.out.println("Lost packets: " + lost + "/" + amount + ", " + (lost * 100) / amount + "%");
            System.out.println("Duplicate packets: " + duplicates + "/" + amount + ", " + (duplicates * 100) / amount + "%");
            System.out.println("Reordered packets: " + reordered);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setTimeout(Runnable runnable, int delay){
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
