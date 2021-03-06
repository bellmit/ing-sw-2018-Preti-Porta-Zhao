package porprezhas.model;

import porprezhas.model.dices.Board;
import porprezhas.model.dices.Pattern;
import porprezhas.model.dices.Dice;
import porprezhas.model.dices.DiceBag;
import porprezhas.model.dices.DraftPool;

public class TestDrive {

    public static void main(String[] args) {

        DiceBag dicebag = new DiceBag();

        DraftPool draftPool = new DraftPool(dicebag, 4 );

        Pattern pattern = new Pattern(Pattern.TypePattern.AURORA_SAGRADIS);

        System.out.println("Pattern");

        for (int i = 0; i <4 ; i++) {
            for (int j = 0; j<5 ; j++) {
                System.out.print(pattern.getBox(i,j).getNumber());
                System.out.print(pattern.getBox(i,j).getColor() + " ");
            }
            System.out.println();
        }
        System.out.println();

        System.out.println("Draft Pool");
        for (Dice cas:
             draftPool.diceList()) {
            System.out.print(cas.getDiceNumber());
            System.out.print(cas.getDiceColor());
        };

        Dice dice0=draftPool.chooseDice(0);
        Dice dice1=draftPool.chooseDice(0);
        Dice dice2=draftPool.chooseDice(0);

        System.out.println();


        Board board = new Board(pattern.getTypePattern());
        //System.out.println(board.insertDice(dice, 0,1));

        //System.out.println(board.insertDice(dice1, 0,1));

        board.insertDice(dice0, 0, 0, Board.Restriction.ALL);

        board.insertDice(dice1, 0,1, Board.Restriction.ALL);
        board.insertDice(dice2, 1, 1, Board.Restriction.ALL);
        System.out.println("Board");
        for (int i = 0; i <4 ; i++) {
            for (int j = 0; j<5 ; j++) {
                System.out.print(board.getDice(i,j).getDiceNumber());
                System.out.print(board.getDice(i,j).getDiceColor() + " ");
            }
            System.out.println();
        }

        Boolean as=false;
        int index= Dice.ColorDice.RED.ordinal();
        System.out.println(index);

    }


    }
