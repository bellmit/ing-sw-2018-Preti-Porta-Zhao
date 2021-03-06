package porprezhas.view.fx.loginScene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import porprezhas.Network.*;
import porprezhas.Network.rmi.client.ClientObserver;
import porprezhas.Useful;
import porprezhas.model.Game;
import porprezhas.view.fx.*;
import porprezhas.view.fx.choosePatternScene.ChoosePatternViewController;
import porprezhas.view.fx.gameScene.GuiSettings;
import porprezhas.view.fx.gameScene.controller.GameViewController;
import porprezhas.view.fx.resultsScene.ResultViewController;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ResourceBundle;

import static porprezhas.control.ServerController.USERNAME_ALREADY_TAKEN;
import static porprezhas.control.ServerController.USERNAME_AVAILABLE;
import static porprezhas.view.fx.gameScene.GuiSettings.*;
import static porprezhas.view.fx.loginScene.LoginViewController.ConnectionType.RMI;
import static porprezhas.view.fx.loginScene.LoginViewController.ConnectionType.SOCKET;

public class LoginViewController implements Initializable, SceneController, MovebleWindowInterface {

    @FXML Button loginViewButton;
    @FXML TextField userNameTextField;
    @FXML TextField fx_ipTextField;
    @FXML TextField fx_portTextField;
    @FXML Button joinViewButton;
    @FXML Button exitJoinViewButton;
    @FXML Button loginViewRMIButton;
    @FXML Button loginViewSocketButton;
    @FXML Button exitLoginViewButton;
    @FXML Button singlePlayerViewButton;
    @FXML Button returnViewButton;

    @FXML Text warningText;
    @FXML ImageView loginViewImage;
    @FXML StackPane backgroundPane;

    @FXML AnchorPane loginView;
    @FXML VBox loginScene;
    @FXML AnchorPane joinScene;
    @FXML VBox difficultyScene;

    @FXML Button difficultyViewButtonEasy;
    @FXML Button difficultyViewButtonBeginner;
    @FXML Button difficultyViewButtonNormal;
    @FXML Button difficultyViewButtonHard;
    @FXML Button difficultyViewButtonExtreme;
    @FXML Button exitDifficultyViewButton;


    private Pane rootLayout;


    private StageManager stageManager;
    private String stageName;

    private MovebleWindowInterface movable;

    private Circle clipWindow;
    private double clipRadius;

//    private DoubleProperty opacityProperty;
    private Timeline timeline;


    private final String voidString = "";


    private StringProperty user_port;
    private StringProperty user_ip;

    private final int port = 58090+1;
    private InetAddress ip;
    private int myPort= 50023;
    private ViewUpdateHandlerInterface viewUpdateHandlerInterface;

    enum ConnectionType{
        RMI,SOCKET
    }
    //Default connection method: rmi
    ConnectionType connectionType=RMI;

    private String username;

    /**
     * Setup the stageManager
     * @param stageManager the Object that creates this Scene Controller and manages all Stages
     * @param stageName    name of the Stage that contains this Scene
     */


    // Stage management
    @Override
    public void setStageManager(StageManager stageManager, String stageName) {
        if(bDebug)
            System.out.println("Set " + stageManager + "\n\t\t to " + stageName + "\n\t\t in " + this);
        this.stageManager = stageManager;
        this.stageName = stageName;
    }

    /**
     * Create the fade out transition for the current stage
     * and switch to the next one
     *
     */

    @Override
    public void goToNextStage() {
        if(bDebug)
            System.out.println("Goto next");
        // Create a Timeline to animate the transition between stages
        Timeline timeline = new Timeline();
        KeyFrame key = new KeyFrame(Duration.millis(STAGE_FADE_OUT),
                new KeyValue(stageManager.getStage(stageName).getScene().getRoot().
                        opacityProperty(), 0));
        timeline.getKeyFrames().add(key);
        timeline.setOnFinished((ae) -> {
            // Switch the Stage
            stageManager.setStage(GuiSettings.stagePatternID, this.stageName);
        });

        timeline.play();

    }

    /**
     * Create the fade in animation  for the next stage
     *
     */

