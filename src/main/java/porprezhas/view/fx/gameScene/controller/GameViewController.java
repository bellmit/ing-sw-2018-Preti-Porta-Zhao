package porprezhas.view.fx.gameScene.controller;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import porprezhas.Network.ClientActionSingleton;
import porprezhas.exceptions.GameAbnormalException;
import porprezhas.model.*;
import porprezhas.model.cards.Card;
import porprezhas.model.dices.*;
import porprezhas.view.fx.BackgroundMusicPlayer;
import porprezhas.view.fx.SceneController;
import porprezhas.view.fx.StageManager;
import porprezhas.view.fx.gameScene.GuiSettings;
import porprezhas.view.fx.gameScene.controller.component.*;
import porprezhas.view.fx.gameScene.state.DiceContainer;
import porprezhas.view.fx.gameScene.state.DiceContainerType;
import porprezhas.view.fx.gameScene.state.GameViewState;
import porprezhas.view.fx.gameScene.state.PlayerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static porprezhas.view.fx.gameScene.GuiSettings.*;
import static porprezhas.view.fx.gameScene.state.DiceContainer.*;

public class GameViewController implements SceneController, GameViewUpdaterInterface {

    //  ***** JavaFX attributes *****
    // these will be initialized by the FXMLLoader when the load() method is called
          private Pane       rootLayout;
    @FXML private AnchorPane fx_gameScene;   // top parent layout
    @FXML private StackPane  fx_gamePane;   // fx:id="fx_gamePane"

    @FXML private VBox fx_enemyPanesParent;    // contain all enemies board

    @FXML private HBox fx_playerPane;     // parent of fx_playerBoard, used to resize fx_playerBoard
    @FXML private GridPane fx_playerBoard;
    @FXML private ImageView fx_bag;
    @FXML private FlowPane fx_tokens;
    @FXML private ImageView fx_timeout;

    @FXML private Button fx_buttonPass;

//    @FXML private GridPane roundTrackDiceTable;
    @FXML private HBox fx_roundTrack;
    @FXML private VBox fx_diceListReference;   // this is used because grid pane didn't cover all the space of parent grid pane

    @FXML private HBox fx_draftPoolParent;

    @FXML private TabPane fx_tabPane;
    @FXML private StackPane fx_toolCardPane;
    @FXML private StackPane fx_privateCardPane;
    @FXML private StackPane fx_publicCardPane;

    @FXML private Label fx_message;

    //  ***** VIDEO Setting attributes *****
    // TODO: this can be imported in the VIDEO settings
    private final double enemyPaneSpacingRatio = 0.3 ;  // should be in range [0, 1]

    private final double buttonSpacingRatio = 0.4;      // sum of 2 button ratio should be in range [0,1.4], otherwise the fx_playerBoard's size would decrease
    private final double buttonIncreaseRatio = 1.0;     // if the sum == 1.4 then the fx_playerBoard size would keep width/height ratio
                                                        // NB. this value of 1.4 may depends by monitor
    private final double defaultPassButtonMinSize = 60;          // this should be bigger than 60, for big resolution monitor would need a bigger value



    //  ***** Game View Controller attributes *****
    // Parent Controller
    private StageManager stageManager;
    private String stageName;

    // Sub Controllers
    private List<EnemyViewController> enemyViewControllers;
    private List<Pane> enemyPanes;      // panel of 0-3 enemies, contain board, name, icon
    //private List<VBox> roundTrackLists;

    private List<BoardView> boardList;      // board of all players
    private RoundTrackBoardView roundTrackBoard;
//    private List<DiceView>[] roundTrackDiceLists;   // make care about List<String> is not a Object. But our List<DiceView> is a Object.
    private DraftPoolView draftPoolView;

    private CardPane[] cardPanes;


    private ImageCursor cursorHand;
    private ImageCursor cursorHandDown;
    private ImageCursor cursorHandUp;


    // auxiliary controller
    private GameViewState state;

    private List<Object> diceContainers;

    private boolean bNextStageAble = false;
    private boolean canInsertDice = false;
    private boolean canUseToolCard = false;
//    private String strAvailableAction = "";


    //  ***** Player attributes *****
    private int num_player;
    private List<PlayerInfo> playersInfo;

