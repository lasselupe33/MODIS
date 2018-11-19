import Messages.TraverseMessages.GetMsg;
import Messages.TraverseMessages.PutMsg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class GetClient {
    public static void main(String[] args) {
        try {
            if (args.length >= 2) {
                // Extract arguments
                int key = Integer.parseInt(args[0]);
                int port = Integer.parseInt(args[1]);
                InetAddress ip = args.length == 3 ? InetAddress.getByName(args[2]) : InetAddress.getLocalHost();

                // Get message to send, and then begin listening
                //Utils.keyValueDebugInfo(key, "");
                Socket socket = new Socket(ip, port);
                GetMsg msg = new GetMsg(key, socket.getPort(), socket.getInetAddress());

                // Send request and await result
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(msg);

                // Await result
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                PutMsg result = (PutMsg) input.readObject();

                // Once result has been delivered, log it and terminate execution
                if (result != null) {
                    System.out.println(result.value);
                } else {
                    System.out.println("No resource was found/mixed values has been set with given key");
                }

                socket.close();
            } else {
                System.out.println("Invalid arguments supplied, I require a key, a port and optionally an ip");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
