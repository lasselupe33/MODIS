package Messages.NodeHandlingMessages;

import Models.SimpleNode;

import java.io.Serializable;

public class NodeDiedMsg implements Serializable {
    public SimpleNode newNodeBehind;

    public NodeDiedMsg(SimpleNode newNodeBehind) {
        this.newNodeBehind = newNodeBehind;
    }
}