    @Override
    public void setCurrentStageTransition() {
        // Create a Timeline to animate the transition between stages
        Timeline timeline = new Timeline();

        // Add the transition animation
        // Using Opacity Fading
//        KeyFrame keyFadeIn = new KeyFrame(Duration.millis(STAGE_FADE_IN * 0.2),
//                new KeyValue(rootLayout.opacityProperty(), 1));

        // and using Rotation transformation
        KeyFrame keyRotate = new KeyFrame(Duration.millis(STAGE_FADE_IN),
                new KeyValue(backgroundPane.rotateProperty(), 1* 360));

        // and add window dimension Growing effect
        // using shape Clip
        KeyFrame keyViewPortDimension = new KeyFrame(Duration.millis(STAGE_FADE_IN*0.80),
                new KeyValue(clipWindow.radiusProperty(), clipRadius));


        // add the effects in the time line
//        timeline.getKeyFrames().add(keyFadeIn);
        timeline.getKeyFrames().add(keyRotate);
        timeline.getKeyFrames().add(keyViewPortDimension);

        timeline.setDelay(Duration.millis(STAGE_FADE_IN/2));

        // reset warning text that causes Lagg
        warningText.setText("");
        warningText.setOpacity(0);

        // set Show Component transition Animation
        timeline.setOnFinished((actionEvent) -> {
            if(null == username  ||  voidString.equals(username)) {
                // show loginScene

                setScene();
               // warningText.setVisible(false);

                // Add the transition animation
                Timeline sceneFading = new Timeline();
                // Using Opacity Fading
                KeyFrame sceneFadeIn = new KeyFrame(Duration.millis(STAGE_FADE_IN * 0.3),
                        new KeyValue(loginScene.opacityProperty(), 1));

                sceneFading.getKeyFrames().add(sceneFadeIn);

                loginScene.setOpacity(0.1f);
                sceneFading.play();

            } else {
                showJoinScene();
                // remain to show joinScene
            }
        });

        // set on Window appear Animation
        stageManager.getStage(stageName).setOnShowing(event -> {
            // add gaming BackGround Music
            BackgroundMusicPlayer.playRandomMusic(pathToLoginMusic);

            // hide scenes
            loginScene.setVisible(false);
            loginScene.setVisible(false);

            // setup transition animation
            rootLayout.setOpacity(1);    // Set starting Opacity value
            clipWindow.setRadius(68);      // Set starting Dimension
//            backgroundPane.setRotate(180);
            loginScene.setOpacity(0);
            timeline.play();
        });
        if(bDebug)
            System.out.println("set " + rootLayout);
    }




    @Override
    public void setupWindowMoveListener(Pane rootLayout, Stage stage) {
        movable.setupWindowMoveListener(rootLayout, stage);
    }

    @Override
    public void addWindowMoveListener() {
        movable.addWindowMoveListener();
    }


    /**
     * Initialize the stage for patternView
     * @param location path to the resources
     * @param resources
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(bDebug)
            System.out.println("Initializing LoginView");

        // assign the rootLayout the top most parent pane, now that it is initialized
        rootLayout = loginView;

        Platform.runLater(() -> {
            // Add Move Window Listener
            movable = new MovebleWindow();
            setupWindowMoveListener(rootLayout, stageManager.getStage(this.stageName));
            addWindowMoveListener();

            // Add Window Appear Animation
            setCurrentStageTransition();
        });


        // Clip the Top most Pane -Root Layout- into a Circle
        clipWindow = clipToCircle(rootLayout);
        clipRadius = clipWindow.getRadius();

        setScene();

        setGameCursor();

        setupNetworkAddress();

        timeline = new Timeline();
    }

    private void setupNetworkAddress() {
        user_port = new SimpleStringProperty();
        user_ip = new SimpleStringProperty();
        user_port.setValue(String.valueOf(port));
        try {
            user_ip.setValue(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        fx_portTextField.textProperty().bindBidirectional(user_port);
        fx_ipTextField.textProperty().bindBidirectional(user_ip);
    }

    private void setScene(){
        connectionButtonsSetup();
        joinButtonSetup();
        difficultySceneSetup();
    }


    private void showJoinScene(){
        joinScene.setVisible(true);
        loginScene.setVisible(false);
        difficultyScene.setVisible(false);

        joinViewButton.setVisible(true);
        singlePlayerViewButton.setVisible(true);
        warningText.setText("");
    }

    private void connectionButtonsSetup(){
        loginScene.setVisible(true);
        loginViewRMIButton.setBorder(new Border(new BorderStroke( Color.rgb(200, 177, 39),
                BorderStrokeStyle.SOLID, new CornerRadii(2),new BorderWidths(2))));
    }

    private void joinButtonSetup() {
        joinScene.setVisible(false);
        joinViewButton.setStyle(
                "-fx-background-color: #FFF1C6; " +
                        "-fx-background-radius: 50em; " +
                        "-fx-min-width: 90px; " +
                        "-fx-min-height: 90px; " +
                        "-fx-max-width: 90px; " +
                        "-fx-max-height: 90px;"
        );
    }

    private void difficultySceneSetup(){
        difficultyScene.setVisible(false);
    }



    private void setGameCursor() {
        rootLayout.setCursor(new ImageCursor(
                new Image(pathToCursor + "cursor_hand.png", 64.0, 64.0, true, true)));
    }



    /**
     * Clip the Root Layout into a Circle shape.
     * Requires his Scene to be Transparent,
     * if you do not want see a White fill of background outside of the circle.
     *
     * @param tempPane  Top most Container that contain all panes and controls.
     */
    private Circle clipToCircle(Pane tempPane){
        // Create a new Circle Sharpe
        Circle circle = new Circle();

        double x = tempPane.getPrefWidth();
        double y = tempPane.getPrefHeight();

        // Calculate the Radius of the Circle Window
        double radius = Double.min(x , y) /2;

        //Setting the properties of the circle
        circle.setCenterX( x /2 );
        circle.setCenterY( y /2 );
        circle.setRadius(radius);

        // Clip the Pane -Layout- into a Circle
        tempPane.setClip(circle);

        return circle;
    }

