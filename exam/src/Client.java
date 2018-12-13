import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) {
        try {
            if (args.length >= 2) {
                // Extract arguments
                String type = args[0];
                int port = Integer.parseInt(args[1]);
                InetAddress ip = args.length == 3 ? InetAddress.getByName(args[2]) : InetAddress.getLocalHost();

                sendRequest(type, port, ip);
            } else {
                System.out.println("Invalid arguments supplied, I require a type, a port and optionally an ip");
            }
        } catch(UnknownHostException e1){
            e1.printStackTrace();
        }

    }

    public static void sendRequest(String type, int port) {
        try {
            sendRequest(type, port, InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void GET(int port) {
        sendRequest("GET", port);
    }

    public static void GET(int port, InetAddress ip) {
        sendRequest("INC", port, ip);
    }

    public static void INC(int port) {
        sendRequest("INC", port);
    }

    public static void INC(int port, InetAddress ip) {
        sendRequest("INC", port, ip);
    }

    private static void sendRequest(String type, int port, InetAddress ip) {
        try {
                // Get message to send, and then begin listening
                Socket socket = new Socket(ip, port);
                socket.setSoTimeout(10000);

                // Send request and await result
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                switch (type) {
                    case "GET":
                        output.writeObject("GET");
                        break;

                    case "INC":
                        output.writeObject("INC");
                        break;

                    default:
                        System.out.println("Invalid type argument, returning...");
                        return;
                }



                // Await result
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                Integer result = (Integer) input.readObject();

                // Once result has been delivered, log it and terminate execution
                if (result != null) {
                    System.out.println("Got the following value via a " + type + " command: " + result + ". This command was sent to " + ip + "@" + port);
                } else {
                    System.out.println("Failed to connect to node at " + ip + "@" + port);
                }

                socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot reach node at " + ip + "@" + port);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
