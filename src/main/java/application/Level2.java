package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;

import java.util.ArrayList;
import java.util.Random;

public class Level2 {
    // Game configuration
    private static final int MAZE_SIZE = 18;
    private static final int CELL_SIZE = 40;
    private static final int PLAYER_SIZE = 40;
    private static final int GADGET_SIZE = 25;
    private static final int PORTAL_SIZE = 40;
    private static final int MONSTER_SIZE = 40;
    private static final double COLLECTION_TIME = 0.2;
    private static final double PORTAL_ACTIVATION_TIME = 0.2;

    // Core game components
    private Pane root;
    private Rectangle[][] maze;
    private Circle player;
    private Circle monster;
    private Circle portal;
    private ArrayList<Circle> gadgets = new ArrayList<>();
    private Label feedback;
    private Timeline timeCollection;
    private Circle collectingGadget;
    private double collectionProgress;
    private Rectangle progressBar;
    private Circle key = null;
    private boolean keySpawned = false;
    private Text keyLabel = null;
    private boolean isKeyCollected = false;
    
    // Player and monster positions
    private int playerRow, playerCol;
    private int monsterRow, monsterCol;
    
    private boolean isPortalActivated = false;
    private Timeline portalTimeline;
    private Rectangle portalProgressBar;
    
    private Random random = new Random();
    private int collectedGadgets = 0;
    private boolean portalAdded = false;

    public void start2(Stage primaryStage) {
        root = new Pane();
        maze = new Rectangle[MAZE_SIZE][MAZE_SIZE];
        
        // Set up mouse event handlers
        root.setOnMousePressed(event -> {
            startGadgetCollection(event.getX(), event.getY());
            startPortalActivation(event.getX(), event.getY(), primaryStage);
        });
        root.setOnMouseReleased(event -> {
            cancelGadgetCollection();
            cancelPortalActivation();
        });
        
        // Progress bar for collection
        progressBar = new Rectangle(0, 0, 0, 5);
        progressBar.setFill(Color.GREEN);
        progressBar.setVisible(false);
        root.getChildren().add(progressBar);
        portalProgressBar = new Rectangle(0, 0, 0, 5);
        portalProgressBar.setFill(Color.PURPLE);
        portalProgressBar.setVisible(false);
        root.getChildren().add(portalProgressBar);

        // UI setup
        Button menu = new Button("☰");
        menu.setOnAction(event -> mainMenu(primaryStage));
        menu.setFocusTraversable(false);
        menu.setPadding(new Insets(15, 20, 15, 20));
        
        Button reload = new Button("↻");
        reload.setOnAction(event -> restart(primaryStage));
        reload.setFocusTraversable(false);
        reload.setPadding(new Insets(15, 20, 15, 20));

        feedback = new Label("Collect 3 coins to win! Watch out for the monster!");
        feedback.setMaxWidth(Double.MAX_VALUE);
        feedback.setPadding(new Insets(15, 25, 15, 25));
        feedback.setStyle("-fx-background-color: GOLD;");

        HBox topbarLayout = new HBox(8);
        HBox.setHgrow(feedback, javafx.scene.layout.Priority.ALWAYS);
        topbarLayout.getChildren().addAll(feedback, menu, reload);
        topbarLayout.setAlignment(Pos.CENTER);

        generateMaze();
        createPlayer();
        createMonster();
        createGadgets(3);
        startProximityCheck();
        createKey();

        VBox mainLayout = new VBox(8, topbarLayout, root);
        mainLayout.setPadding(new Insets(35, 50, 35, 50));
        
        // Set the background color of the root pane
        mainLayout.setStyle("-fx-background-color: LIGHTYELLOW;");

        Scene scene = new Scene(mainLayout, 20 * 41, 12 * 70);
        root.setFocusTraversable(true);
        scene.setOnMouseClicked(event -> root.requestFocus());
        scene.setOnKeyPressed(e -> movePlayer(e.getCode().toString()));

        primaryStage.setTitle("Maze Runner");
        primaryStage.setScene(scene);
        primaryStage.show();
        root.requestFocus();

        startGameLoop(primaryStage);
    }

