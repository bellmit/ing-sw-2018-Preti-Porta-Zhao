package porprezhas.model.dices;

import org.junit.Before;
import org.junit.Test;
import porprezhas.exceptions.diceMove.BoardCellOccupiedException;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;


public class BoardTest1 {

    Pattern pattern;
    Dice die, die1, die0;
    Board board;
    Box box;

    @Before
    public void setUp()  {
        pattern = mock(Pattern.class);
        long idCounter = 0;
        die = new Dice(Dice.ColorDice.YELLOW,1, idCounter++);
        die1 = new Dice (Dice.ColorDice.BLUE,2, idCounter++);
        die0 = new Dice (Dice.ColorDice.WHITE,0, idCounter++);
        board = new Board(Pattern.TypePattern.KALEIDOSCOPIC_DREAM);
        box = mock(Box.class);




        when(pattern.checkEdges(0,0)).thenReturn(true);
        when(pattern.getBox(0,0)).thenReturn(box);
        when(pattern.getBox(1,1)).thenReturn(box);
        when(pattern.getBox(0,1)).thenReturn(box);
        when(box.checkConstraint(die)).thenReturn(true);

    }



    @Test
    public void compatibleDiceTest() {

        assertFalse(board.compatibleDice(die1, die1));

        assertTrue(board.compatibleDice(die, die1));
    }

    @Test
    public void adjacentDiceTest() {
        board.insertDice(die, 0,0);
        assertTrue(board.adjacentDice(die,1,1));
        assertFalse(board.adjacentDice(die,0,1));
        assertTrue(board.adjacentDice(die1, 0, 1));




    }

    @Test (expected = BoardCellOccupiedException.class)
    public void insertDiceTest() {

        assertEquals(board.getDiceQuantity(), 0);
        assertTrue( board.insertDice(die, 0,0));
        assertEquals(board.getDiceQuantity(), 1);

        BoardCellOccupiedException exception = null;
        try {
            assertFalse( board.insertDice(die, 0,0));
        } catch (BoardCellOccupiedException e) {
            exception = e;
        }

        assertEquals(board.getDiceQuantity(), 1);
        assertFalse(board.isBoxOccupied(0,1));
        assertTrue(board.insertDice(die, 1,1));
        assertEquals(board.getDiceQuantity(), 2);


        throw exception;
    }



    @Test
    public void getDiceTest() {

        assertEquals(board.getDice(0,0).getDiceColor(), die0.getDiceColor());
        assertEquals(board.getDice(0,0).getDiceNumber(), die0.getDiceNumber());

        board.insertDice(die, 0,0);
        assertEquals(board.getDice(0,0).getDiceNumber(), die.getDiceNumber());
        assertEquals(board.getDice(0,0).getDiceColor(), die.getDiceColor());

    }
}