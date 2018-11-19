package Messages.TraverseMessages;

import java.io.Serializable;

public class ReturnMsg extends PutMsg implements Serializable {
    public ReturnMsg(int key, String value) {
        super(key, value);
    }
}
