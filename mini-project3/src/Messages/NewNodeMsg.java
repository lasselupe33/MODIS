package Messages;

import Models.SimpleNode;

import java.io.Serializable;

public class NewNodeMsg implements Serializable {
    public SimpleNode node;
    public int currLevel;

    public NewNodeMsg(SimpleNode node, int currLevel) {
        this.node = node;
        this.currLevel = currLevel;
    }
}
