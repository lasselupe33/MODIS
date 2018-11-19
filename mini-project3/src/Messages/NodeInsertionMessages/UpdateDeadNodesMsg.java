package Messages.NodeInsertionMessages;

import java.io.Serializable;
import java.util.ArrayList;

public class UpdateDeadNodesMsg implements Serializable {
    public ArrayList<Integer> location;

    public UpdateDeadNodesMsg(ArrayList<Integer> location) {
        this.location = location;
    }
}
