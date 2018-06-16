package porprezhas.model.cards;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import porprezhas.Useful;
import porprezhas.exceptions.GameAbnormalException;
import porprezhas.exceptions.diceMove.DiceNotFoundException;
import porprezhas.exceptions.diceMove.IndexOutOfBoardBoundsException;
import porprezhas.exceptions.toolCard.DiceNotFoundInBoardException;
import porprezhas.exceptions.toolCard.IncorrectParamQuantityException;
import porprezhas.exceptions.toolCard.MoveToSelfException;
import porprezhas.exceptions.toolCard.ToolCardParameterException;
import porprezhas.model.GameConstants;
import porprezhas.model.dices.*;

import java.util.*;

import static org.junit.Assert.*;
import static porprezhas.model.dices.Board.COLUMN;
import static porprezhas.model.dices.Board.ROW;
import static porprezhas.model.dices.CellPosition.*;
import static porprezhas.model.dices.CellPosition.getRandomBorderValue;
import static porprezhas.model.dices.Dice.*;

@RunWith(Parameterized.class)
public class ToolCard2StrategyTest {
    @Parameterized.Parameter(0)
    public String parameterizedTestName;
    @Parameterized.Parameter(1)
    public Board parameterizedBoard;
    @Parameterized.Parameter(2)
    public Dice parameterizedDice;
    @Parameterized.Parameter(3)
    public List<Integer> parameterizedIntegerParams;
    @Parameterized.Parameter(4)
    public boolean parameterizedSuccess;
    @Parameterized.Parameter(5)
    public boolean parameterizedReturnValue;
    @Parameterized.Parameter(6)
    public Class<? extends RuntimeException> expectedException;


    private ToolCard toolCard2;
    private ToolCardParam param;
    private List<Integer> integerParams;

    private Dice genericDice;
    // used dice container
    private Board emptyBoard;

    // not used container
    private DiceBag diceBag;
    private DraftPool draftPool;
    private RoundTrack roundTrack;


    // CREATE SOME NOTE POSITIONS

    private static final Random random = new Random();    // used to choose an in range(correct) number arbitrarily
    // NB: this random should NOT influence the Result of tests

    // inside border
    private static final CellPosition innerPosition = getRandomInnerValue(random.nextLong());

    // corners
    private static final CellPosition topLeftPosition = new CellPosition(MIN_ROW, MIN_COLUMN);
    private static final CellPosition bottomRightPosition = new CellPosition(MAX_ROW, MAX_COLUMN);
    private static final CellPosition topRightPosition = new CellPosition(MIN_ROW, MAX_COLUMN);
    private static final CellPosition bottomLeftPosition = new CellPosition(MAX_ROW, MIN_COLUMN);

    // inner boarders
    private static final CellPosition topBorderPosition = getRandomBorderValue(random.nextLong(), Bound.TOP, true);
    private static final CellPosition botBorderPosition = getRandomBorderValue(random.nextLong(), Bound.BOTTOM, true);
    private static final CellPosition leftBorderPosition = getRandomBorderValue(random.nextLong(), Bound.LEFT, true);
    private static final CellPosition rightBorderPosition = getRandomBorderValue(random.nextLong(), Bound.RIGHT, true);

    // second inner boarder
    private static final CellPosition different_topBorderPosition = new CellPosition(
            Useful.getRandomNumberExcept(ROW, topBorderPosition.getRow(), MIN_ROW, MAX_ROW),
            CellPosition.MIN_COLUMN);

    private static final CellPosition different_botBorderPosition = new CellPosition(
            Useful.getRandomNumberExcept(ROW, botBorderPosition.getRow(), MIN_ROW, MAX_ROW),
            CellPosition.MAX_COLUMN);

    private static final CellPosition different_leftBorderPosition = new CellPosition(
            CellPosition.MIN_ROW,
            Useful.getRandomNumberExcept(COLUMN, leftBorderPosition.getCol(), MIN_COLUMN, MAX_COLUMN));

    private static final CellPosition different_rightBorderPosition = new CellPosition(
            CellPosition.MAX_ROW,
            Useful.getRandomNumberExcept(COLUMN, rightBorderPosition.getCol(), MIN_COLUMN, MAX_COLUMN));

    // out of bounds
    private static final CellPosition over_topBoundPosition = new CellPosition(
            MIN_ROW - 1,
            Useful.getRandomNumber(COLUMN));

