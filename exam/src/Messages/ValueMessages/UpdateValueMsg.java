package Messages.ValueMessages;

import java.io.Serializable;

public class UpdateValueMsg implements Serializable {
    public int newValue;

    public UpdateValueMsg(int value) {
        this.newValue = value;
    }
}
