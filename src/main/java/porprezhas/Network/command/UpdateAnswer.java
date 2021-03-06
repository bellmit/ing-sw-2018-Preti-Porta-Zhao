package porprezhas.Network.command;

import porprezhas.model.SerializableGameInterface;

public class UpdateAnswer implements Answer {
    public final SerializableGameInterface serializableGameInterface;

    public UpdateAnswer(SerializableGameInterface serializableGameInterface) {
        this.serializableGameInterface = serializableGameInterface;
    }

    @Override
    public void handle(AnswerHandler answerHandler) {
        answerHandler.handle(this);
    }
}