    private static final CellPosition over_botBoundPosition = new CellPosition(
            MAX_ROW + 1,
            Useful.getRandomNumber(COLUMN));

    private static final CellPosition over_leftBoundPosition = new CellPosition(
            Useful.getRandomNumber(ROW),
            MIN_COLUMN - 1);

    private static final CellPosition over_rightBoundPosition = new CellPosition(
            Useful.getRandomNumber(ROW),
            MAX_COLUMN + 1);

    private static final CellPosition outBoundPosition_topLeft = new CellPosition(MIN_ROW - 1, MIN_COLUMN - 1);
    private static final CellPosition outBoundPosition_bottomRight = new CellPosition(MAX_ROW + 1, MAX_COLUMN + 1);
    private static final CellPosition outBoundPosition_topRight = new CellPosition(MIN_ROW - 1, MAX_COLUMN + 1);
    private static final CellPosition outBoundPosition_bottomLeft = new CellPosition(MAX_ROW + 1, MIN_COLUMN - 1);


/*    public ToolCard2StrategyTest(Board parameterizedBoard, Dice parameterizedDice, List<Integer> parameterizedIntegerParams, boolean parameterizedSuccess, boolean parameterizedReturnValue) {
        this.parameterizedBoard = parameterizedBoard;
        this.parameterizedDice = parameterizedDice;
        this.parameterizedIntegerParams = parameterizedIntegerParams;
        this.parameterizedSuccess = parameterizedSuccess;
        this.parameterizedReturnValue = parameterizedReturnValue;
    }
*/

    @Parameterized.Parameters
    public static Collection<Object[]> data() {

        // NB: Arrays.asList() returns AbstractList that hasn't implemented a correct add method
        return Arrays.asList(new Object[][]{
                {"All Null Test",
                        null,
                        null,
                        null,
                        false,
                        false,
                        IncorrectParamQuantityException.class},
                {"Null Param Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        null,
                        false,
                        false,
                        IncorrectParamQuantityException.class},
                {"Null Board Test",
                        null,
                        null,
                        Arrays.asList(
                                topLeftPosition.getRow(),
                                topLeftPosition.getCol(),
                                topLeftPosition.getRow(),
                                topLeftPosition.getCol()),
                        false,
                        false,
                        GameAbnormalException.class},

                {"All Empty Test",
                        new Board(Pattern.TypePattern.VOID),
                        null,
                        new ArrayList(),
                        false,
                        false,
                        IncorrectParamQuantityException.class},
                {"Empty integer Parameter Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        new ArrayList(),
                        false,
                        false,
                        IncorrectParamQuantityException.class},
                {"Empty Board Test",
                        new Board(Pattern.TypePattern.VOID),
                        null,
                        Arrays.asList(
                                topLeftPosition.getRow(),
                                topLeftPosition.getCol(),
                                botBorderPosition.getRow(),
                                botBorderPosition.getCol()),
                        false,
                        false,
                        DiceNotFoundInBoardException.class},

                // on self - 6
                {"Move on self Test1",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                topLeftPosition.getRow(),
                                topLeftPosition.getCol(),
                                topLeftPosition.getRow(),
                                topLeftPosition.getCol()),
                        false,
                        false,
                        MoveToSelfException.class},

                {"Move on self Test2",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                bottomRightPosition.getRow(),
                                bottomRightPosition.getCol(),
                                bottomRightPosition.getRow(),
                                bottomRightPosition.getCol()),
                        false,
                        false,
                        MoveToSelfException.class},

/*              // Impossible initial situation!!! (to place inside inner board position)
                {"Move on self Test3",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                innerPosition.getRow(),
                                innerPosition.getCol(),
                                innerPosition.getRow(),
                                innerPosition.getCol()),
                        false,
                        false,
                        MoveToSelfException.class},
*/
                // not on border, not respect adjacent constraint
                // out of bound - 8
                {"Out of bound Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                over_topBoundPosition.getRow(),
                                over_topBoundPosition.getCol(),
                                topBorderPosition.getRow(),
                                topBorderPosition.getCol()),
                        false,
                        false,
                        IndexOutOfBoardBoundsException.class},
                {"Out of bound Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                outBoundPosition_bottomLeft.getRow(),
                                outBoundPosition_bottomLeft.getCol(),
                                leftBorderPosition.getRow(),
                                leftBorderPosition.getCol()),
                        false,
                        false,
                        IndexOutOfBoardBoundsException.class},

                {"Out of bound Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                over_rightBoundPosition.getRow(),
                                over_rightBoundPosition.getCol(),
                                botBorderPosition.getRow(),
                                botBorderPosition.getCol()),
                        false,
                        false,
                        IndexOutOfBoardBoundsException.class},

                {"Out of bound Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                over_rightBoundPosition.getRow(),
                                over_rightBoundPosition.getCol(),
                                outBoundPosition_topRight.getRow(),
                                outBoundPosition_topRight.getCol()),
                        false,
                        false,
                        IndexOutOfBoardBoundsException.class},

