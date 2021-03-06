import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Source {
    private static int port = 3000;
    private static Socket sourceSocket;
    private static DataOutputStream outToManager;

    public static void main(String[] args) {
        try {
            Runtime.getRuntime().addShutdownHook(shutDownSource());

            sourceSocket = new Socket("localhost", port);

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            outToManager = new DataOutputStream(sourceSocket.getOutputStream());

            // Read line from console and push message to manager
            while (true) {
                String message = inFromUser.readLine();

                outToManager.writeBytes(message);
                outToManager.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Thread shutDownSource() {
        return new Thread(() -> {
            try {
                sourceSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}