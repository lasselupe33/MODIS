package Messages.NodeHandlingMessages;

import Models.SimpleNode;

import java.io.Serializable;

public class NewNodeMsg implements Serializable {
    public SimpleNode node;
    public int neighbourIndex;

    public NewNodeMsg(SimpleNode node, int neighbourIndex) {
        this.node = node;
        this.neighbourIndex = neighbourIndex;
    }
}
