package Messages;

import java.io.Serializable;

public class PutMsg implements Serializable {
    public int key;
    public String value;

    public PutMsg(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
