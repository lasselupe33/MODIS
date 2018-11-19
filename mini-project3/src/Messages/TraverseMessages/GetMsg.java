package Messages.TraverseMessages;

import java.io.Serializable;
import java.net.InetAddress;

public class GetMsg extends TransferMsg implements Serializable {
    public int port;
    public InetAddress ip;

    public GetMsg(int key, int port, InetAddress ip) {
        super(key);
        this.port = port;
        this.ip = ip;
    }
}
