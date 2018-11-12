package Messages;

import java.io.Serializable;

public class InsertResourceInNearestIndexMsg implements Serializable {
    public PutMsg putMsg;

    public InsertResourceInNearestIndexMsg(PutMsg putMsg) {
        this.putMsg = putMsg;
    }
}
