package Messages.ValueMessages;

import java.io.Serializable;
import java.net.Socket;

public class IncrementValueMsg implements Serializable {
    public Socket responseSocket;

    public IncrementValueMsg(Socket responseSocket) {
        this.responseSocket = responseSocket;
    }
}
