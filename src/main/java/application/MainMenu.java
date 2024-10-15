package application;

import java.io.File;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage mainStage) {
		
        StackPane menuLayout = createMenuLayout(mainStage);
        mainStage.setTitle("MazeAdventure");
        mainStage.setScene(new Scene(menuLayout, 20 * 41, 12 * 70));
        mainStage.setResizable(false);
        mainStage.show();
    }

    private StackPane createMenuLayout(Stage mainStage) {

        // Set up the root layout
        StackPane rootLayout = new StackPane();

        // Add the Background Image
        ImageView backgroundView = new ImageView(new Image("/assets/BG.png"));
        backgroundView.setFitWidth(20 * 41);
        backgroundView.setFitHeight(12 * 70);
        backgroundView.setPreserveRatio(false);
        
        // Create the VBox to hold the logo, description, and button
        VBox menuBox = new VBox(15); 
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));
        
        
        // Add a Short Description
        Label descriptionLabel = new Label("You need to go through each period of time and try to collect some items to move to the next period.\nWhen the player collects all items from all levels he will return to its original time period");
        descriptionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-text-alignment: center;");
        descriptionLabel.setPadding(new Insets(600, 0, 25, 0));
        
        // Add the Play Button
        Button levelOneButton = new Button("Play");
        levelOneButton.setOnAction(event -> startLevel(mainStage));
        levelOneButton.setStyle("-fx-background-color: #ffffff; -fx-font-size: 18px; -fx-text-fill: #39416C; -fx-background-radius: 0 0 0 0;");
        levelOneButton.setPadding(new Insets(15, 100, 15, 100));
        
        // Add elements to the VBox
        menuBox.getChildren().addAll(descriptionLabel, levelOneButton);
        
        // Add the background and VBox to the root layout
        rootLayout.getChildren().addAll(backgroundView, menuBox);
        
        return rootLayout;
    }

    // Switches from the menu to Level1 (game window)
    private void startLevel(Stage mainStage) {
        mainStage.close();
        Level3 level3 = new Level3();
        level3.start3(mainStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}