    private int playerPosition;       // this identify the client's player
    private String userName;    // TODO: network use, change this in a token



    // *************************************
    // ********** <<< Methods >>> **********

    public GameViewController() {

        num_player = 4; // TODO: Remove this Fake -- actually server will reset this
//        updatePlayerInfo(player);
//        if(this.num_player > 1)
        enemyPanes = new ArrayList<>();     // Pane[this.num_player-1];
//        else
//            enemyPanes = new Pane[0];
        boardList = new ArrayList<>();

        enemyViewControllers = new ArrayList<>();

        roundTrackBoard = new RoundTrackBoardView(GameConstants.MAX_DICE_PER_ROUND, GameConstants.ROUND_NUM);
/*        for (int i = 0; i < num_player; i++) {
            boardList.add(new GridPane());
        }*/
//        roundTrackLists = new ArrayList<>();
/*        roundTrackDiceLists = new ArrayList[Game.GameConstants.ROUND_NUM];
        for (int i = 0; i < Game.GameConstants.ROUND_NUM; i++) {
            roundTrackDiceLists[i] = new ArrayList<>();
        }
*/

        draftPoolView = new DraftPoolView();

        cardPanes = new CardPane[CardTab.values().length];

        state = new GameViewState();
//        diceContainers = new ArrayList<>(DiceContainerType.values().length);
//        for (int i = 0; i < DiceContainerType.values().length; i++) {
//            diceContainers;
//        }

        if(bDebug)
            System.out.println("GameView Constructed");

    }

    // Test use, invoked by ViewTest.java
    // @requires playersInfo.size >= 1
    // @Param playersInfo.get(0).typePattern == player.typePattern &&
    //        forall( 1 <= i < playersInfo.size(); playersInfo.get(i).typePattern == enemies[i-1].typePattern
    public GameViewController( String ourUserIdentifier) {   // NOTE: during the construction method the fxml variables haven't be set yet
//    public GameViewController(List<Player> player, String ourUserIdentifier) {   // NOTE: during the construction method the fxml variables haven't be set yet

        if(bDebug)
            System.out.println("Constructing GameView");
        this.userName = ourUserIdentifier;  // NOTE: Currently, We use UNIQUE User Name to bind the client with the player

        num_player = 4; // TODO: Remove this Fake
//        updatePlayerInfo(player);
//        if(this.num_player > 1)
            enemyPanes = new ArrayList<>(this.num_player-1);
//        else
//            enemyPanes = new Pane[0];
        boardList = new ArrayList<>(num_player);

        enemyViewControllers = new ArrayList<>();

        roundTrackBoard = new RoundTrackBoardView(GameConstants.MAX_DICE_PER_ROUND, GameConstants.ROUND_NUM);
/*        for (int i = 0; i < num_player; i++) {
            boardList.add(new GridPane());
        }*/
//        roundTrackLists = new ArrayList<>();
/*        roundTrackDiceLists = new ArrayList[Game.GameConstants.ROUND_NUM];
        for (int i = 0; i < Game.GameConstants.ROUND_NUM; i++) {
            roundTrackDiceLists[i] = new ArrayList<>();
        }
*/

        draftPoolView = new DraftPoolView();

        cardPanes = new CardPane[CardTab.values().length];

        state = new GameViewState();
        diceContainers = new ArrayList<>();

        if(bDebug)
            System.out.println("GameView Constructed");
    }






    // getters
    public DraftPoolView getDraftPoolView() {
        return draftPoolView;
    }

    public List<BoardView> getBoardList() {
        return boardList;
    }






    // Stage management
    @Override
    public void setStageManager(StageManager stageManager, String stageName) {
        // Used to change Stages
        if(bDebug)
            System.out.println("Set " + stageManager + "\n\t\t to " + stageName + "\n\t\t in " + this);
        this.stageManager = stageManager;
        this.stageName = stageName;
    }

    @Override
    public void goToNextStage() {
        // Create a Timeline to animate the transition between stages
        Timeline timeline = new Timeline();

        KeyFrame key = new KeyFrame(Duration.millis(STAGE_FADE_OUT),
                new KeyValue(stageManager.getStage(stageName).getScene().getRoot().
                        opacityProperty(), 0));

        timeline.getKeyFrames().add(key);

        timeline.setOnFinished((ae) -> {
            // Switch the Stage
            stageManager.setStage(GuiSettings.stageResultsID, this.stageName);
        });

        timeline.play();

    }

