package Messages.NodeInsertionMessages;

import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;

public class NewNodeMsg implements Serializable {
    public SimpleNode node;
    public ArrayList<Integer> prevNodeLocation;

    public NewNodeMsg(SimpleNode node, ArrayList<Integer> prevNodeLocation) {
        this.node = node;
        this.prevNodeLocation = prevNodeLocation;
    }
}