/*              // from inner board to border
                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MAX_ROW / 2, MAX_COLUMN + 2, MIN_ROW, MAX_COLUMN / 2),
                        false,
                        false},

                // from inner board to border
                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MAX_ROW / 2, MAX_COLUMN, MAX_ROW, MAX_COLUMN),
                        false,
                        false},
                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MAX_ROW, MAX_COLUMN / 2, MAX_ROW, MAX_COLUMN),
                        false,
                        false},
                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MAX_ROW / 2, MAX_COLUMN / 2, MAX_ROW, MAX_COLUMN),
                        false,
                        false},
*/
                // from border to inner board - 11
                {"from border to inner board Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                bottomRightPosition.getRow(),
                                bottomRightPosition.getCol(),
                                innerPosition.getRow(),
                                innerPosition.getCol()),
                        false,
                        false,
                        ToolCardParameterException.class},

                {"from border to inner board Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                topBorderPosition.getRow(),
                                topBorderPosition.getCol(),
                                innerPosition.getRow(),
                                innerPosition.getCol()),
                        false,
                        false,
                        ToolCardParameterException.class},
                {"from border to inner board Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                bottomRightPosition.getRow(),
                                bottomRightPosition.getCol(),
                                innerPosition.getRow(),
                                innerPosition.getCol()),
                        false,
                        false,
                        ToolCardParameterException.class},

                // from board's border to an other border
                // corners exchange - 14
                {"corners exchange Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                bottomRightPosition.getRow(),
                                bottomRightPosition.getCol(),
                                topLeftPosition.getRow(),
                                topLeftPosition.getCol()),
                        true,
                        true,
                        null},

                {"corners exchange Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                topLeftPosition.getRow(),
                                topLeftPosition.getCol(),
                                bottomRightPosition.getRow(),
                                bottomRightPosition.getCol() ),
                        true,
                        true,
                        null},

                {"corners exchange Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(
                                bottomLeftPosition.getRow(),
                                bottomLeftPosition.getCol(),
                                topRightPosition.getRow(),
                                topRightPosition.getCol()),
                        true,
                        true,
                        null},

                {"corners exchange Test",
                        new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MIN_ROW, MAX_COLUMN, MAX_ROW, MIN_COLUMN),
                        true,
                        true,
                        null},
