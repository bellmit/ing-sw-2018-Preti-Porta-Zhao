package porprezhas.control;

import porprezhas.model.Game;
import porprezhas.model.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerControllerInterface  {
    void join(Player newPlayer) throws RemoteException;
    void leave(Player player) throws RemoteException;
    List getClientRmiAddress(String username);
    GameControllerInterface createNewGame() throws RemoteException;
    GameControllerInterface createNewGame(Player player, Game.SolitaireDifficulty difficulty) throws RemoteException;
    boolean isAlreadyInGame(Player player) throws RemoteException;
    GameControllerInterface getGameController(Player player) throws RemoteException;
    void closedConnection(String username);
    void removeGameController(GameControllerInterface gameControllerInterface);
    void timeoutReset();

    //int getGameControllerIndex(String username) throws RemoteException;
}
