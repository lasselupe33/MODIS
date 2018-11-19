package Messages.TraverseMessages;

import java.io.Serializable;

public class TransferMsg implements Serializable {
    public int key;

    public TransferMsg(int key) {
        this.key = key;
    }
}
