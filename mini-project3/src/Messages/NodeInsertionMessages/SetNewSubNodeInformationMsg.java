package Messages.NodeInsertionMessages;

import Messages.NodeInsertionMessages.SetNewNodeInformationMsg;
import Models.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;

public class SetNewSubNodeInformationMsg extends SetNewNodeInformationMsg implements Serializable {
    public SimpleNode levelAbove;

    public SetNewSubNodeInformationMsg(ArrayList<SimpleNode> table, ArrayList<Integer> prevNodeLocation, SimpleNode levelAbove){
        super(table, prevNodeLocation);
        this.levelAbove = levelAbove;
    }

}
