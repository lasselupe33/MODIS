import java.net.*;
import java.io.*;
import java.util.Scanner; 

public class UDPClient{
	private static int serverPort = 7007; 


    public static void main(String args[]){ 
		DatagramSocket aSocket = null;
		Scanner msgScan = new Scanner(System.in);

		while(true) { //Keep ask user for messages. 
			try {
			aSocket = new DatagramSocket();
			InetAddress aHost = InetAddress.getByName("localhost");
			String msg = msgScan.nextLine();
			byte [] m = msg.getBytes();		                                                 
			DatagramPacket request =
				new DatagramPacket(m,  msg.length(), aHost, serverPort);
			aSocket.send(request);
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	
			aSocket.receive(reply);
			System.out.println("Reply: " + new String(reply.getData()));
			}catch (SocketException e){System.out.println("Socket: " + e.getMessage());
			}catch (IOException e){System.out.println("IO: " + e.getMessage());
			}finally {if(aSocket != null) aSocket.close();}
		}
	}	      	
}
