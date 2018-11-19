package Messages.ResourceHandlingMessages;

import Messages.TraverseMessages.PutMsg;

import java.io.Serializable;

public class InsertResourceInNearestIndexMsg implements Serializable {
    public PutMsg putMsg;

    public InsertResourceInNearestIndexMsg(PutMsg putMsg) {
        this.putMsg = putMsg;
    }
}
