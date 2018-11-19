package Messages.NodeInsertionMessages;

import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SetNewNodeInformationMsg implements Serializable {
    public ArrayList<SimpleNode> routingTable;
    public SimpleNode levelBelow;
    public ArrayList<Integer> prevNodeLocation;
    public HashMap<Integer, String> resources;

    public SetNewNodeInformationMsg(
            ArrayList<SimpleNode> table,
            ArrayList<Integer> prevNodeLocation,
            SimpleNode levelBelow,
            HashMap<Integer, String> resources
    ) {
        this.routingTable = table;
        this.prevNodeLocation = prevNodeLocation;
        this.levelBelow = levelBelow;
        this.resources = resources;

    }
}
