package Messages.TraverseMessages;

import Models.SimpleNode;

import java.io.Serializable;

public class TraverseGetMsg extends TransferMsg implements Serializable {
    public SimpleNode returnNode;

    public TraverseGetMsg(int key, SimpleNode returnNode) {
        super(key);
        this.returnNode = returnNode;
    }
}
