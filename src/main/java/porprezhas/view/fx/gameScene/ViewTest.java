package porprezhas.view.fx.gameScene;

import java.io.IOException;
import java.util.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import porprezhas.model.Game;
import porprezhas.model.Player;
import porprezhas.model.dices.Dice;
import porprezhas.model.dices.Pattern;
import porprezhas.view.fx.gameScene.component.BackgroundMusicPlayer;
import porprezhas.view.fx.gameScene.component.ConfirmBox;
import porprezhas.view.fx.gameScene.controller.GameViewController;

import static porprezhas.view.fx.gameScene.GuiSettings.*;


public class ViewTest extends Application {
    private Stage primaryStage;
    private AnchorPane rootLayout;


    private List<Player> players;
    private List<GameViewController.PlayerInfo> playersInfo;

    private GameViewController gameViewController;




    int mainPlayerPosition;
    public ViewTest() {
        Random random = new Random();
        // add all players
        players = new ArrayList<>();
        players.add( new Player("P1"));
        players.get(players.size()-1).setPosition(players.size()-1);
        players.get(players.size()-1).setIcon(random.nextInt(ICON_QUANTITY) +1);
        players.add( new Player("P2"));
        players.get(players.size()-1).setPosition(players.size()-1);
        players.get(players.size()-1).setIcon(random.nextInt(ICON_QUANTITY) +1);
        players.add( new Player("me"));
        players.get(players.size()-1).setPosition(players.size()-1);
        players.get(players.size()-1).setIcon(random.nextInt(ICON_QUANTITY) +1);
        mainPlayerPosition = players.size() -1;
        players.add( new Player("P4"));
        players.get(players.size()-1).setPosition(players.size()-1);
        players.get(players.size()-1).setIcon(random.nextInt(ICON_QUANTITY) +1);
/*
        players.add( new Player("P1", 0, random.nextInt(ICON_QUANTITY) +1));
        players.add( new Player("P2", 1, random.nextInt(ICON_QUANTITY) +1));
        players.add( new Player("me", 2, random.nextInt(ICON_QUANTITY) +1));
        mainPlayerPosition = players.size() -1;
        players.add( new Player("P4", 3, random.nextInt(ICON_QUANTITY) +1));
*/

        this.playersInfo = new ArrayList<>();
        int i = 0;
        for (Player player : players) {
            playersInfo.add(new GameViewController.PlayerInfo(
                    i++,
                    player.getName(),
                    player.getIconId(),
                    Pattern.TypePattern.values()[1]));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("tester");

        // initialize the layout from fxml and
        // setup the game GUI in base the player info!!!
        initRootLayout();

        // application GUI logic
        initMainLogic();


        Random random = new Random();
        for (int col = 0; col < Game.GameConstants.ROUND_NUM; col++) {
            if (random.nextInt(10) < 2) {
                for (int row = 0; row < Game.GameConstants.MAX_DICE_PER_ROUND; row++) {
                    gameViewController.addDiceToRoundTrack(
                            new Dice(random.nextInt(6) + 1,
                                    Dice.ColorDice.values()[random.nextInt(Dice.ColorDice.values().length - 1)]),
                            col );
                }
            } else {
                gameViewController.addDiceToRoundTrack(
                        new Dice(random.nextInt(6) + 1,
                                Dice.ColorDice.values()[random.nextInt(Dice.ColorDice.values().length - 1)]),
                        col );
                for (int row = 0; row < Game.GameConstants.MAX_DICE_PER_ROUND; row++) {
                    if (random.nextInt(10) < 3) {
                        gameViewController.addDiceToRoundTrack(
                                new Dice(random.nextInt(6) + 1,
                                        Dice.ColorDice.values()[random.nextInt(Dice.ColorDice.values().length - 1)]),
                                col );

                    }
                }
            }
        }
        for (int i = 0; i < players.size(); i++) {
            // insert a lot of dices to test
            for (int col = 0; col < BOARD_COLUMN; col++) {
                for (int row = 0; row < BOARD_ROW; row++) {
                    if (random.nextInt(10) < 6) {
                        gameViewController.addDice(
                                i,
                                new Dice(random.nextInt(6) + 1,
                                            Dice.ColorDice.values()[random.nextInt(Dice.ColorDice.values().length - 1)]),
                                col, row );
                    }
                }
            }
        }

        List<Dice> diceList = new ArrayList<>();
        for (int i = 0; i < Game.GameConstants.MAX_DICE_PER_ROUND; i++) {
            diceList.add(
                    new Dice(random.nextInt(6) + 1,
                            Dice.ColorDice.values()[random.nextInt(Dice.ColorDice.values().length - 1)])
                    );
        }
        gameViewController.setDraftPool(diceList);

        ;
//        showElementOverview();
        // output FPS
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                System.out.println("FPS " + com.sun.javafx.perf.PerformanceTracker.getSceneTracker(primaryStage.getScene()).getInstantFPS());
            }
        }, 0, (long) (60*1000.0 / FPS_PRINT_AT_MIN));

        // play background music
        BackgroundMusicPlayer.playMusic();
    }


    private void initMainLogic (){
        primaryStage.setOnCloseRequest(event -> {
            event.consume();    // consume close_request, because we are going to handle it
            quitGame();
        });
/*      this is notified before maximizing the window, I have to work with old size, so this is useless....
        primaryStage.maximizedProperty().addListener((ov, t, t1) -> {
            gameViewController.updateSize();
//            if(bDebug)
//                System.out.println("maximized:" + t1.booleanValue());
        });
*/
    }

    private void quitGame() {
        Boolean bQuit = new ConfirmBox().display("Title", "Are you sure to quit during a Game?");
        if(bQuit) {
            primaryStage.close();
        }
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // create a FXMLLoader and open fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/GameView.fxml"));
            if(loader == null)
                System.err.println(this + ": Error with loader.setLocation(" + getClass().getResource("/GameView.fxml") + ")");

            // Create a controller instance, passing the information about players
            gameViewController = new GameViewController(playersInfo, mainPlayerPosition, "PlayerZX");
            // Set it in the FXMLLoader
            loader.setController(gameViewController);   // I haven't set the controller in fxml because i want the controller get setup at construction

            // Load root layout from fxml file.
            rootLayout = loader.load();     // NOTE: If you get ONLY one ERROR in this line, it may because you haven't mark the folder 'resource' as resources root
                                            //       look for folder resource on the project root path, there is a folder resource, right click and choose in the end of list: 'Mark directory as'
            // Solitaire has smaller window size
            if(playersInfo.size() == 1) {
                primaryStage.setWidth(SOLITAIRE_WIDTH);
            }

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}