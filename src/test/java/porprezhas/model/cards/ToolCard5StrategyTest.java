package porprezhas.model.cards;

import org.junit.Before;
import org.junit.Test;
import porprezhas.model.dices.*;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ToolCard5StrategyTest {
    ToolCard toolCard5;
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

        toolCard5 = new ToolCard(Card.Effect.TC6);
        param = new ToolCardParam(roundTrack, draftPool, diceBag, board, params);
    }

    @Test
    public void nullTest() {
        assertNotNull( toolCard5.getStrategy() );
        assertFalse( toolCard5.getStrategy().use(null) );
        assertFalse( toolCard5.getStrategy().use(param) );


        RoundTrack roundTrack = new RoundTrack();
        DraftPool draftPool = new DraftPool();
        DiceBag diceBag = new DiceBag();
        Board board = new Board(Pattern.TypePattern.VOID);
        List<Integer> params = null;

        toolCard5 = new ToolCard(Card.Effect.TC6);
        param = new ToolCardParam(roundTrack, draftPool, diceBag, board, params);

        ;
    }


}
