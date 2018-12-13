package Messages.Election;

import Models.SimpleNode;

import java.io.Serializable;

public class ElectedMsg implements Serializable {
    public SimpleNode winner;

    public ElectedMsg(SimpleNode winner) {
        this.winner = winner;
    }
}
