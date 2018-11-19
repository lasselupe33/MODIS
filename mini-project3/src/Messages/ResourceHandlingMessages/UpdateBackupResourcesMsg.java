package Messages.ResourceHandlingMessages;

import java.io.Serializable;
import java.util.HashMap;

public class UpdateBackupResourcesMsg implements Serializable {
    public HashMap<Integer, String> backupResources;

    public UpdateBackupResourcesMsg(HashMap<Integer, String> backupResources) {
        this.backupResources = backupResources;
    }
}
