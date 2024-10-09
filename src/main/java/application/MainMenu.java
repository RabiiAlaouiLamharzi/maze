package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage mainStage) {
        StackPane menuLayout = createMenuLayout(mainStage);
        mainStage.setTitle("MazeAdventure");
        mainStage.setScene(new Scene(menuLayout, 20 * 32, 12 * 58));
        mainStage.show();
    }

    private StackPane createMenuLayout(Stage mainStage) {

        // Set up the layout
        StackPane rootLayout = new StackPane();
        
        // Add Buttons
        Button levelOneButton = new Button("Play");
        levelOneButton.setOnAction(event -> startLevel(mainStage));
        
        // Add the button to the layout
        rootLayout.getChildren().add(levelOneButton);

        return rootLayout;
    }

    // Switches from the menu to Level1 (game window)
    private void startLevel(Stage mainStage) {
        mainStage.close();
        Level1 newLevel = new Level1();
        newLevel.start(mainStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

