import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

class Manager {
    private static CopyOnWriteArrayList<Socket> subscribers = new CopyOnWriteArrayList();
    private static ServerSocket sinkSocket;
    private static ServerSocket sourceSocket;

    public static void main(String[] args){
        try {
            sinkSocket = new ServerSocket(2000);
            sourceSocket = new ServerSocket(3000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        incomingSubscriptions().start();
        incomingSource().start();
    }

    // Should have two threads.
    // 1. dedicated to keeping track of the list of subscribers
    public static Thread incomingSubscriptions() {
        return new Thread(() -> {
            try {

                while (true) {
                    Socket connectionSocket = sinkSocket.accept();
                    subscribers.add(connectionSocket);
                    System.out.println("Sink connected");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    // 2. dedicated to receive messages from publishers and relaying them to all notifiers
    public static Thread incomingSource() {
        return new Thread(() -> {
            try {
                while (true) {
                    Socket connectionSocket = sourceSocket.accept();
                    System.out.println("Source added");

                    broadcastSourceMessages(connectionSocket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static Thread broadcastSourceMessages(Socket publisherSocket) {
        return new Thread(() -> {
            while (true) {
                try {
                    BufferedReader inFromSource = new BufferedReader(new InputStreamReader(publisherSocket.getInputStream()));

                    String messageFromSource = inFromSource.readLine();
                    DataOutputStream outToSink;

                    // If messageFromSource is ever null, then it means that the connection has been closed on the
                    // publisher side, hence we wish to stop.
                    if (messageFromSource == null) {
                        break;
                    }

                    for (Socket sink : subscribers) {
                        // If a sinks OutputStream throws an IOException, remove it from subscriptions, since that
                        // means the connection has been closed on the subscriber-side
                        try {
                            outToSink = new DataOutputStream(sink.getOutputStream());

                            outToSink.writeBytes(messageFromSource);
                            outToSink.write('\n');
                        } catch (IOException e) {
                            System.out.println("Closed sink connection removed with address " + sink.getInetAddress() + " and port " + sink.getPort());
                            subscribers.remove(sink);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}