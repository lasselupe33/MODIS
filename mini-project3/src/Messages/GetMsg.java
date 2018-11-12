package Messages;

import java.io.Serializable;

public class GetMsg implements Serializable {
    public int key;

    public GetMsg(int key) {
        this.key = key;
    }
}
