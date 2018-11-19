import Messages.TraverseMessages.PutMsg;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PutClient {

    /**
     * Send a put message with specified key and value to note at specified port and ip
     */
    public static void main(String[] args) {
        try {
            if (args.length >= 3) {
                // Set key, value, port and ip
                int key = Integer.parseInt(args[0]);
                String value = args[1];
                int port = Integer.parseInt(args[2]);
                InetAddress ip = args.length == 4 ? InetAddress.getByName(args[3]) : InetAddress.getLocalHost();

                // Create new Put Message
                PutMsg msg = new PutMsg(key, value);
                //Utils.keyValueDebugInfo(key, value);

                // Send message to node at given port and ip
                Socket socket = new Socket(ip, port);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(msg);
                socket.close();
            } else {
                System.out.println("Invalid arguments applied. You need to specify key, value, " +
                        "port and ip in this order. If no ip is specified localhost is used.");
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
