package porprezhas.Network.Command;

public interface ActionHandler {
    Answer handle(LoginAction loginAction);

    Answer handle(JoinAction joinAction);

    Answer handle(InsertDiceAction insertDiceAction);

    Answer handle(ChoosePatternAction choosePatternAction);

    Answer handle(UseToolCardAction useToolCardAction);

    Answer handle(PassAction passAction);
}
