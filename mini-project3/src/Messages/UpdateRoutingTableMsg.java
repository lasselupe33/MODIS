package Messages;

import Models.SimpleNode;

import java.io.Serializable;

public class UpdateRoutingTableMsg implements Serializable {
    public int index;
    public SimpleNode value;

    public UpdateRoutingTableMsg(int index, SimpleNode node) {
        this.index = index;
        this.value = node;
    }
}
