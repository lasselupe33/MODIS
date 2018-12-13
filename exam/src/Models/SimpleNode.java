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

    @Override
    public String toString() {
        return "SimpleNode " + this.ip + "@" + this.port;
    }

    @Override
    public boolean equals(Object obj) {
        SimpleNode compareTo = (SimpleNode) obj;

        return this.ip.equals(compareTo.ip) && this.port == compareTo.port;
    }
}
