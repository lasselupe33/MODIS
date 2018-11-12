import Messages.GetMsg;

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
                GetMsg msg = new GetMsg(key);
                Socket socket = new Socket(ip, port);

                // Create streams
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                // Send request and await result
                output.writeObject(msg);
                Object result = input.readObject();

                // Once result has been delivered, log it and terminate execution
                System.out.println(result);
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
