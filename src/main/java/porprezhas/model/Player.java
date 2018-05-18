package porprezhas.model;

import porprezhas.model.dices.Board;
import porprezhas.model.dices.Dice;
import porprezhas.model.cards.*;
import porprezhas.model.dices.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {

    // player identity attribute
    private Long playerID;
    private String name;
    private int position;

    // Game play attribute
    private List<Pattern.TypePattern> patternsToChoose;
    private Board board;
    private List<Card> privateObjectiveCardList;
//    private ScoreMark scoreMark;    // thinking to move this to
    private int nFavorToken;

    // Game control attribute
    private boolean bPass;
    private boolean bUsedToolCard;
    private int pickableDice;


    public Player(String name) {
        playerID = new Random().nextLong();     // TODO: this should be player's unique (String)username or (Long)ID
        this.name = name;
        nFavorToken = 0;
        privateObjectiveCardList = new ArrayList<>();
        patternsToChoose = new ArrayList<>();

        bUsedToolCard = false;
        pickableDice = 1;
    }

    public Long getPlayerID() {
        return playerID;
    }

    public void setPlayerID(Long playerID) {
        this.playerID = playerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Board getBoard() {
        return board;
    }

    public int getFavorToken() {
        return nFavorToken;
    }

    private void setFavorToken(int favorToken) {
        this.nFavorToken = favorToken;
    }

// converts from pattern difficulty to nFavorToken quantity
    public void setFavorTokenByDifficulty(int difficulty) {
        int nFavorToken = difficulty;
        setFavorToken(nFavorToken);
    }

    public List<Pattern.TypePattern> getPatternsToChoose() {
        return new ArrayList<>(patternsToChoose);
    }

    public void setPatternsToChoose(List<Pattern.TypePattern> patternsToChoose) {
        this.patternsToChoose = patternsToChoose;
    }

    public List<Card> getPrivateObjectiveCardList() {
        return new ArrayList<Card>(privateObjectiveCardList);
    }   // return a new list to protect the list being not modified by external class

    public void setPrivateObjectCardList(List<Card> privateObjectiveCardList) {
        this.privateObjectiveCardList = privateObjectiveCardList;
    }

    public boolean hasPassed() {    // TODO: can someone help me to make a better name for this method?
        return bPass;
    }

    public void passes(boolean bPass) {
        this.bPass = bPass;
    }

    public boolean hasUsedToolCard() {
        return bUsedToolCard;
    }

    public void setUsedToolCard(boolean bUsedToolCard) {
        this.bUsedToolCard = bUsedToolCard;
    }

    public int getPickableDice() {
        return pickableDice;
    }

    public void setPickableDice(int pickableDice) {
        this.pickableDice = pickableDice;
    }

    public void choosePatternCard (int indexPatternType) {
        this.board = new Board(
                getPatternsToChoose().get(indexPatternType));
    }

    public void placeDice(Dice dice, int x, int y) {
        board.insertDice(dice, x, y);
    }

    /* 3 optional actions:
     *   1. choose a Dice from Draft Pool
     *   2. use a Tool Card
     *   3. pass/finish
     */
    public void play() {
        bPass = false;
        // set flag to unlock view actions
    }

}
