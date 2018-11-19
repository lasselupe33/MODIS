package Messages.NodeInsertionMessages;

import Messages.NodeInsertionMessages.SetNewNodeInformationMsg;
import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;

public class SetNewSubNodeInformationMsg extends SetNewNodeInformationMsg implements Serializable {
    public ArrayList<SimpleNode> levelAbove;

    public SetNewSubNodeInformationMsg(ArrayList<SimpleNode> table, ArrayList<Integer> prevNodeLocation, ArrayList<SimpleNode> levelAbove){
        super(table, prevNodeLocation);
        this.levelAbove = levelAbove;
    }

}
