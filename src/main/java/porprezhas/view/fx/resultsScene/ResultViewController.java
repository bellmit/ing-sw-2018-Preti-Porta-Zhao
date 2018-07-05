package porprezhas.view.fx.resultsScene;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import porprezhas.model.Player;
import porprezhas.model.dices.Pattern;
import porprezhas.view.fx.*;
import porprezhas.view.fx.gameScene.GuiSettings;
import porprezhas.model.Game.*;


import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import static porprezhas.view.fx.gameScene.GuiSettings.*;

public class ResultViewController implements Initializable, SceneController, MovebleWindowInterface {

    @FXML Pane winnerPane;
    @FXML Label winnerNameLabel;
    @FXML Label winnerPointsLabel;

    @FXML Pane noWinner1Pane;
    @FXML Label noWinner1NameLabel;
    @FXML Label noWinner1PointsLabel;

    @FXML Pane noWinner2Pane;
    @FXML Label noWinner2NameLabel;
    @FXML Label noWinner2PointsLabel;

    @FXML Pane noWinner3Pane;
    @FXML Label noWinner3NameLabel;
    @FXML Label noWinner3PointsLabel;


    @FXML
    private Button resultsViewCloseButton;
    @FXML
    private AnchorPane resultsView;
    private Pane rootLayout;

    // Parent Controller
    StageManager stageManager;
    String stageName;

    private MovebleWindowInterface movable;



    // Stage management
    @Override
    public void setStageManager(StageManager stageManager, String stageName) {
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

            // Close this Stage
            stageManager.setStage(GuiSettings.stageLoginID, this.stageName);
        });

        timeline.play();

    }

    @Override
    public void setCurrentStageTransition() {
        // Create a Timeline to animate the transition between stages
        Timeline timeline = new Timeline();

        // Add the transition animation
        // Using Opacity Fading
        KeyFrame key = new KeyFrame(Duration.millis(STAGE_FADE_IN),
                new KeyValue(stageManager.getStage(stageName).
                        getScene().getRoot().opacityProperty(), 1));
        timeline.getKeyFrames().add(key);

        // Change Stage
        timeline.setOnFinished((actionEvent) -> {
            ;
        });

        stageManager.getStage(stageName).setOnShowing(event -> {
            // add Ending music
            BackgroundMusicPlayer.playRandomMusic(pathToResultMusic);

            timeline.play();
        });
    }




    @Override
    public void setupWindowMoveListener(Pane rootLayout, Stage stage) {
        movable.setupWindowMoveListener(rootLayout, stage);
    }

    @Override
    public void addWindowMoveListener() {
        movable.addWindowMoveListener();
    }





    // this will be called by JavaFX
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(bDebug)
            System.out.println("Initializing ResultView");

        // assign the rootLayout the top most parent pane, now that it is initialized
        rootLayout = resultsView;

        setGameCursor();

        Platform.runLater(() -> {
            // Add Move Window Listener
            movable = new MovebleWindow();
            setupWindowMoveListener(rootLayout, stageManager.getStage(this.stageName));
            addWindowMoveListener();

            // Add Window Appear Animation
            setCurrentStageTransition();
        });

        // this window is borderless and
        // we do not want it be resized
//        stageManager.getStage(stageName).setResizable(false);

    }



    private void setGameCursor() {
        rootLayout.setCursor(new ImageCursor(
                new Image(pathToCursor + "cursor_hand.png", 64.0, 64.0, true, true)));
    }


    public  void resultSetup(Player winner, HashMap ranking,List<Player> playerList) {
        int i = 0;

        String[] usernames = new String[3];
        String[] points = new String[3];
        int j=0;


        winnerNameLabel.setText(winner.getName());
        winnerPointsLabel.setText((String) ranking.get(winner).toString());

        for (Player player: playerList ) {
            if(!ranking.get(player).equals(winner)){
                usernames[i] = player.getName();
                points[i] = ranking.get(player).toString();
                j++;
             }
        }

        noWinner1NameLabel.setText(usernames[0]);
        noWinner1PointsLabel.setText(points[0]);

       if(j>0){
           noWinner2NameLabel.setText(usernames[1]);
           noWinner2PointsLabel.setText(points[1]);
       }

       if(j>1){
        noWinner3NameLabel.setText(usernames[2]);
        noWinner3PointsLabel.setText(points[2]);
       }


    }


    // FXML methods

    @FXML
    public void handleCloseButtonAction(ActionEvent event) {
        goToNextStage();

/*        Stage stage = (Stage)resultsViewCloseButton.getScene().getWindow();
        Platform.exit();
        System.exit(0);
*/
    }




}
