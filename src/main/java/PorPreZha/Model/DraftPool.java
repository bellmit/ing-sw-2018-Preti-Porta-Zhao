package PorPreZha.Model;

import java.util.ArrayList;

public class DraftPool {

    private ArrayList<Dice> draftPool;

    public DraftPool(DiceBag diceBag, int numberOfPlayers) {
        this.draftPool = diceBag.GetRandomDices(numberOfPlayers);

    }

    public void setDraftPool(DiceBag diceBag, int numberOfPlayers) {
        this.draftPool = diceBag.GetRandomDices(numberOfPlayers);
    }
    public Dice chooseDice(int dice){

        Dice choosenDice;

        if(dice <= draftPool.size()){
            choosenDice=draftPool.remove(dice);

            return choosenDice;
        }
        return null;
    }

    public ArrayList<Dice> diceList(){

        ArrayList<Dice> listDice = new ArrayList<Dice>(draftPool.size());

        for (Dice dice : draftPool) {
            listDice.add(new Dice(dice));
        }

        return listDice;
    }

}