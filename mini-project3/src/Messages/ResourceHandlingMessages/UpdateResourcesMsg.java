package Messages.ResourceHandlingMessages;

import java.io.Serializable;
import java.util.HashMap;

public class UpdateResourcesMsg implements Serializable {
    public HashMap<Integer, String> resources;

    public UpdateResourcesMsg(HashMap<Integer, String> resources) {
        this.resources = resources;
    }
}
