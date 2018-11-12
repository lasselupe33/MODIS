package Messages;

import java.io.Serializable;

public class PutMsg implements Serializable {
    private int key;
    private String value;

    public PutMsg(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
