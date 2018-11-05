package Messages;

import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;

public class SetNewNodeInformationMsg implements Serializable {
    public ArrayList<SimpleNode> routingTable;
    public int level;

    public SetNewNodeInformationMsg(ArrayList<SimpleNode> table, int level) {
        this.routingTable = table;
        this.level = level;
    }
}
