package Messages.NodeHandlingMessages;

import Models.SimpleNode;

import java.io.Serializable;

public class UpdateNodeBehindMsg implements Serializable {
    public SimpleNode nodeBehind;

    public UpdateNodeBehindMsg(SimpleNode nodeBehind) {
        this.nodeBehind = nodeBehind;
    }
}
