package Messages.TraverseMessages;

import java.io.Serializable;
import java.util.ArrayList;

public class DeadNodeMsg extends TraverseMsg implements Serializable {
    public ArrayList<Integer> location;

    public DeadNodeMsg(ArrayList<Integer> location) {
        super(-1);
        this.location = location;
    }
}
