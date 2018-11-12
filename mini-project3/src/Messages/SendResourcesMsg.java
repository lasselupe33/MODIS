package Messages;

import java.io.Serializable;
import java.util.HashMap;

public class SendResourcesMsg implements Serializable {
    public HashMap<Integer, String> resources;
    public boolean isSubNode;

    public SendResourcesMsg(HashMap<Integer, String> resources, boolean isSubNode) {
        this.resources = resources;
        this.isSubNode = isSubNode;
    }
}