/*
                //
                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MIN_ROW, MAX_COLUMN, MAX_ROW, MAX_COLUMN / 2),
                        false,
                        false},

                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MAX_ROW, MIN_COLUMN, MAX_ROW, MAX_COLUMN),
                        false,
                        false},


                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MIN_ROW, MIN_COLUMN, MAX_ROW, MAX_COLUMN),
                        false,
                        false},

                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(MIN_ROW, MIN_COLUMN, ROW, COLUMN),
                        false,
                        false},

                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(0, 0, 1, 1),
                        false,
                        false},

                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(0, 0, 1, 1),
                        false,
                        false},

                {new Board(Pattern.TypePattern.VOID),
                        new Dice(ColorDice.RED, 6, -1),
                        Arrays.asList(0, 0, 1, 1),
                        false,
                        false},
*/        });
    }


    @Before
    public void setUp() {
        emptyBoard = new Board(Pattern.TypePattern.VOID);
        integerParams = new ArrayList<>();

        diceBag = new DiceBag();
        draftPool = new DraftPool(diceBag.GetRandomDices(random.nextInt(GameConstants.MAX_PLAYER_QUANTITY) + 1));
        roundTrack = new RoundTrack();

        toolCard2 = new ToolCard(Card.Effect.TC2);
        param = new ToolCardParam(roundTrack, draftPool, diceBag, emptyBoard, integerParams);

        ColorDice genericColor = ColorDice.values()[random.nextInt(ColorDice.values().length - 1)];
        int genericNumber = random.nextInt(MAX_DICE_NUMBER) + 1;
        genericDice = new Dice(genericColor, genericNumber, -1);
    }


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parametrizedTest() throws RuntimeException{
        System.out.print("\n" + parameterizedTestName + ": \t");
        boolean bImpossibleInitialSituation = false;


        // emulate the board situation
        if(null != parameterizedDice && null != parameterizedIntegerParams && parameterizedIntegerParams.size()>=2) {
            try {
                parameterizedBoard.insertDice(parameterizedDice, parameterizedIntegerParams.get(0), parameterizedIntegerParams.get(1));
            } catch (Exception e) {
//                System.out.println("becomes Empty Board Test");
                //setup expected exception
//                bImpossibleInitialSituation = true;
//                thrown.expect(DiceNotFoundInBoardException.class);
            }
        }
        //setup expected exception
        if (expectedException != null  &&  !bImpossibleInitialSituation) {
            thrown.expect(expectedException);
        }


        param = new ToolCardParam(null, null, null, parameterizedBoard, parameterizedIntegerParams);

        // Check Before
        // do not check return, because it depends by previous use
        // PRINT the situation of board
        if(null != parameterizedIntegerParams && parameterizedIntegerParams.size()>=4) {
            System.out.println("Move from (" + parameterizedIntegerParams.get(0) + ":" + parameterizedIntegerParams.get(1) +
                    ") \tto (" + parameterizedIntegerParams.get(2) + ":" + parameterizedIntegerParams.get(3) + ")");
        }
        if(null != parameterizedBoard) {
            System.out.println(parameterizedBoard.toString());
        }


        boolean bSuccess = toolCard2.getStrategy().use(param);          // USE

        // NB: if ToolCard.use() throws Exception, the print will be aborted
        // Check After -result-
        System.out.println(parameterizedBoard.toString());

        assertEquals(parameterizedSuccess, bSuccess);
        assertEquals(parameterizedReturnValue, ((ToolCard2) toolCard2.getStrategy()).getReturn());
    }




