package Messages.NodeInsertionMessages;

import Models.SimpleNode;
import java.io.Serializable;

public class SetBackUpNodeMsg implements Serializable{

    public SimpleNode backupNode;

    public SetBackUpNodeMsg(SimpleNode backupNode) {
        this.backupNode = backupNode;
    }
}