package Messages.NodeInsertionMessages;

import Models.SimpleNode;

import java.io.Serializable;

public class NewSubNodeMsg implements Serializable {
    public SimpleNode node;

    public NewSubNodeMsg(SimpleNode node) {
        this.node = node;
    }
}
