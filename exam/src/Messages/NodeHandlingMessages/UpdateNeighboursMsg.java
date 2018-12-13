package Messages.NodeHandlingMessages;

import Models.SimpleNode;

import java.io.Serializable;

public class UpdateNeighboursMsg implements Serializable {
    public SimpleNode[] newNeighbours;
    public int timesRun;
    public int requiredRuns;

    public UpdateNeighboursMsg(SimpleNode[] newNeighbours, int timesRun, int requiredRuns) {
        this.newNeighbours = newNeighbours;
        this.timesRun = timesRun;
        this.requiredRuns = requiredRuns;
    }
}
