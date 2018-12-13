package Messages.Election;

import Models.SimpleNode;

import java.io.Serializable;

public class ElectionMsg implements Serializable {
    public SimpleNode candidate;
    public SimpleNode deadSuperNode;

    public ElectionMsg(SimpleNode candidate, SimpleNode deadSuperNode) {
        this.candidate = candidate;
        this.deadSuperNode = deadSuperNode;
    }
}