    /**
     * For not prefect Circle Window
     * Clip the Root Layout into a Ellipse shape.
     * Requires his Scene to be Transparent,
     * if you do not want see a White fill of background outside of the ellipse.
     *
     * @param tempPane  Top most Container that contain all panes and controls.
     */
    private Ellipse clipToEllipse(Pane tempPane){
        // Create a new Ellipse Sharpe
        Ellipse ellipse = new Ellipse();

        double x = tempPane.getPrefWidth();
        double y = tempPane.getPrefHeight();

        // Calculate the Radius of the Circle Window
//        double radius = Useful.getMinBetween(x , y) /2;
        x /= 2;
        y /= 2;

        //Setting the properties of the circle
        ellipse.setCenterX( x );
        ellipse.setCenterY( y );
        ellipse.setRadiusX( x );
        ellipse.setRadiusX( y );

        // Clip the Pane -Layout- into a Circle
        tempPane.setClip(ellipse);

        return ellipse;
    }






    private void showWarningText(String text) {
        warningText.setText(text);
        warningText.setFill(Color.rgb(0xCB, 0x3C, 0x15));
        warningText.setStyle("-fx-stroke:black;" +
                "-fx-stroke-width:1.5;");
        warningText.setOpacity(1);

        timeline.stop();
        timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(5000),
                new KeyValue(warningText.opacityProperty(), 0, Interpolator.DISCRETE)));
        timeline.play();
    }

    private void showTipText(String text) {
        warningText.setText(text);
        warningText.setFill(Color.rgb(0x7B, 0xD7, 0xE1));
        warningText.setStyle("-fx-stroke:black;"+
                "-fx-stroke-width:1.5;");
        warningText.setOpacity(1);

        timeline.stop();
        timeline = new Timeline();
        timeline.setDelay(Duration.millis(2000));
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000),
                new KeyValue(warningText.opacityProperty(), 0, Interpolator.EASE_IN)));
        timeline.play();
    }

    public void showText(String text) {
        warningText.setText(text);
        warningText.setFill(Color.rgb(0x6B, 0xD7, 0xA1));
        warningText.setStyle("-fx-stroke:black;"+
                "-fx-stroke-width:1.5;");
        warningText.setOpacity(1);
    }


    /**
     *  Manages the join scene
     * @param event
     */


    @FXML
    public void onJoinButton(ActionEvent event) {
        System.out.println("Join button Clicked");

        // assign View Controllers to GUI ViewUpdateHandler
        GameViewController gameViewController = Useful.convertInstanceOfObject(stageManager.getController(stageGameID), GameViewController.class);

         viewUpdateHandlerInterface = new GUIViewUpdateHandler(gameViewController, username);

        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setLoginViewController(Useful.convertInstanceOfObject(stageManager.getController(stageLoginID), LoginViewController.class));
        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setPatternViewController(Useful.convertInstanceOfObject(stageManager.getController(stagePatternID), ChoosePatternViewController.class));
        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setResultViewController(Useful.convertInstanceOfObject(stageManager.getController(stageResultsID), ResultViewController.class));


        gameViewController.setUserName(username);

        System.out.println(viewUpdateHandlerInterface);
        System.out.println(username);

        if(this.connectionType==RMI) {
            try {
                ClientObserver clientObserver = new ClientObserver(viewUpdateHandlerInterface, username, myPort);
                ((RMIClientAction) ClientActionSingleton.getClientAction()).setMyport(myPort);
            } catch (RemoteException e) {
                e.printStackTrace();
                showWarningText("Could not Join");
            }
        }
        ClientActionSingleton.getClientAction().join(viewUpdateHandlerInterface);

        singlePlayerViewButton.setVisible(false);
        joinViewButton.setVisible(false);

//        System.out.println("waiting for players");
        showText("Waiting for players...");
    }

    /**
     * Used to return to a previous started game
     * @param event
     */

    @FXML
    public void  onReturnViewButton(ActionEvent event) {
        System.out.println("Return in game button Clicked");

        GameViewController gameViewController = Useful.convertInstanceOfObject(stageManager.getController(stageGameID), GameViewController.class);
        viewUpdateHandlerInterface = new GUIViewUpdateHandler(gameViewController, username);

        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setLoginViewController(Useful.convertInstanceOfObject(stageManager.getController(stageLoginID), LoginViewController.class));
        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setPatternViewController(Useful.convertInstanceOfObject(stageManager.getController(stagePatternID), ChoosePatternViewController.class));

        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setResultViewController(Useful.convertInstanceOfObject(stageManager.getController(stageResultsID), ResultViewController.class));

        gameViewController.setUserName(username);

        System.out.println(viewUpdateHandlerInterface);
        System.out.println(username);

        if(this.connectionType==RMI) {
            try {
                ClientObserver clientObserver = new ClientObserver(viewUpdateHandlerInterface, username, myPort);
                ((RMIClientAction) ClientActionSingleton.getClientAction()).setMyport(myPort);
            } catch (RemoteException e) {
                e.printStackTrace();
                showWarningText("Could not Join");
            }
        }
        ClientActionSingleton.getClientAction().resumeGame(viewUpdateHandlerInterface);

        System.out.println("Goto next");

        Platform.runLater(() -> {
            // Switch the Stage
            stageManager.setStage(GuiSettings.stageGameID, this.stageName);
        });
    }

    /**
     * Manages the post login event
     * @param event
     */

    @FXML
    public void loginDone(ActionEvent event) {

        // if user typed his UserName
        if(userNameTextField.getText()!=null  &&  !(userNameTextField.getText().equals(voidString))) {

            // Start the Connection to the server
            if (this.connectionType == RMI) {
                try {
//                    ip = InetAddress.getByName("192.168.1.79");
                    System.out.println(user_ip.getValue());
                    System.out.println(user_port.getValue());
                    ClientActionSingleton.setClientActionInstance(new RMIClientAction(user_ip.getValue(), Integer.parseInt(user_port.getValue())-2 ));
                } catch (RemoteException e) {
                    System.err.println(e.getMessage());
                } catch (NotBoundException e) {
                    System.err.println(e.getMessage());
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
                }
            }
            else if(this.connectionType == SOCKET){
                try {
                    ip = InetAddress.getLocalHost();
                    ClientActionSingleton.setClientActionInstance(new SocketClientAction(ip, port));
                } catch (UnknownHostException e) {
                    System.err.println(e.getMessage());
                }
            }

            // Error cached
            if(null == ClientActionSingleton.getClientAction()  ||
                    !ClientActionSingleton.getClientAction().isConnected() ) {
                showWarningText("404: server NOT Found");


            // Connected to server
            } else {
                // Try to Login with given user name
                int result=ClientActionSingleton.getClientAction().login(userNameTextField.getText());
                if (result != USERNAME_ALREADY_TAKEN) {
                    // Logged In
                    this.username = userNameTextField.getText();

                    if (result == USERNAME_AVAILABLE)
                        returnViewButton.setVisible(false);
                    // Open next Scene - Join Scene
                    loginScene.setVisible(false);
                    joinScene.setVisible(true);

//                    joinScene.toFront();


                // Login Failed!!!
                } else {
                    showWarningText("Can not Login with given username");

                    //if there is an active Socket connection we close it
                    if (this.connectionType == SOCKET && ClientActionSingleton.getClientAction() != null)
                        ((SocketClientAction) ClientActionSingleton.getClientAction()).closeConnection();
                }
            }
        } else {
            showWarningText("???Choose an username???");
        }
    }


    @FXML
    public void  onSinglePlayerViewButton(ActionEvent event){

        joinScene.setVisible(false);
        difficultyScene.setVisible(true);


    }

    /**
     * Try to start a new single player game with the selected difficulty
     * @param event
     */
    @FXML
    public void onDifficultyButton(ActionEvent event){
        Button button= (Button) event.getSource();
        String choosen= button.getId().substring(20, button.getId().length());
        Game.SolitaireDifficulty solitaireDifficulty;
        switch (choosen) {
            case "easy":
               solitaireDifficulty= Game.SolitaireDifficulty.EASY;
                break;
            case "beginner":
                solitaireDifficulty= Game.SolitaireDifficulty.BEGINNER;
                break;
            case "normal":
                solitaireDifficulty=Game.SolitaireDifficulty.NORMAL;
                break;
            case "hard":
                solitaireDifficulty=Game.SolitaireDifficulty.HARD;
                break;
            case "extreme":
                solitaireDifficulty=Game.SolitaireDifficulty.EXTREME;
                break;

                default:
                    solitaireDifficulty=Game.SolitaireDifficulty.NORMAL;
        }
        GameViewController gameViewController = Useful.convertInstanceOfObject(stageManager.getController(stageGameID), GameViewController.class);


        viewUpdateHandlerInterface = new GUIViewUpdateHandler(gameViewController, username);

        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setLoginViewController(Useful.convertInstanceOfObject(stageManager.getController(stageLoginID), LoginViewController.class));
        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setPatternViewController(Useful.convertInstanceOfObject(stageManager.getController(stagePatternID), ChoosePatternViewController.class));

        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setResultViewController(Useful.convertInstanceOfObject(stageManager.getController(stageResultsID), ResultViewController.class));
        gameViewController.setUserName(username);

        System.out.println(viewUpdateHandlerInterface);
        System.out.println(username);

        if(this.connectionType==RMI) {
            try {
                ClientObserver clientObserver = new ClientObserver(viewUpdateHandlerInterface, username, myPort);
                ((RMIClientAction) ClientActionSingleton.getClientAction()).setMyport(myPort);
            } catch (RemoteException e) {
                e.printStackTrace();
                showWarningText("Could not Join");
            }
        }
        ClientActionSingleton.getClientAction().joinSinglePlayer(viewUpdateHandlerInterface, solitaireDifficulty);

        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setLoginViewController(Useful.convertInstanceOfObject(stageManager.getController(stageLoginID), LoginViewController.class));
        ((GUIViewUpdateHandler) viewUpdateHandlerInterface).setPatternViewController(Useful.convertInstanceOfObject(stageManager.getController(stagePatternID), ChoosePatternViewController.class));

        difficultyScene.setVisible(false);

    }

    /**
     * Set the connection method to RMI
     * @param event
     */

    @FXML
    public void connectionMethodChooseRMI(ActionEvent event){
       //If the connection is already set to Socket, the rmi border is highlighted
        if (connectionType.equals(SOCKET)) {
            connectionType = RMI;
            loginViewRMIButton.setBorder(new Border(new BorderStroke( Color.rgb(200, 177, 39),
                    BorderStrokeStyle.SOLID, new CornerRadii(2), new BorderWidths(2))));
            loginViewSocketButton.setBorder(new Border(new BorderStroke( Color.rgb(200, 177, 39),
                    BorderStrokeStyle.SOLID,new CornerRadii(0), new BorderWidths(0))));

            showTipText("Connection mode set to rmi");
        }
        //If the connection is already set to rmi, there is no  need to do anything

    }

    /**
     * Set the connection method to Socket
     * @param event
     */

    @FXML
    public void connectionMethodChooseSocket(ActionEvent event){
        //If the connection is already set to rmi, the Socket border is highlighted
        if (connectionType.equals(RMI)) {
            connectionType = SOCKET;
            loginViewSocketButton.setBorder(new Border(new BorderStroke( Color.rgb(200, 177, 39),
                    BorderStrokeStyle.SOLID, new CornerRadii(2), new BorderWidths(2))));
            loginViewRMIButton.setBorder(new Border(new BorderStroke( Color.rgb(200, 177, 39),
                    BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0))));
            showTipText("Connection mode set to Socket");
        }
        //If the connection is already set to rmi, there is no  need to do anything

    }

    @FXML
    public void onExitLoginViewButton(ActionEvent event){
        Platform.exit();
        System.exit(0);

    }

    @FXML
    public void onExitJoinViewButton(ActionEvent event){
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void onExitDifficultyViewButton(ActionEvent event){
        Platform.exit();
        System.exit(0);
    }
}
