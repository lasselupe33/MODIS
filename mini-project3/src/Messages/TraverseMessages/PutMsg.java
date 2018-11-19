package Messages.TraverseMessages;

import java.io.Serializable;

public class PutMsg extends TransferMsg implements Serializable {
    public String value;

    public PutMsg(int key, String value) {
        super(key);
        this.value = value;
    }
}