    @Override
    public void setCurrentStageTransition() {
        // Create a Timeline to animate the transition between stages
        Timeline timeline = new Timeline();


        System.out.println();
        System.out.println();
        System.out.println("GameViewController");
        System.out.println(stageName);
        System.out.println(stageManager);


        // Add the transition animation
        // Using Opacity Fading
        KeyFrame key = new KeyFrame(Duration.millis(STAGE_FADE_IN),
                new KeyValue(stageManager.getStage(stageName).
                        getScene().getRoot().opacityProperty(), 1));
        timeline.getKeyFrames().add(key);

        //
        timeline.setOnFinished((actionEvent) -> {
            ;
        });

        stageManager.getStage(stageName).setOnShowing(event -> {
            // add gaming BackGround Music
            BackgroundMusicPlayer.playRandomMusic(pathToGameMusic);

            timeline.play();
        });
    }



    // VIEW SETUPs

    // this will be called by JavaFX
    public void initialize() {
        if(bDebug)
            System.out.println("Initializing GameView");

        // assign the rootLayout the top most parent pane, now that it is initialized
        rootLayout = fx_gameScene;

        Platform.runLater(() -> {
            // Add Window Appear Animation
            setCurrentStageTransition();
        });


        // setup our game GUI with following methods
        // Images
        setGameCursor();
        setBackground();

        // Listeners
        setResizeListener();        // add Resize Listener for GamePane(including all players panel, round track, etc)
//        setupRoundTrackListener();  // add action listener for round track's dice list dropping

/*
        for (int i = 0; i < Game.GameConstants.ROUND_NUM; i++) {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.TOP_CENTER);
            vBox.setBorder(new Border(new BorderStroke(Color.RED,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            roundTrackLists.add(vBox);
            roundTrackDiceTable.add(vBox, i, 1, 1, 8);
        }
*/
    }




    private void setupCards(List<Card> toolCards, List<Card> publicObjectiveCards, List<Card> privateObjectiveCards) {
        if(bDebug)
            System.out.println("\nSetup Cards");


        cardPanes[CardTab.TOOL_CARD.ordinal()] =    new CardPane( fx_toolCardPane,      CardTab.TOOL_CARD,      pathToToolCard);
        cardPanes[CardTab.PUBLIC_CARD.ordinal()] =  new CardPane( fx_publicCardPane,    CardTab.PUBLIC_CARD,    pathToPublicCard);
        cardPanes[CardTab.PRIVATE_CARD.ordinal()] = new CardPane( fx_privateCardPane,   CardTab.PRIVATE_CARD,   pathToPrivateCard);


        cardPanes[CardTab.PUBLIC_CARD.ordinal()].setupSubController(this);
        cardPanes[CardTab.PUBLIC_CARD.ordinal()].setupCardPane(publicObjectiveCards);
        if(bDebug)
            System.out.println("Public Objective Cards set up done");

        cardPanes[CardTab.TOOL_CARD.ordinal()].setupSubController(this);
        cardPanes[CardTab.TOOL_CARD.ordinal()].setupCardPane(toolCards);
        if(bDebug)
            System.out.println("Tool Cards set up done");

        cardPanes[CardTab.PRIVATE_CARD.ordinal()].setupSubController(this);
        cardPanes[CardTab.PRIVATE_CARD.ordinal()].setupCardPane(privateObjectiveCards);
        if(bDebug)
            System.out.println("Private Objective Cards set up done");
    }

