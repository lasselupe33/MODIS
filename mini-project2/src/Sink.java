import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class Sink {
    public static int EventPort = 2000;
    public static InetAddress EventIP;

    private static Socket socket;
    private static BufferedReader inFromManager;

    public static void main(String[] args) {
        try {
            // Setup eventIP once the sink is created
            EventIP = InetAddress.getLocalHost();

            // Subscribe to events on creation
            subscribe();

            // Ensure unsubscribe is run once the process is termminated
            Runtime.getRuntime().addShutdownHook(unsubscribe());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static void subscribe() {
        try {
            socket = new Socket(EventIP, EventPort);
            inFromManager = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            listen();

            // Now that we've subscribed to the manager, begin listening to
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listen() {
        while (true) {
            try {
                String msg = inFromManager.readLine();

                // If msg is ever null that means the connection has been closed by the Manager, hence we wish to stop
                // listening..
                if (msg == null) {
                    break;
                }

                System.out.println(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Unsubscribe on destroy
    private static Thread unsubscribe() {
        return new Thread(() -> {
            try {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();

                System.out.println("bye");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}