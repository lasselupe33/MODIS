import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

class Manager {
    private static CopyOnWriteArrayList<Socket> subscribers = new CopyOnWriteArrayList();

    public static void main(String[] args){
        incomingSubscriptions().run();
        incomingSource().run();
    }

    // Should have two threads.
    // 1. dedicated to keeping track of the list of subscribers

    public static Thread incomingSubscriptions() {
        return new Thread(() -> {
            try {
                ServerSocket managerSocket = new ServerSocket(2000);

                while (true) {
                    Socket connectionSocket = managerSocket.accept();
                    subscribers.add(connectionSocket);
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
                ServerSocket managerSocket = new ServerSocket(2000);

                while (true) {
                    Socket connectionSocket = managerSocket.accept();
                    BufferedReader inFromSource = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    String messageFromSource = inFromSource.readLine();
                    System.out.println(messageFromSource);
                    DataOutputStream outToSink;

                    for (Socket sink : subscribers) {
                        // If a sinks OutputStream returns null, remove it from subscriptions
                        try {
                            outToSink = new DataOutputStream(sink.getOutputStream());
                            outToSink.writeBytes(messageFromSource);
                        } catch (IOException e) {
                            subscribers.remove(sink);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}