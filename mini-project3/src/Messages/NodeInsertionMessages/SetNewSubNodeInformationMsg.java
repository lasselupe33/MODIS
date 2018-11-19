package Messages.NodeInsertionMessages;

import Messages.NodeInsertionMessages.SetNewNodeInformationMsg;
import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SetNewSubNodeInformationMsg extends SetNewNodeInformationMsg implements Serializable {
    public ArrayList<SimpleNode> levelAbove;

    public SetNewSubNodeInformationMsg(
            ArrayList<SimpleNode> table,
            ArrayList<Integer> prevNodeLocation,
            SimpleNode levelBelow,
            ArrayList<SimpleNode> levelAbove,
            HashMap<Integer, String> resources
    ){
        super(table, prevNodeLocation, levelBelow, resources);
        this.levelAbove = levelAbove;
    }

}
