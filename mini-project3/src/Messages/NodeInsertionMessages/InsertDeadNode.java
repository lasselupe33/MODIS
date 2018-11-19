package Messages.NodeInsertionMessages;

import Messages.TraverseMessages.TraverseMsg;
import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;

public class InsertDeadNode extends TraverseMsg implements Serializable {
    public SimpleNode newNode;
    public ArrayList<Integer> location;
    public int level;

    public InsertDeadNode(SimpleNode newNode, ArrayList<Integer> location, int level) {
        super(0);
        this.location = location;
        this.level = level;
        this.newNode = newNode;
    }
}