/*
    @Test
    public void nullTest() {
        // test toolCard is constructed
        assertNotNull(toolCard2.getStrategy());

        // test with Null parameters
        assertFalse(toolCard2.getStrategy().use(null));

        // test with Empty parameters
        assertFalse(toolCard2.getStrategy().use(param));

        // test with all Null
        param = new ToolCardParam(null, null, null, null, null);
        assertFalse(toolCard2.getStrategy().use(param));

        // test with Null Integer Params
        param = new ToolCardParam(null, null, null, emptyBoard, null);
        assertFalse(toolCard2.getStrategy().use(param));


        // CORRECT TEST

        // test with Correct Container and param, but Null not used Container
        int fromRow = botBorderPosition.getRow();
        int fromCol = botBorderPosition.getCol();
        int toRow = topBorderPosition.getRow();
        int toCol = topBorderPosition.getCol();
        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        ColorDice genericColor = ColorDice.values()[random.nextInt(ColorDice.values().length - 1)];
        int genericNumber = random.nextInt(MAX_DICE_NUMBER) + 1;
        emptyBoard.insertDice(new Dice(genericColor, genericNumber, -1), fromRow, fromCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        // PRINT the Board, because we are testing a generic situation
        System.out.print("\n\nnullTest: ");
        System.out.println("\t Move from (" + parameterizedIntegerParams.get(0) + ":" + parameterizedIntegerParams.get(1) +
                ") \tto (" + parameterizedIntegerParams.get(2) + ":" + parameterizedIntegerParams.get(3) + ")");
        System.out.println(emptyBoard.toString());

        assertTrue(toolCard2.getStrategy().use(param));           // USE

        System.out.println(emptyBoard.toString());
    }

    @Test
    public void emptyTest() {
        // initialize empty containers and params
        RoundTrack emptyRoundTrack = new RoundTrack();
        DraftPool emptyDraftPool = new DraftPool();
        DiceBag emptyDiceBag = new DiceBag();
        Board emptyBoard = new Board(Pattern.TypePattern.VOID);
        List<Integer> emptyParams = new ArrayList<>();

        // test with All Empty
        param = new ToolCardParam(emptyRoundTrack, emptyDraftPool, emptyDiceBag, emptyBoard, emptyParams);
        assertFalse(toolCard2.getStrategy().use(param));

        // test with Null Dice Container and EMPTY Integer Params
        param = new ToolCardParam(null, null, null, null, emptyParams);
        assertFalse(toolCard2.getStrategy().use(param));

        // test with Correct Dice Container and EMPTY Integer Params
        param = new ToolCardParam(emptyRoundTrack, draftPool, diceBag, emptyBoard, emptyParams);
        assertFalse(toolCard2.getStrategy().use(param));

        // test with NULL Dice Container and Correct Integer Params
        param = new ToolCardParam(null, null, null, null, integerParams);
        assertFalse(toolCard2.getStrategy().use(param));

        // test with EMPTY Dice Container and Correct Integer Params
        param = new ToolCardParam(emptyRoundTrack, emptyDraftPool, emptyDiceBag, emptyBoard, integerParams);
        assertFalse(toolCard2.getStrategy().use(param));
    }

    @Test
    public void genericTest() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = getRandomBorderValue(random.nextLong());
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        // PRINT the Board, because we are testing a generic situation
        System.out.print("\n\ngenericTest: ");
        System.out.println("\t Move from (" + parameterizedIntegerParams.get(0) + ":" + parameterizedIntegerParams.get(1) +
                ") \tto (" + parameterizedIntegerParams.get(2) + ":" + parameterizedIntegerParams.get(3) + ")");
        System.out.println(emptyBoard.toString());

        if (!fromPosition.equals(toPosition))
            assertTrue(toolCard2.getStrategy().use(param));           // USE
        else
            assertFalse(toolCard2.getStrategy().use(param));

        System.out.println(emptyBoard.toString());
    }



    @Test
    public void moveOnSelfTest() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = fromPosition;
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        // use
        assertFalse(toolCard2.getStrategy().use(param));

        // check result
        assertFalse(((ToolCard2) toolCard2.getStrategy()).getReturn());
        assertEquals(genericDice, emptyBoard.getDice(fromRow, fromCol));
    }


    @Test
    public void adjacentConstraintTest_topBorder() {
        // setup
        int fromRow = topBorderPosition.getRow();
        int fromCol = topBorderPosition.getCol();
        int toRow = different_topBorderPosition.getRow();
        int toCol = different_topBorderPosition.getCol();


        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        // use
        assertFalse(toolCard2.getStrategy().use(param));

        // check result
        assertFalse(((ToolCard2) toolCard2.getStrategy()).getReturn());
        assertEquals(genericDice, emptyBoard.getDice(fromRow, fromCol));
    }

    @Test
    public void adjCentConstraintTest_botBorder() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = getRandomBorderValue(random.nextLong());
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        assertFalse(toolCard2.getStrategy().use(param));
    }

    @Test
    public void adjCentConstraintTest_leftBorder() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = getRandomBorderValue(random.nextLong());
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        assertFalse(toolCard2.getStrategy().use(param));
    }

    @Test
    public void adjCentConstraintTest_rightBorder() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = getRandomBorderValue(random.nextLong());
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        assertFalse(toolCard2.getStrategy().use(param));
    }

    @Test
    public void adjacentConstraintTest_topBound() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = getRandomBorderValue(random.nextLong());
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        assertFalse(toolCard2.getStrategy().use(param));
    }

    @Test
    public void adjCentConstraintTest_botBound() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = getRandomBorderValue(random.nextLong());
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        assertFalse(toolCard2.getStrategy().use(param));
    }

    @Test
    public void adjCentConstraintTest_leftBound() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = getRandomBorderValue(random.nextLong());
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        assertFalse(toolCard2.getStrategy().use(param));
    }

    @Test
    public void adjCentConstraintTest_rightBound() {
        // setup
        CellPosition fromPosition = getRandomBorderValue(random.nextLong());
        CellPosition toPosition = getRandomBorderValue(random.nextLong());
        int fromRow = fromPosition.getRow();
        int fromCol = fromPosition.getCol();
        int toRow = toPosition.getRow();
        int toCol = toPosition.getCol();

        emptyBoard.insertDice(genericDice, fromRow, fromCol);

        integerParams.add(fromRow);
        integerParams.add(fromCol);
        integerParams.add(toRow);
        integerParams.add(toCol);

        param = new ToolCardParam(null, null, null, emptyBoard, integerParams);   // correct Container

        assertFalse(toolCard2.getStrategy().use(param));
    }
    */
}