    // Generate the maze layout
    private void generateMaze() {
        for (int row = 0; row < MAZE_SIZE; row++) {
            for (int col = 0; col < MAZE_SIZE; col++) {
                maze[row][col] = new Rectangle(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                maze[row][col].setFill(Color.GOLD);
                maze[row][col].setStroke(Color.GRAY);
                root.getChildren().add(maze[row][col]);
            }
        }

        Image blackCellImage = new Image(getClass().getResourceAsStream("/assets/walls1.png"));
        
        // Randomly place black cells (represented by transparent cells with an image on top)
        for (int row = 0; row < MAZE_SIZE; row++) {
            for (int col = 0; col < MAZE_SIZE; col++) {
                boolean canPlaceBlack = true;

                if (random.nextDouble() < 0.25) {
                    if (row > 0 && col > 0 && maze[row - 1][col - 1].getFill() == Color.BLACK) {
                        canPlaceBlack = false;
                    }
                    if (row > 0 && col < MAZE_SIZE - 1 && maze[row - 1][col + 1].getFill() == Color.BLACK) {
                        canPlaceBlack = false;
                    }
                    if (row < MAZE_SIZE - 1 && col > 0 && maze[row + 1][col - 1].getFill() == Color.BLACK) {
                        canPlaceBlack = false;
                    }
                    if (row < MAZE_SIZE - 1 && col < MAZE_SIZE - 1 && maze[row + 1][col + 1].getFill() == Color.BLACK) {
                        canPlaceBlack = false;
                    }
                }

                // Place a black cell if conditions allow
                if (canPlaceBlack && random.nextDouble() < 0.25) {
                    maze[row][col].setFill(Color.TRANSPARENT);

                    ImageView blackCellView = new ImageView(blackCellImage);
                    blackCellView.setFitWidth(CELL_SIZE);
                    blackCellView.setFitHeight(CELL_SIZE);
                    blackCellView.setX(col * CELL_SIZE);
                    blackCellView.setY(row * CELL_SIZE);

                    root.getChildren().add(blackCellView);
                }
            }
        }

        maze[0][0].setFill(Color.GOLD);
        maze[MAZE_SIZE - 1][MAZE_SIZE - 1].setFill(Color.GOLD);
        
        ensureNoIsolatedCells();
    }

    // Creates a specified number of gadgets in valid GOLD cells.
    private void createGadgets(int count) {
        int gadgetsCreated = 0;
        while (gadgetsCreated < count) {
            int[] position = findValidGOLDCell(2, 2);
            if (position == null) break;
            
            int row = position[0];
            int col = position[1];

            if (isGOLDCell(row, col) && isCellEmpty(row, col)) {
                Circle gadget = new Circle((col + 0.5) * CELL_SIZE, (row + 0.5) * CELL_SIZE, GADGET_SIZE / 2, Color.GOLD);
                gadgets.add(gadget);
                root.getChildren().add(gadget);
                gadgetsCreated++;
            }
        }
    }

    // Handles the collection of gadgets and the key
    private void collectGadget() {
        if (collectingGadget != null) {
            if (gadgets.contains(collectingGadget)) {
                root.getChildren().remove(collectingGadget);
                gadgets.remove(collectingGadget);
                collectedGadgets++;

                if (collectedGadgets == 3) {
                    keySpawned = true;
                    createKey();
                    if (portal != null) {
                        portal.setFill(Color.PURPLE);
                    } else {
                        createPortal();
                    }
                    updateFeedback("All gadgets collected! A key has appeared!", 3, "Find and collect the key!");
                } else { 
                    String gadgetMessage = "Gadget collected! Gadgets left: " + (3 - collectedGadgets);
                    updateFeedback(gadgetMessage, 2, "Hold-click on gadgets to collect them!");
                }
            } else if (collectingGadget == key) {
                root.getChildren().removeAll(key, keyLabel);
                key = null;
                keyLabel = null;
                isKeyCollected = true;
                updateFeedback("Key collected! Now you can use the portal!", 2, "Hold-click on the portal to activate it!");
            }
        }
        cancelGadgetCollection();
    }

    // Checks if a specific cell is empty
    private boolean isCellEmpty(int row, int col) {
        for (Circle gadget : gadgets) {
            int gadgetRow = (int)(gadget.getCenterY() / CELL_SIZE);
            int gadgetCol = (int)(gadget.getCenterX() / CELL_SIZE);
            if (row == gadgetRow && col == gadgetCol) {
                return false;
            }
        }

        return true;
    }

    // Starts collecting a gadget or key if the player is close enough, and creates a portal if the key is collected.
    private void startGadgetCollection(double x, double y) {
        if (timeCollection != null) return;

        Circle targetObject = null;
        double minDistance = Double.MAX_VALUE;

        // Check gadgets
        for (Circle gadget : gadgets) {
            double distance = Math.sqrt(
                Math.pow(x - gadget.getCenterX(), 2) + 
                Math.pow(y - gadget.getCenterY(), 2)
            );

            if (distance < CELL_SIZE && distance < minDistance && 
                isPlayerAdjacentTo(gadget.getCenterX(), gadget.getCenterY())) {
                targetObject = gadget;
                minDistance = distance;
            }
        }

        // Check key if it exists
        if (key != null) {
            double keyDistance = Math.sqrt(
                Math.pow(x - key.getCenterX(), 2) + 
                Math.pow(y - key.getCenterY(), 2)
            );

            if (keyDistance < CELL_SIZE && keyDistance < minDistance && 
                isPlayerAdjacentTo(key.getCenterX(), key.getCenterY())) {
                targetObject = key;
                minDistance = keyDistance;
            }
        }

        if (targetObject != null) {
            collectingGadget = targetObject;
            collectionProgress = 0;

            progressBar.setX(collectingGadget.getCenterX() - GADGET_SIZE);
            progressBar.setY(collectingGadget.getCenterY() - GADGET_SIZE * 2);
            progressBar.setWidth(0);
            progressBar.setVisible(true);

            timeCollection = new Timeline(
                new KeyFrame(Duration.seconds(0.05), e -> {
                    collectionProgress += 0.05;
                    progressBar.setWidth((collectionProgress / COLLECTION_TIME) * (GADGET_SIZE * 2));

                    if (!isPlayerAdjacentTo(collectingGadget.getCenterX(), collectingGadget.getCenterY())) {
                        cancelGadgetCollection();
                        return;
                    }

                    if (collectionProgress >= COLLECTION_TIME) {
                        collectGadget();
                        if (collectingGadget == key) {
                            createPortal();
                        }
                    }
                })
            );
            timeCollection.setCycleCount(Timeline.INDEFINITE);
            timeCollection.play();
        }
    }
    // Checks if the player is adjacent to a specific cell.
    private boolean isPlayerAdjacentTo(double x, double y) {
        int cellX = (int)(x / CELL_SIZE);
        int cellY = (int)(y / CELL_SIZE);

        return Math.abs(playerCol - cellX) <= 1 && Math.abs(playerRow - cellY) <= 1;
    }

    // Cancels the ongoing gadget collection process.
    private void cancelGadgetCollection() {
        if (timeCollection != null) {
            timeCollection.stop();
            timeCollection = null;
        }
        collectingGadget = null;
        progressBar.setVisible(false);
    }
    
    // Creates a key at a valid GOLD empty cell position
    private void createKey() {
        if (key != null || collectedGadgets < 3) return;
        
        int[] position = findValidGOLDCell(2, 2);
        if (position == null) return;

        int row = position[0];
        int col = position[1];

        double centerX = (col + 0.5) * CELL_SIZE;
        double centerY = (row + 0.5) * CELL_SIZE;

        key = new Circle(centerX, centerY, GADGET_SIZE / 2);
        key.setFill(Color.TRANSPARENT);

        keyLabel = new Text("🗝️");
        keyLabel.setFill(Color.BLACK);
        keyLabel.setFont(Font.font(15));

        keyLabel.setX(centerX - (keyLabel.getLayoutBounds().getWidth() / 2));
        keyLabel.setY(centerY + (keyLabel.getLayoutBounds().getHeight() / 4));

        root.getChildren().addAll(key, keyLabel);
    }

    // Validates if the move to the specified cell is within bounds and is a GOLD cell.
    private boolean isValidMove(int row, int col) {
        if (row < 0 || row >= MAZE_SIZE || col < 0 || col >= MAZE_SIZE) {
            return false;
        }
        
        if (maze[row][col].getFill() != Color.GOLD) {
            return false;
        }
        
        return true;
    }
    
    // Gets rid of trapped spaces
    private void ensureNoIsolatedCells() {
        for (int row = 1; row < MAZE_SIZE-1; row++) {
            for (int col = 1; col < MAZE_SIZE-1; col++) {
                if (maze[row][col].getFill() == Color.GOLD) {
                    boolean top = maze[row-1][col].getFill() == Color.BLACK;
                    boolean bottom = maze[row+1][col].getFill() == Color.BLACK;
                    boolean left = maze[row][col-1].getFill() == Color.BLACK;
                    boolean right = maze[row][col+1].getFill() == Color.BLACK;

                    if (top && bottom && left && right) {
                        // If trapped then remove a random wall
                        int wall = random.nextInt(4);
                        switch (wall) {
                            case 0: maze[row-1][col].setFill(Color.GOLD); break;
                            case 1: maze[row+1][col].setFill(Color.GOLD); break;
                            case 2: maze[row][col-1].setFill(Color.GOLD); break;
                            case 3: maze[row][col+1].setFill(Color.GOLD); break;
                        }
                    }
                }
            }
        }
    }

    // Check if a cell has GOLD neighbors on all sides
    private boolean hasGOLDNeighbors(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newRow = row + i;
                int newCol = col + j;
                
                if (newRow >= 0 && newRow < MAZE_SIZE && 
                    newCol >= 0 && newCol < MAZE_SIZE && 
                    !isGOLDCell(newRow, newCol)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Create and place the player
    private void createPlayer() {
        do {
            playerRow = random.nextInt(MAZE_SIZE);
            playerCol = random.nextInt(MAZE_SIZE);
        } while (!isGOLDCell(playerRow, playerCol) || 
                 !hasGOLDNeighbors(playerRow, playerCol) || 
                 !isCellEmpty(playerRow, playerCol));

        player = new Circle((playerCol + 0.5) * CELL_SIZE, (playerRow + 0.5) * CELL_SIZE, PLAYER_SIZE / 2);
        player.setFill(Color.TRANSPARENT);

        Image playerGif = new Image(getClass().getResourceAsStream("/assets/player.png"));
        ImageView playerView = new ImageView(playerGif);
        playerView.setFitWidth(PLAYER_SIZE);
        playerView.setFitHeight(PLAYER_SIZE);

        playerView.xProperty().bind(player.centerXProperty().subtract(PLAYER_SIZE / 2));
        playerView.yProperty().bind(player.centerYProperty().subtract(PLAYER_SIZE / 2));

        root.getChildren().addAll(player, playerView);
    }

 // Create and place the monster (similar to player creation)
    private void createMonster() {
        do {
            monsterRow = random.nextInt(MAZE_SIZE);
            monsterCol = random.nextInt(MAZE_SIZE);
        } while (!isGOLDCell(monsterRow, monsterCol) || 
                 !hasGOLDNeighbors(monsterRow, monsterCol) ||
                 getMazeDistance(monsterRow, monsterCol, playerRow, playerCol) < 4 ||
                 !isCellEmpty(monsterRow, monsterCol));
        
        monster = new Circle((monsterCol + 0.5) * CELL_SIZE, (monsterRow + 0.5) * CELL_SIZE, MONSTER_SIZE / 2);
        monster.setFill(Color.TRANSPARENT);

        Image monsterGif = new Image(getClass().getResourceAsStream("/assets/lvl2 - monstr.png"));
        ImageView monsterView = new ImageView(monsterGif);
        monsterView.setFitWidth(MONSTER_SIZE);
        monsterView.setFitHeight(MONSTER_SIZE);

        monsterView.xProperty().bind(monster.centerXProperty().subtract(MONSTER_SIZE / 2));
        monsterView.yProperty().bind(monster.centerYProperty().subtract(MONSTER_SIZE / 2));

        root.getChildren().addAll(monster, monsterView);
    }
    
    // Checks if a cell is GOLD
    private boolean isGOLDCell(int row, int col) {
        return maze[row][col].getFill() == Color.GOLD;
    }

    // Calculates the distance between two cells.
    private int getMazeDistance(int row1, int col1, int row2, int col2) {
        return Math.abs(row1 - row2) + Math.abs(col1 - col2);
    }

    // Finds a random valid GOLD cell in the maze that is a specified distance from both the player and the monster.
    private int[] findValidGOLDCell(int minDistanceFromPlayer, int minDistanceFromMonster) {
        ArrayList<int[]> validCells = new ArrayList<>();
        for (int row = 0; row < MAZE_SIZE; row++) {
            for (int col = 0; col < MAZE_SIZE; col++) {
                if (isGOLDCell(row, col) &&
                    getMazeDistance(row, col, playerRow, playerCol) >= minDistanceFromPlayer &&
                    getMazeDistance(row, col, monsterRow, monsterCol) >= minDistanceFromMonster) {
                    validCells.add(new int[]{row, col});
                }
            }
        }
        if (validCells.isEmpty()) return null;
        return validCells.get(random.nextInt(validCells.size()));
    }

    // Creates a portal in the maze at a valid GOLD cell
    private void createPortal() {
        if (portal != null) {
            portal.setFill(collectedGadgets >= 3 ? Color.PURPLE : Color.TRANSPARENT);
            return;
        }
        
        int[] position = findValidGOLDCell(3, 3);
        if (position == null) {
            do {
                position = new int[]{random.nextInt(MAZE_SIZE), random.nextInt(MAZE_SIZE)};
            } while (!isGOLDCell(position[0], position[1]));
        }
        
        int row = position[0];
        int col = position[1];
        
        portal = new Circle((col + 0.5) * CELL_SIZE, (row + 0.5) * CELL_SIZE, GADGET_SIZE / 2);
        portal.setFill(collectedGadgets >= 3 ? Color.PURPLE : Color.TRANSPARENT);
        root.getChildren().add(portal);
    }

    // Move the player based on input
    private void movePlayer(String direction) {
        int newRow = playerRow;
        int newCol = playerCol;

        switch (direction) {
            case "UP":    newRow--; break;
            case "DOWN":  newRow++; break;
            case "LEFT":  newCol--; break;
            case "RIGHT": newCol++; break;
            default: return;
        }

        if (isValidMove(newRow, newCol)) {
            playerRow = newRow;
            playerCol = newCol;
            player.setCenterX((playerCol + 0.5) * CELL_SIZE);
            player.setCenterY((playerRow + 0.5) * CELL_SIZE);
            collectGadget();
        }
    }

    // Move the monster
    private void moveMonster() {
        int dx = Integer.compare(playerCol, monsterCol);
        int dy = Integer.compare(playerRow, monsterRow);

        // 50% chance to move horizontally or vertically towards the player
        if (random.nextBoolean()) {
            if (isValidMove(monsterRow + dy, monsterCol)) {
                monsterRow += dy;
            }
        } else {
            if (isValidMove(monsterRow, monsterCol + dx)) {
                monsterCol += dx;
            }
        }

        monster.setCenterX((monsterCol + 0.5) * CELL_SIZE);
        monster.setCenterY((monsterRow + 0.5) * CELL_SIZE);
    }

    // Main game loop (Games are dynamic and need to constantly update)
    private void startGameLoop(Stage primaryStage) {
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 400_000_000) {
                    moveMonster();
                    if (checkGameOver(primaryStage)) {
                        this.stop();
                    }
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }

    // Check if the game is over (player caught)
    private boolean checkGameOver(Stage primaryStage) {
    	if (player.getBoundsInParent().intersects(monster.getBoundsInParent())) {
            updateFeedback("Game Over! The monster caught you!", 2, "");
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> mainMenu(primaryStage));
            pause.play();
            return true;
        }
        return false;
    }
    
    // Initiates portal activation once the player is close enough and the activation is completed.
    private void startPortalActivation(double x, double y, Stage primaryStage) {
        if (portalTimeline != null || 
            portal == null || 
            portal.getFill() == Color.TRANSPARENT || 
            !isKeyCollected) { 
            return;
        }

        double distance = Math.sqrt(
            Math.pow(x - portal.getCenterX(), 2) + 
            Math.pow(y - portal.getCenterY(), 2)
        );

        if (distance < CELL_SIZE && isPlayerAdjacentTo(portal.getCenterX(), portal.getCenterY())) {
            portalProgressBar.setX(portal.getCenterX() - GADGET_SIZE);
            portalProgressBar.setY(portal.getCenterY() - GADGET_SIZE * 2);
            portalProgressBar.setWidth(0);
            portalProgressBar.setVisible(true);

            portalTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.05), e -> {
                    double progress = portalProgressBar.getWidth() / (GADGET_SIZE * 2);
                    progress += 0.05 / PORTAL_ACTIVATION_TIME;
                    portalProgressBar.setWidth(progress * (GADGET_SIZE * 2));

                    if (!isPlayerAdjacentTo(portal.getCenterX(), portal.getCenterY())) {
                        cancelPortalActivation();
                        return;
                    }

                    if (progress >= 1.0) {
                        activatePortal(primaryStage);
                    }
                })
            );
            portalTimeline.setCycleCount(Timeline.INDEFINITE);
            portalTimeline.play();
        }
    }

    // Highlights nearby gadgets, the key, and the portal by changing their stroke when the player is adjacent (just to style things a bit)
    private void checkProximityHighlights() {
        // Highlight gadgets
        for (Circle gadget : gadgets) {
            if (isPlayerAdjacentTo(gadget.getCenterX(), gadget.getCenterY())) {
                gadget.setStroke(Color.GOLD);
                gadget.setStrokeWidth(2);
            } else {
                gadget.setStroke(null);
            }
        }

        // Highlight key if it exists
        if (key != null) {
            if (isPlayerAdjacentTo(key.getCenterX(), key.getCenterY())) {
                key.setStroke(Color.GOLD);
                key.setStrokeWidth(2);
            } else {
                key.setStroke(null);
            }
        }

        // Only highlight the portal if it exists (after key collection)
        if (portal != null) {
            if (isPlayerAdjacentTo(portal.getCenterX(), portal.getCenterY())) {
                portal.setStroke(Color.GOLD);
                portal.setStrokeWidth(2);
            } else {
                portal.setStroke(null);
            }
        }
    }

    // Starts a continuous check for proximity highlights.
    private void startProximityCheck() {
        Timeline proximityTimeline = new Timeline(
            new KeyFrame(Duration.millis(100), e -> checkProximityHighlights())
        );
        proximityTimeline.setCycleCount(Timeline.INDEFINITE);
        proximityTimeline.play();
    }

    // Cancels portal activation and resets progress.
    private void cancelPortalActivation() {
        if (portalTimeline != null) {
            portalTimeline.stop();
            portalTimeline = null;
        }
        isPortalActivated = false;
        portalProgressBar.setVisible(false);
    }

    // Activates the portal and transitions to the next level.
    private void activatePortal(Stage primaryStage) {
        cancelPortalActivation();
        updateFeedback("Portal activated! Teleporting to next level...", 2, "");
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> nextLevel(primaryStage));
        pause.play();
    }

    // Updates feedback messages for the player.
    private void updateFeedback(String message, double delay, String originalMessage) {
        feedback.setText(message);
        if (!originalMessage.isEmpty()) {
            PauseTransition pause = new PauseTransition(Duration.seconds(delay));
            pause.setOnFinished(e -> feedback.setText(originalMessage));
            pause.play();
        }
    }

    // Return to main menu
    private void mainMenu(Stage mainStage) {
        mainStage.close();
        MainMenu menu = new MainMenu();
        menu.start(mainStage);
    }
    
    // Go to next level
    private void nextLevel(Stage mainStage) {
        mainStage.close();
        Level3 next = new Level3();
        next.start3(mainStage);
    }

    // Restart the game
    private void restart(Stage mainStage) {
        mainStage.close();
        Level2 level = new Level2();
        level.start2(mainStage);
    }
}