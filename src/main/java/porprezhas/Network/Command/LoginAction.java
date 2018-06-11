package porprezhas.Network.Command;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginAction implements Action {

    public final String username;
    private ObjectOutputStream out;

    public LoginAction(String username) {
        this.username = username;

    }
    public void setObjectOutputStream(ObjectOutputStream out) {
        this.out = out;
    }
    public ObjectOutputStream getObjectOutputStream() {
        return out;
    }
    @Override
    public Answer handle(ActionHandler actionHandler) {
        return actionHandler.handle(this);
    }
}
