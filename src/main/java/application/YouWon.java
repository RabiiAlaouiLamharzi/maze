package application;

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

public class YouWon {

    public void display(Stage mainStage) {
        // Create and display the celebration window separately
        Stage celebrationWindow = new Stage();
        celebrationWindow.setTitle("Congratulations!");

        // Set up the root layout
        StackPane rootLayout = new StackPane();

        // Add the Background Image
        ImageView backgroundView = new ImageView(new Image("/assets/BGW.png"));
        backgroundView.setFitWidth(20 * 41);
        backgroundView.setFitHeight(12 * 70); 
        backgroundView.setPreserveRatio(false);

        // Create the VBox to hold the victory message, achievement message, and button
        VBox celebrationLayout = new VBox(20);
        celebrationLayout.setPadding(new Insets(20));
        celebrationLayout.setAlignment(Pos.CENTER);

        // Main winning message
        Label victoryMessage = new Label("YOU WIN!");
        victoryMessage.setStyle("-fx-text-fill: #FFD629; -fx-font-weight: bold; -fx-font-size: 48px; -fx-text-alignment: center; -fx-font-weight: bold;");
        victoryMessage.setPadding(new Insets(450, 0, 5, 0));

        // Message about the achievement
        Label achievementMessage = new Label("Congratulations! You've successfully cleared all the levels and defeated the monster!");
        achievementMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-text-alignment: center;");
        achievementMessage.setPadding(new Insets(0, 0, 25, 0));

        // Buttons for user actions
        Button menuButton = new Button("Main Menu");
        menuButton.setOnAction(e -> mainMenu(celebrationWindow, mainStage));
        menuButton.setStyle("-fx-background-color: #ffffff; -fx-font-size: 18px; -fx-text-fill: #39416C; -fx-background-radius: 0 0 0 0;");
        menuButton.setPadding(new Insets(15, 100, 15, 100));

        // Add elements to the VBox
        celebrationLayout.getChildren().addAll(victoryMessage, achievementMessage, menuButton);

        // Add the background and VBox to the root layout
        rootLayout.getChildren().addAll(backgroundView, celebrationLayout);

        // Create the scene and display the celebration window
        Scene celebrationScene = new Scene(rootLayout, 20 * 41, 12 * 70);
        celebrationWindow.setScene(celebrationScene);
        celebrationWindow.show();
    }

    // Return to main menu
    private void mainMenu(Stage celebrationWindow, Stage mainStage) {
        celebrationWindow.close();
        MainMenu menu = new MainMenu();
        menu.start(mainStage);
    }
}