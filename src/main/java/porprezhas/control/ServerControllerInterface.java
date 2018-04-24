package porprezhas.control;

import porprezhas.model.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerControllerInterface extends Remote {
    void join(Player newPlayer) throws RemoteException;
    GameControllerInterface createNewGame() throws RemoteException;
}