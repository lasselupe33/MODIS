package Messages;

import java.io.Serializable;
import java.util.HashMap;

public class SendResourcesMsg implements Serializable {
    HashMap<Integer, String> resources;

    public SendResourcesMsg(HashMap<Integer, String> resources) {
        this.resources = resources;
    }
}