    private void setupCardPane(Pane cardPane, String pathToCards, List<String> cardsName) {
        if(bDebug) {
            System.out.println("Printing setupCardPane() for " + pathToCards);
        }
        cardPane.setBorder(new Border(new BorderStroke( Color.rgb(200, 200, 200),
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
/*

        Border cardBorder = new Border(new BorderImage(
                new Image(pathToBorderFile),
                new BorderWidths(BORDER_SIZE), Insets.EMPTY, // new Insets(10, 10, 10, 10),
                new BorderWidths(BORDER_SIZE), true,
                BorderRepeat.STRETCH, BorderRepeat.STRETCH
        ));
*/


        List<Label> labels = new ArrayList<>();
        for (int i = 0; i < cardsName.size(); i++) {
            if(bDebug) {
                System.out.println("loading: " + pathToCards + cardsName.get(i) + ".jpg"); }
            ImageView imageView = new ImageView(new Image(pathToCards + cardsName.get(i) + ".jpg"));
            imageView.fitWidthProperty().bind(cardPane.widthProperty().multiply(CARD_FIT_RATIO));
            imageView.fitHeightProperty().bind(cardPane.heightProperty().multiply(CARD_FIT_RATIO));
            imageView.setPreserveRatio(true);

            labels.add(new Label());
            labels.get(i).setGraphic(imageView);

            if(cardsName.size() == 1) {
                labels.get(i).translateXProperty().bind( cardPane.widthProperty().subtract(labels.get(i).widthProperty()).subtract(CARD_PANE_PADDING).multiply(0.5) );
                labels.get(i).translateYProperty().bind( cardPane.heightProperty().subtract(labels.get(i).heightProperty()).subtract(CARD_PANE_PADDING).multiply(0.5) );
            } else {
                labels.get(i).translateXProperty().bind(cardPane.widthProperty().subtract(labels.get(i).widthProperty()).subtract(CARD_PANE_PADDING).multiply((double) (i) / (cardsName.size() - 1)));
                labels.get(i).translateYProperty().bind(cardPane.heightProperty().subtract(labels.get(i).heightProperty()).subtract(CARD_PANE_PADDING).multiply((double) (i) / (cardsName.size() - 1)));
            }
//            labels.get(i).setBorder(cardBorder);
            labels.get(i).setOpacity(cardOpacity);

            // add transition animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(CARD_FADE_IN), labels.get(i));
            fadeIn.setFromValue(cardOpacity);
            fadeIn.setToValue(1.0f);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(CARD_FADE_OUT), labels.get(i));
            fadeOut.setFromValue(1.0f);
            fadeOut.setToValue(cardOpacity);

            labels.get(i).setOnMouseEntered(event -> {
                Label source = ((Label)event.getSource());
                source.toFront();
                fadeIn.play();
            });

            labels.get(i).setOnMouseExited(event -> {
                Label source = ((Label)event.getSource());
                source.setOpacity(cardOpacity);
                fadeIn.stop();
                fadeOut.play();
            });
        }

        cardPane.getChildren().addAll(labels);

        cardPane.setOnSwipeLeft(event -> {
            // change tab to next one
            System.out.println("Swipe to left");
        });
    }

    private void setupRoundTrack() {
        roundTrackBoard.setupSubController(this);
        roundTrackBoard.setup(fx_roundTrack);
    }
    private void setupDraftPool() {
        draftPoolView.setupSubController(this);
        draftPoolView.setup(fx_draftPoolParent);
    }


    private void setupBoards() {
        if(bDebug) {
            System.out.println("\nSetup board"); }
        boardList.clear();  // this must be redundant but i want keep safe
//        fx_enemyPanesParent.getChildren().clear();

        for (int i = 0, offset = 0; i < playersInfo.size(); i++) {
            PlayerInfo playerInfo = playersInfo.get(i);
            // assign the boards to a list, for simplify the use
            if( i == getPlayerPosition()) {
                boardList.add(i,
                        new BoardView(i, fx_playerBoard, fx_bag, fx_tokens, fx_timeout));
                offset++;
            } else {
//                 gridPane = enemyViewControllers.get(i + offset).getBoard();
                boardList.add(i,        // this 'i' is index of cycle
                        new BoardView(i,    // this 'i' is server.game.player.position
                                enemyViewControllers.get(i - offset).getBoard(),
                                enemyViewControllers.get(i - offset).getBag(),
                                enemyViewControllers.get(i - offset).getTokens(),
                                enemyViewControllers.get(i - offset).getTimer()) );
//                fx_enemyPanesParent.getChildren().add(boardList.get(i).getBoard());
//                enemyViewControllers.get(i - offset).setPlayerInfo(playerInfo);
            }

            boardList.get(i).setupSubController(this);
            boardList.get(i).setPattern(playerInfo.typePattern);

            if(bDebug)
                System.out.println(playerInfo);
            if(null == boardList.get(i).getBoard()) {
                System.err.println("\nsetupBoard failed!!!\n");
            }
        }
    }

    private void setEnemyPanes() {
        if(bDebug) {
            System.out.println("Loading Enemy Pane"); }
        fx_enemyPanesParent.getChildren().clear();
        for (int i = 0; i < playersInfo.size() -1; i++) {
            // Load the panel from .fxml
            FXMLLoader loader = new FXMLLoader();   //NOTE: We must create more time loader to get multiple pane; Otherwise only one pane would be displayed
            loader.setLocation(getClass().getResource("/EnemyPaneView.fxml"));
            if(loader == null)
                System.err.println(this + ": Error with loader.setLocation(" + getClass().getResource("/EnemyPaneView.fxml") + ")");
            try {
                enemyPanes.add( loader.load() );  // if there is a error, it may be caused by incorrect setting in EnemyPaneView.fxml
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(bDebug) {
                System.out.println("Enemy Pane View Loaded successfully"); }

            // add the enemy panel on the game view
            fx_enemyPanesParent.getChildren().add(enemyPanes.get(i));

            // get controller
            EnemyViewController enemyViewController = loader.getController();
            enemyViewControllers.add(enemyViewController);

            // disable enemy board from dragging
            if(!bDebug) {
                fx_enemyPanesParent.setDisable(true);
            }

            // setup player info
            enemyViewController.setPlayerInfo(playersInfo.get(i >= playerPosition ? i+1 : i));
        }
    }

    private void setGameCursor() {
        cursorHand = new ImageCursor(
                new Image(pathToCursor + "cursor_hand.png", 64.0, 64.0, true, true),
                12, 12);
        cursorHandDown = new ImageCursor(
                new Image(pathToCursor + "cursor_hand_down.png") );
        cursorHandUp = new ImageCursor(
                new Image(pathToCursor + "cursor_hand_up.png") );

        rootLayout.setCursor(cursorHand);
//        fx_gamePane.setCursor(cursorHand);
    }

    private void setBackground() {
            Background background = new Background(
                    new BackgroundFill(new ImagePattern(
                            new Image(pathToBackground + "game.jpeg")),
                            CornerRadii.EMPTY, Insets.EMPTY));
            fx_gamePane.setBackground(background);
    }

    private void setResizeListener() {
        fx_gamePane.heightProperty().addListener( (observable, oldValue, newValue) -> {
            updateEnemyPaneSize(newValue.doubleValue());
//            updatePlayerPaneSize(fx_playerPane.getWidth(), fx_playerPane.getHeight());  // commented because we don't know the new value of height
        });

        fx_playerPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                updatePlayerPaneSize(fx_playerPane.getWidth(), newValue.doubleValue());
        });
        fx_playerPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                updatePlayerPaneSize(newValue.doubleValue(), fx_playerPane.getHeight());
        });

