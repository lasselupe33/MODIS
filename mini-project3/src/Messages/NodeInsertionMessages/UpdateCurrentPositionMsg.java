package Messages.NodeInsertionMessages;

import java.io.Serializable;

public class UpdateCurrentPositionMsg implements Serializable {
    public int newPos;

    public UpdateCurrentPositionMsg(int pos) {
        newPos = pos;
    }
}
