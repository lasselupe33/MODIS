package Messages.NodeInsertionMessages;

import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;

public class SetNewNodeInformationMsg implements Serializable {
    public ArrayList<SimpleNode> routingTable;
    public SimpleNode levelBelow;
    public ArrayList<Integer> prevNodeLocation;

    public SetNewNodeInformationMsg(ArrayList<SimpleNode> table, ArrayList<Integer> prevNodeLocation, SimpleNode levelBelow) {
        this.routingTable = table;
        this.prevNodeLocation = prevNodeLocation;
        this.levelBelow = levelBelow;
    }
}
