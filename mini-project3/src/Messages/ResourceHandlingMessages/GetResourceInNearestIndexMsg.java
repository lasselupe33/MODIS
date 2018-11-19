package Messages.ResourceHandlingMessages;

import Messages.TraverseMessages.TraverseGetMsg;

import java.io.Serializable;

public class GetResourceInNearestIndexMsg implements Serializable {
    public TraverseGetMsg getMsg;

    public GetResourceInNearestIndexMsg(TraverseGetMsg getMsg) {
        this.getMsg = getMsg;
    }
}
