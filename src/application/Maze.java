package application;

import java.io.File;  
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label; 
import javafx.scene.layout.StackPane; 
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.Group;  
import javafx.scene.media.Media;  
import javafx.scene.media.MediaPlayer;  
import javafx.scene.media.MediaView;  
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;

public class Maze extends Application {
	
	Label titleLabel;
	Label rulesLabel;
	Label gameOverLabel;
	Label gameOverDescribtionLabel;
	Label winLabel;
	Label winDescribtionLabel;
	Button startButton;
	Button startAgainButton;
	Button mainMenuButton;
	
	@Override
	public void start(Stage stage) {
		
		String musicFile = "secrets-of-the-old-library.mp3";     
		Media sound = new Media(new File(musicFile).toURI().toString());
		MediaPlayer mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.play();
		
		Region spacerV1 = new Region();
		VBox.setVgrow(spacerV1, Priority.ALWAYS);
		
		titleLabel = new Label();
		titleLabel.setText("Maze");
		titleLabel.setFont(new Font("Arial", 48));
		titleLabel.setWrapText(true);
		
		rulesLabel = new Label("You need to go through each period of time and try to collect some items to move to the next period. When the player collects all items from all levels he will return to its original time period");
		rulesLabel.setWrapText(true);
		rulesLabel.setFont(new Font("Arial", 12));
		
		startButton = new Button("Start");
		startButton.setMaxWidth(Double.MAX_VALUE);
		
		// Set button style for normal, hover, and pressed states
        startButton.setStyle(
            "-fx-background-color: #39416C; " +        // Normal background
            "-fx-text-fill: white; " +                  // Text color
            "-fx-font-size: 14px; " +                    // Font size
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"); // Shadow effect

        // Add hover and pressed effects using event handlers
        startButton.setOnMouseEntered(e -> startButton.setStyle(
            "-fx-background-color: #546A99; " +         // Hover background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
        startButton.setOnMouseExited(e -> startButton.setStyle(
            "-fx-background-color: #39416C; " +         // Normal background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
        startButton.setOnMousePressed(e -> startButton.setStyle(
            "-fx-background-color: #2E3B5D; " +          // Pressed background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
        startButton.setOnMouseReleased(e -> startButton.setStyle(
            "-fx-background-color: #39416C; " +         // Normal background after release
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
		
        Image logoImage = new Image(new File("Logo.png").toURI().toString());
        ImageView logoImageView = new ImageView(logoImage);

        logoImageView.setFitWidth(300);  
        logoImageView.setPreserveRatio(true); 
        
        StackPane imagePane = new StackPane(logoImageView);
        imagePane.setAlignment(Pos.CENTER);
		
		VBox vboxStartScene = new VBox();
		vboxStartScene.getChildren().addAll(imagePane, rulesLabel, spacerV1, startButton);
		vboxStartScene.setPadding(new Insets(20));
		
		//
		
		Image monster1 = new Image(new File("lvl1 - monstr.png").toURI().toString());
        ImageView monster1View = new ImageView(monster1);

        monster1View.setFitWidth(100);  
        monster1View.setPreserveRatio(true); 
        
        Image monster2 = new Image(new File("lvl2 - monstr.png").toURI().toString());
        ImageView monster2View = new ImageView(monster2);

        monster2View.setFitWidth(100);  
        monster2View.setPreserveRatio(true); 
        
        Image monster3 = new Image(new File("lvl3 - monstr.png").toURI().toString());
        ImageView monster3View = new ImageView(monster3);

        monster3View.setFitWidth(100);  
        monster3View.setPreserveRatio(true); 
        
        Region spacerH1 = new Region();
		HBox.setHgrow(spacerH1, Priority.ALWAYS);
		
		Region spacerH2 = new Region();
		HBox.setHgrow(spacerH2, Priority.ALWAYS);
        
        HBox monsters = new HBox();
        monsters.getChildren().addAll(monster1View, spacerH1, monster2View, spacerH2, monster3View);
        monsters.setPrefHeight(300);
        monsters.setAlignment(Pos.CENTER); 
		
		startAgainButton = new Button("Start again");
		startAgainButton.setMaxWidth(Double.MAX_VALUE);
		
		// Set button style for normal, hover, and pressed states
		startAgainButton.setStyle(
            "-fx-background-color: #39416C; " +        // Normal background
            "-fx-text-fill: white; " +                  // Text color
            "-fx-font-size: 14px; " +                    // Font size
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"); // Shadow effect

        // Add hover and pressed effects using event handlers
		startAgainButton.setOnMouseEntered(e -> startAgainButton.setStyle(
            "-fx-background-color: #546A99; " +         // Hover background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
		startAgainButton.setOnMouseExited(e -> startAgainButton.setStyle(
            "-fx-background-color: #39416C; " +         // Normal background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
		startAgainButton.setOnMousePressed(e -> startAgainButton.setStyle(
            "-fx-background-color: #2E3B5D; " +          // Pressed background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
		startAgainButton.setOnMouseReleased(e -> startAgainButton.setStyle(
            "-fx-background-color: #39416C; " +         // Normal background after release
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
		
		mainMenuButton  = new Button("Main menu");
		mainMenuButton.setMaxWidth(Double.MAX_VALUE);
		
		// Set button style for normal, hover, and pressed states
		mainMenuButton.setStyle(
            "-fx-background-color: #39416C; " +        // Normal background
            "-fx-text-fill: white; " +                  // Text color
            "-fx-font-size: 14px; " +                    // Font size
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"); // Shadow effect

        // Add hover and pressed effects using event handlers
		mainMenuButton.setOnMouseEntered(e -> mainMenuButton.setStyle(
            "-fx-background-color: #546A99; " +         // Hover background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
		mainMenuButton.setOnMouseExited(e -> mainMenuButton.setStyle(
            "-fx-background-color: #39416C; " +         // Normal background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
		mainMenuButton.setOnMousePressed(e -> mainMenuButton.setStyle(
            "-fx-background-color: #2E3B5D; " +          // Pressed background
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
        
		mainMenuButton.setOnMouseReleased(e -> mainMenuButton.setStyle(
            "-fx-background-color: #39416C; " +         // Normal background after release
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"));
		
		HBox buttonsGameOverScene = new HBox(10);
		buttonsGameOverScene.getChildren().addAll(startAgainButton, mainMenuButton);
		HBox.setHgrow(startAgainButton, Priority.ALWAYS);
        HBox.setHgrow(mainMenuButton, Priority.ALWAYS);
		
		gameOverLabel = new Label();
		gameOverLabel.setText("Game Over");
		gameOverLabel.setFont(new Font("Arial", 48));
		gameOverLabel.setWrapText(true);
		
		gameOverDescribtionLabel = new Label();
		gameOverDescribtionLabel.setText("You was not able to return to your original time period");
		gameOverDescribtionLabel.setFont(new Font("Arial", 12));
		gameOverDescribtionLabel.setWrapText(true);
		
		Region spacerV2 = new Region();
		VBox.setVgrow(spacerV2, Priority.ALWAYS);
		
		Region spacerV4 = new Region();
		VBox.setVgrow(spacerV4, Priority.ALWAYS);
		
		VBox vboxGameOverScene = new VBox();
		vboxGameOverScene.getChildren().addAll(monsters, gameOverLabel, gameOverDescribtionLabel, spacerV2, buttonsGameOverScene);
		vboxGameOverScene.setPadding(new Insets(20));
		
		//
		
		Image player = new Image(new File("player.png").toURI().toString());
        ImageView playerView = new ImageView(player);

        playerView.setFitWidth(200);  
        playerView.setPreserveRatio(true); 
        
        Image key = new Image(new File("Lvl2.7.png").toURI().toString());
        ImageView keyView = new ImageView(key);

        keyView.setFitWidth(50);  
        keyView.setPreserveRatio(true);
        
        Region spacerH3 = new Region();
		HBox.setHgrow(spacerH3, Priority.ALWAYS);
        
        HBox players = new HBox();
        players.getChildren().addAll(keyView, playerView);
        players.setPrefHeight(300);
        players.setAlignment(Pos.CENTER); 
		
		winLabel = new Label();
		winLabel.setText("Win");
		winLabel.setFont(new Font("Arial", 48));
		winLabel.setWrapText(true);
		
		winDescribtionLabel = new Label();
		winDescribtionLabel.setText("You was able to return to your original time period");
		winDescribtionLabel.setFont(new Font("Arial", 12));
		winDescribtionLabel.setWrapText(true);
		
		Region spacerV3 = new Region();
		VBox.setVgrow(spacerV3, Priority.ALWAYS);
		
		VBox vboxWinScene = new VBox(players, winLabel, winDescribtionLabel, spacerV3, mainMenuButton);
		vboxWinScene.getChildren().addAll();
		vboxWinScene.setPadding(new Insets(20));
		
		Scene startScene = new Scene(new StackPane(vboxStartScene), 350, 480); 
		
		Scene gameOverScene = new Scene(new StackPane(vboxGameOverScene), 350, 480); 
		
		Scene winScene = new Scene(new StackPane(vboxWinScene), 350, 480); //640
		
		stage.setScene(winScene);
		stage.show();
	}
	
	public static void main(String[] args) { 
		launch();
	} 
}
