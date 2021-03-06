package porprezhas.model.cards;

import porprezhas.model.Game;

import java.util.ArrayList;
import java.util.Random;

import static porprezhas.model.cards.Card.Effect.*;


public class ToolCardFactory implements CardFactory {
    private final int TOOL_CARD_PER_GAME = 12;

    ArrayList<Card> cards;
    ArrayList<Integer> numberList;
    private final int numberOfCard;

    /**
     * create the list of Tool Cards
     * @param difficulty used to define the case of singleplayer
     */

    public ToolCardFactory(Game.SolitaireDifficulty difficulty) {
        numberList = new ArrayList<Integer>();
        for(int i = 0; i<TOOL_CARD_NUMBER; i++){
            numberList.add(i);
        }

        this.numberOfCard = difficulty.toToolCardsQuantity();
    }

    /**
     * create the list of Tool Cards
     * @param numberOfPlayer used to define the difference between singleplayer and multiplayer
     */

    public ToolCardFactory(int numberOfPlayer) {
        numberList = new ArrayList<Integer>();
        for(int i = 0; i<TOOL_CARD_NUMBER; i++){
            numberList.add(i);
        }

        this.numberOfCard = TOOL_CARD_PER_GAME;
    }


    /**
     * create the list of Tool cards
     * @return the list of cards
     */

    @Override
    public ArrayList<Card> createCard() {
        int cardId;
        Random random = new Random();

        cards= new ArrayList<>();

        for(int i=0; i < numberOfCard; i++){


            cardId = random.nextInt(numberList.size());

            Card newCard = new ToolCard( Card.Effect.values() [TC1.ordinal() + numberList.get(cardId)] );
            cards.add(newCard);

            numberList.remove(cardId);
        }
        return cards;
    }
}
