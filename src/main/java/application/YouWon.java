package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class YouWon {

    public void display(Stage mainStage) {
        // Create and display the celebration window separately
        Stage celebrationWindow = new Stage();
        celebrationWindow.setTitle("Congratulations!");

        // Main winning message
        Label victoryMessage = new Label("YOU WIN!");
        victoryMessage.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 48px;");

        // Message about the achievement
        Label achievementMessage = new Label("Congratulations! You've successfully cleared all the levels and defeated the monster!");

        // Buttons for user actions
        Button menuButton = new Button("Main Menu");

        // Set up action for the menu button to return to the main menu
        menuButton.setOnAction(e -> mainMenu(celebrationWindow, mainStage));
        menuButton.setPrefWidth(150);

        // Set up the layout for the celebration window
        VBox celebrationLayout = new VBox(20, victoryMessage, achievementMessage, menuButton);
        celebrationLayout.setPadding(new Insets(20));
        celebrationLayout.setAlignment(Pos.CENTER);

        // Create the scene and display the celebration window
        Scene celebrationScene = new Scene(celebrationLayout, 20 * 32, 12 * 58);
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