        ((Pane) fx_roundTrack.getParent()).widthProperty().addListener((observable, oldValue, newValue) -> {
            updateRoundTrackSize(newValue.doubleValue(), ((Pane) fx_roundTrack.getParent()).getHeight());
        });
        ((Pane) fx_roundTrack.getParent()).heightProperty().addListener((observable, oldValue, newValue) -> {
            updateRoundTrackSize(((Pane) fx_roundTrack.getParent()).getWidth(), newValue.doubleValue());
        });
    }

    // adjust enemy panel size and gap between panels
    private void updateEnemyPaneSize(double gamePaneHeight) {

        // these are the dimension we designed for enemy board
        final int designedEnemyNumber = 3;
        final double desiredEnemyPaneWidth = 260.0;
        final double desiredEnemyPaneHeight = 160.0;
        final double referenceEnemyPaneHeight = 500.0;   // this is the reference min height of the fx_gamePane with 3 enemies
        final double enemyPaneWidthFactor = desiredEnemyPaneWidth / desiredEnemyPaneHeight;    // this is the ratio between width and height of a single enemy pane
        final double enemyPaneHeightFactor = desiredEnemyPaneHeight / referenceEnemyPaneHeight;    // this is the ratio of enemy panel's height on entire game's height

        // My note:
        // H: 600 -> 3 x 160 = 480 + 4 x 120/4
        // H: 300 : 160 - 222
        // H: 600 : 600 - 222
        // W: 300 : 260 - 360

        final double newHeight = gamePaneHeight;    // final is used to mark this read only
        boolean bMinimum = newHeight <= referenceEnemyPaneHeight;
//            double minSpacing = (referenceEnemyPaneHeight/3 *  (3- (playersInfo.size()-1)));
        double totalSpacing =
                bMinimum ?
                        0 :
                        ( newHeight - (referenceEnemyPaneHeight) * (playersInfo.size()-1)/designedEnemyNumber ) * enemyPaneSpacingRatio;
        double paneHeight =
                bMinimum ?
                        newHeight * enemyPaneHeightFactor :
                        enemyPaneHeightFactor * (newHeight - totalSpacing);
        double paneWidth = paneHeight * enemyPaneWidthFactor;

        // if the height isn't narrow increase size and gap between enemy panels
        fx_enemyPanesParent.setSpacing(totalSpacing / (playersInfo.size()-1 +1));  // playersInfo.size() -1 == enemy_num;
                                                                        // +1 for spacing top and bottom too,
                                                                        // -1 if you just want increase the gap
        for ( Pane pane: enemyPanes) {
            if(null != pane) {
                pane.setPrefWidth(paneWidth);
                pane.setPrefHeight(paneHeight);
/*                if(enemyPanes.length == 1) {
                    fx_enemyPanesParent.setPadding(new Insets((newValue.doubleValue() - enemyPanes[0].getHeight())/10, 0, 0, 0));
                }
*/
            }
        }
    }

    // adjust Player panel size, working on button size and layout padding
    private void updatePlayerPaneSize(double playerPaneWidth, double playerPaneHeight) {
        if (bDebug)
            System.out.println("fx_playerPane: w=" + playerPaneWidth + "\th=" + playerPaneHeight);

        // dimension configured for player panel
        final double playerPaneDesiredWidth = 340.0;
        final double playerPaneDesiredRatio = 128.0 / 160.0;    // desiredEnemyPaneHeight / desiredEnemyPaneWidth
        final double playerPaneWidthFactor = 240.0 / playerPaneDesiredWidth;

        double w = playerPaneWidth * playerPaneWidthFactor;
        if (w > playerPaneHeight / playerPaneDesiredRatio) {
            double inc = (w - playerPaneHeight / playerPaneDesiredRatio);
            double spacing = inc * buttonSpacingRatio;
            double buttonSize = defaultPassButtonMinSize + inc * buttonIncreaseRatio;
            fx_buttonPass.setPrefWidth(buttonSize);
            ((Pane) fx_buttonPass.getParent()).setPadding(new Insets(
                    spacing / 2, spacing / 2, spacing / 2, spacing / 2));
        } else {
            fx_buttonPass.setPrefWidth(defaultPassButtonMinSize);
            ((Pane) fx_buttonPass.getParent()).setPadding(new Insets(0, 0, 0, 0));
        }
    }

    // this resize only the round numbers
    private void updateRoundTrackSize (double roundTrackParentWidth, double roundTrackHeight) {
        if (bDebug)
            System.out.println("update round track: w=" + roundTrackParentWidth + "\th=" + roundTrackHeight);
        final double referenceButtonSize = 24;  // estimated button size, not necessary to have an accurate measure of this
        double height = roundTrackHeight;   // fx_roundTrack number icon height
        double width = (roundTrackParentWidth - referenceButtonSize) / 10;  // fx_roundTrack number icon width, 10 numbers
        if (width >= height) {
            width = height;     // adapt to height
        } else {
            height = width;     // adapt to width, cut height
        }
        for (Node node : fx_roundTrack.getChildren()) {
            ImageView imageView = (ImageView) node;
            imageView.setFitHeight(width);  // height == width
            imageView.setFitWidth(width);
//            imageView.setPreserveRatio(true); // this is the default value
        }
    }



    // ********** <<< Send Message to Server >>> **********
    public boolean insertDice(long diceID, int row, int col) {
//        if(idBoardTo == DiceContainerType.fromPlayer(playerPosition).toInt()) {
            /*return*/ ClientActionSingleton.getClientAction().
                    insertDice(diceID, row, col);
            return true;
//        } else
//            return false;
    }

    public boolean useToolCard(int cardID, ArrayList<Integer> params) {
        return ClientActionSingleton.getClientAction().useToolCard(userName, cardID, params);
    }


    // decide what send to server
    public boolean clickDice(int idBoard, DiceView diceView) {
        return state.clickDice(idBoard, diceView);
    }

    public boolean moveDice(int idBoardFrom, DiceView diceView, int idBoardTo, int toRow, int toCol) {
        return state.moveDice(idBoardFrom, diceView, idBoardTo, toRow, toCol);
    }


    // ******************************************
    // ********** <<< FXML Methods >>> **********

    @FXML
    protected void onShowRoundTrackDices(ActionEvent event) {
        roundTrackBoard.showRoundTrackDices(-1);
    }

    @FXML
    protected void onMovingInRoundTrack(MouseEvent event) {
/*        try { return; } catch (Exception e) { e.printStackTrace(); }
        int i = (int) (event.getX() / ((HBox) event.getSource()).getWidth() *10);
        if( i > 10)
            i  = 9;
        System.out.println("Moving on " + (i+1) );
*/
    }

    @FXML
    protected void onTabelEntered(MouseEvent event) {
        if(bDebug)
            System.out.println("Tabel Entered!");
    }

    @FXML
    protected void onTabelExited(MouseEvent event) {
        if(bDebug)
            System.out.println("Tabel Exited");
    }

    @FXML
    protected void onPass(ActionEvent event) {
        System.out.println("PASS");
        // for test drafting
       /* Random random = new Random();
        List<Dice> diceList = new ArrayList<>();
        for (int i = 0; i < Game.GameConstants.MAX_DICE_PER_ROUND; i++) {
            diceList.add(
                    new Dice(random.nextInt(6) + 1,
                            Dice.ColorDice.values()[random.nextInt(Dice.ColorDice.values().length - 1)])
            );
        }
        draftPoolView.reroll(diceList);*/

       if(bNextStageAble) {
           goToNextStage();
       } else {
           ClientActionSingleton.getClientAction().pass();
       }
    }

    @FXML
    protected void onToolCardTab() {
//        System.out.println("Tool Cards");
    }

    @FXML
    protected void onPrivateCardTab() {
        //System.out.println("Private Objectives");
    }

    @FXML
    protected void onPublicCardTab() {
        //System.out.println("Public Objectives");
    }


    // ********************************************
    // ********** <<< Public Methods >>> **********


    public int getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(int playerPosition) {
        if(bDebug)
            System.out.println("\nPlayer position set to " + playerPosition);
        this.playerPosition = playerPosition;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void updateSize() {
        updateEnemyPaneSize(fx_gamePane.getHeight());
        updatePlayerPaneSize(fx_playerPane.getWidth(), fx_playerPane.getHeight());
        updateRoundTrackSize( ((Pane) fx_roundTrack.getParent()).getWidth(),
                              ((Pane) fx_roundTrack.getParent()).getHeight());
    }

    public DiceView addDice(int indexPlayer, Dice dice, int row, int col) {
        if (null == boardList)
            System.err.println("\nWarning BoardList is empty!!!\n");
        return boardList.get(indexPlayer).addDice(dice, row, col);
    }

    public DiceView addDiceToRoundTrack(Dice dice, int round0_9) {
        DiceView diceImage = roundTrackBoard.addDice(dice, 0, round0_9);
//        if(null != diceImage)
//        diceImage.setPreserveRatio(false);

//        diceImage.fitWidthProperty().bind( fx_diceListReference.widthProperty() );
//        diceImage.fitHeightProperty().bind( fx_diceListReference.heightProperty().divide(Game.GameConstants.MAX_DICE_PER_ROUND));
        return diceImage;
    }



    public void usingToolCard(Card.Effect effect) {
        state.useToolCard(effect);
    }


    public void setNextStageAble() {
        bNextStageAble = true;
        updateMessage("\t\tGame Over!!!\n\n" +
                "Press PASS to see the Raking"
        );
    }


    // MVC interface methods

    public void updatePlayerInfo(List<Player> players) {
        playersInfo = new ArrayList<>();

        this.num_player = players.size();
        for (int i = 0; i < num_player; i++) {
            Player player = players.get(i);

            playersInfo.add(new PlayerInfo(
                    i,
                    player.getName(),
                    player.getIconId(),
                    player.getBoard().getPattern().getTypePattern()));

            if (userName.equals(player.getName())) {
                setPlayerPosition(i);   //playerPosition = i;
            }
        }
    }

    public void setupView(List<Player> players, List<Card> toolCards, List<Card> publicObjectiveCards, List<Card> privateObjectiveCards) {

        updatePlayerInfo(players);

        SetupView();

        setupCards(toolCards, publicObjectiveCards, privateObjectiveCards);

        setupState();
    }


    private void SetupView() {
        if (bDebug)
            System.out.println("\n\n\nUpdate Game View!!!\n");

        // Load EnemyPanels; get their ViewController; setup the PlayerInfo;
        setEnemyPanes();    //NOTE: If the program give error on loading fxml, the problem may be here.

        // create BoardList and set pattern image to all player
        setupBoards();

        // attach RoundTrack dice table
        setupRoundTrack();

        setupDraftPool();

        if (bDebug) {
            System.out.println("\nSetup Game View -Dice Containers- Done!\n\n");
        }
    }

    public void setupState() {
        state.setupSubController(this);
        state.setup(draftPoolView, roundTrackBoard, boardList.get(playerPosition));
        state.activate();
    }

    public void updateBoard(int idBoard, Dice[][] dices) {
        boardList.get(idBoard).update(dices);
    }


    // activates roll animation
    public void updateDraftPool(List<Dice> dices) {
        draftPoolView.update(dices);
    }

    // active roll animation only for missing dices
    public void updateDraftPool(DraftPool draftPool) {
        draftPoolView.update(draftPool);
    }

    public void updateRoundTrack(int actualRound, List<Dice>[] dices) {
        roundTrackBoard.update(actualRound, dices);
    }

    public void updateFirstPlayer(Player player) {
        for (int i = 0; i < boardList.size(); i++) {
            BoardView boardView = boardList.get(i);
            if (i == player.getPosition()) {
                boardView.showBag(true);
            } else {
                boardView.showBag(false);
            }
        }
    }

    public void updateTokens(List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            boardList.get(i).setTokens(players.get(i).getFavorToken());
        }
    }

    public void updateTimer(Player player) {
        if(player.getPosition() == playerPosition) {
            // print help
            setCanUseToolCard(true);
            setCanInsertDice(true);
        } else {
            updateMessage("Wait other players pass the turn to you");
        }
        for (int i = 0; i < boardList.size(); i++) {
            BoardView boardView = boardList.get(i);
            if (i == player.getPosition()) {
                boardView.showTimer(true);
            } else {
                boardView.showTimer(false);
            }
        }
    }

    public void updateMessage(String message) {
        fx_message.setText(message);
    }

    public void updateCardEffect(List<Dice> diceList) {
        Platform.runLater(() -> {
            state.updateBox(diceList);
        });
    }

    /**
     * reset all tool card parameters and
     * close the dialog box containing the dice list
     */
    public void resetUseToolCard() {
            state.useToolCard(null);    // reset
    }


    public void setCanInsertDice(boolean can) {
        canInsertDice = can;
        updateHelp();
    }
    public void setCanUseToolCard(boolean can) {
        canUseToolCard = can;
        updateHelp();
    }

    /**
     * build a help String and update it to user
     */
    public void updateHelp() {
        StringBuilder stringBuilder = new StringBuilder("The available actions: \n");
        if(canInsertDice)
            stringBuilder.append("- Drag a Dice from Draft to your Board\n");
        if(canUseToolCard)
            stringBuilder.append("- use a Tool Card above\n");
        stringBuilder.append("- pass your turn\n");
//        strAvailableAction = stringBuilder.toString();

        updateMessage(stringBuilder.toString());
    }
}
