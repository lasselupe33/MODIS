package Messages;

import Models.SimpleNode;

import java.io.Serializable;


public class RequestResourcesMsg implements Serializable {
    SimpleNode requestingNode;

    public RequestResourcesMsg(SimpleNode requestingNode){
        this.requestingNode = requestingNode;
    }
}
