package Messages.NodeInsertionMessages;

import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;

public class SetNewNodeInformationMsg implements Serializable {
    public ArrayList<SimpleNode> routingTable;
    public ArrayList<Integer> prevNodeLocation;

    public SetNewNodeInformationMsg(ArrayList<SimpleNode> table, ArrayList<Integer> prevNodeLocation) {
        this.routingTable = table;
        this.prevNodeLocation = prevNodeLocation;
    }
}
