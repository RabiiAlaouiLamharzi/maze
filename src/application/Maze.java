package application;

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
		
		VBox vboxStartScene = new VBox();
		vboxStartScene.getChildren().addAll(titleLabel, rulesLabel, spacerV1, startButton);
		vboxStartScene.setPadding(new Insets(20));
		
		startAgainButton = new Button("Start again");
		startAgainButton.setMaxWidth(Double.MAX_VALUE);
		
		mainMenuButton  = new Button("Main menu");
		mainMenuButton.setMaxWidth(Double.MAX_VALUE);
		
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
		
		VBox vboxGameOverScene = new VBox();
		vboxGameOverScene.getChildren().addAll(gameOverLabel, gameOverDescribtionLabel, spacerV2, buttonsGameOverScene);
		vboxGameOverScene.setPadding(new Insets(20));
		
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
		
		VBox vboxWinScene = new VBox(winLabel, winDescribtionLabel, spacerV3, mainMenuButton);
		vboxWinScene.getChildren().addAll();
		vboxWinScene.setPadding(new Insets(20));
		
		Scene startScene = new Scene(new StackPane(vboxStartScene), 640, 480); 
		
		Scene gameOverScene = new Scene(new StackPane(vboxGameOverScene), 640, 480); 
		
		Scene winScene = new Scene(new StackPane(vboxWinScene), 640, 480); 
		
		stage.setScene(gameOverScene);
		stage.show();
	}
	
	public static void main(String[] args) { 
		launch();
	} 
}
