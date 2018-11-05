package Models;

import java.io.Serializable;
import java.net.InetAddress;

public class SimpleNode implements Serializable {
    public InetAddress ip;
    public int port;

    public SimpleNode(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
