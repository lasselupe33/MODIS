package Messages.NodeHandlingMessages;

import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class SetNewNodeInformationMsg implements Serializable {
    public SimpleNode neighbourBehind;
    public SimpleNode[] neighbours;
    public CopyOnWriteArrayList<SimpleNode> superNodes;

    public SetNewNodeInformationMsg(SimpleNode neighbourBehind, SimpleNode[] neighbours, CopyOnWriteArrayList<SimpleNode> superNodes) {
        this.neighbourBehind = neighbourBehind;
        this.neighbours = neighbours;
        this.superNodes = superNodes;
    }
}
