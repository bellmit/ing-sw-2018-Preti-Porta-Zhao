package porprezhas.model.cards;

import org.junit.Before;
import org.junit.Test;
import porprezhas.model.dices.*;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ToolCard10StrategyTest {
    ToolCard toolCard10;
    ToolCardParam param;


    Random random;  // used to choose an in range number arbitrarily


    @Before
    public void setUp() {
        random = new Random();
        RoundTrack roundTrack = new RoundTrack();
        DraftPool draftPool = new DraftPool();
        DiceBag diceBag = new DiceBag();
        Board board = new Board(Pattern.TypePattern.VOID);
        List<Integer> params = null;

        toolCard10 = new ToolCard(Card.Effect.TC10);
        param = new ToolCardParam(roundTrack, draftPool, diceBag, board, params);
    }

    @Test
    public void nullTest() {
        assertNotNull( toolCard10.getStrategy() );
        assertFalse( toolCard10.getStrategy().use(null) );
        assertFalse( toolCard10.getStrategy().use(param) );


        RoundTrack roundTrack = new RoundTrack();
        DraftPool draftPool = new DraftPool();
        DiceBag diceBag = new DiceBag();
        Board board = new Board(Pattern.TypePattern.VOID);
        List<Integer> params = null;

        toolCard10 = new ToolCard(Card.Effect.TC10);
        param = new ToolCardParam(roundTrack, draftPool, diceBag, board, params);

        ;
    }
}