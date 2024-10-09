package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Iterator;

public class Level3 {
    // Game configuration
    private static final int MAZE_SIZE = 20;
    private static final int CELL_SIZE = 30;
    private static final int PLAYER_SIZE = 20;
    private static final int GADGET_SIZE = 10;
    private static final int MONSTER_SIZE = 25;
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
    private Circle gun = null;
    private boolean gunSpawned = false;
    private Text gunLabel = null;
    private boolean isgunCollected = false;
    
    // Adding new variables for laser shooting
    private ArrayList<Line> activeLasers = new ArrayList<>();
    private String lastDirection = "RIGHT";
    private static final double LASER_SPEED = 400;
    private static final int LASER_LENGTH = 20;
    private long lastShotTime = 0;
    private static final long SHOT_COOLDOWN = 250_000_000;
    private int monsterHitCount = 0;
    private static final int MONSTER_DEATH_HITS = 5;
    private boolean isMonsterAlive = true;
    
    // Player and monster positions
    private int playerRow, playerCol;
    private int monsterRow, monsterCol;
  
    private Random random = new Random();
    private int collectedGadgets = 0;

    public void start(Stage primaryStage) {
        root = new Pane();
        maze = new Rectangle[MAZE_SIZE][MAZE_SIZE];
        
        // Set up mouse event handlers
        root.setOnMousePressed(event -> {
            startGadgetCollection(event.getX(), event.getY());
        });
        
        root.setOnMouseReleased(event -> {
            cancelGadgetCollection();
        });
        
        // Progress bar for collection
        progressBar = new Rectangle(0, 0, 0, 5);
        progressBar.setFill(Color.GREEN);
        progressBar.setVisible(false);
        root.getChildren().add(progressBar);

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
        feedback.setStyle("-fx-background-color: white;");

        HBox topbarLayout = new HBox(8);
        HBox.setHgrow(feedback, javafx.scene.layout.Priority.ALWAYS);
        topbarLayout.getChildren().addAll(feedback, menu, reload);
        topbarLayout.setAlignment(Pos.CENTER);

        generateMaze();
        createPlayer();
        createMonster();
        createGadgets(3);
        startProximityCheck();
        creategun();
        startLaserUpdateLoop(primaryStage);

        VBox mainLayout = new VBox(8, topbarLayout, root);
        mainLayout.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(mainLayout, 20 * 32, 12 * 58);
        root.setFocusTraversable(true);
        scene.setOnMouseClicked(event -> root.requestFocus());
        scene.setOnKeyPressed(e -> movePlayer(e.getCode().toString()));
        
        scene.setOnKeyPressed(e -> {
            String direction = e.getCode().toString();
            if (direction.equals("UP") || direction.equals("DOWN") || 
                direction.equals("LEFT") || direction.equals("RIGHT")) {
                lastDirection = direction;
            }
            movePlayer(direction);
            
            if (direction.equals("SPACE") && isgunCollected) {
                shootLaser();
            }
        });

        primaryStage.setTitle("Maze Runner");
        primaryStage.setScene(scene);
        primaryStage.show();
        root.requestFocus();

        startGameLoop(primaryStage);
    }

    // Generate the maze layout
    private void generateMaze() {
        // Initialize maze with empty cells
        for (int row = 0; row < MAZE_SIZE; row++) {
            for (int col = 0; col < MAZE_SIZE; col++) {
                maze[row][col] = new Rectangle(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                maze[row][col].setFill(Color.WHITE);
                maze[row][col].setStroke(Color.GRAY);
                root.getChildren().add(maze[row][col]);
            }
        }

        // Randomly place black cells
        for (int row = 0; row < MAZE_SIZE; row++) {
            for (int col = 0; col < MAZE_SIZE; col++) {
                boolean canPlaceBlack = true;

                if (random.nextDouble() < 0.3) {
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

                if (canPlaceBlack && random.nextDouble() < 0.2) {
                    maze[row][col].setFill(Color.BLACK);
                }
            }
        }

        maze[0][0].setFill(Color.WHITE);
        maze[MAZE_SIZE - 1][MAZE_SIZE - 1].setFill(Color.WHITE);
        
        ensureNoIsolatedCells();
    }

    // Creates a specified number of gadgets in valid white cells.
    private void createGadgets(int count) {
        int gadgetsCreated = 0;
        while (gadgetsCreated < count) {
            int[] position = findValidWhiteCell(2, 2);
            if (position == null) break;

            int row = position[0];
            int col = position[1];

            if (isWhiteCell(row, col) && isCellEmpty(row, col)) {
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
                    gunSpawned = true;
                    creategun();
                    updateFeedback("All gadgets collected! Find and collect the gun!", 3, "Kill the monster with the gun to win!!!");
                } else {
                    String gadgetMessage = "Gadget collected! Gadgets left: " + (3 - collectedGadgets);
                    updateFeedback(gadgetMessage, 2, "Hold-click on gadgets to collect them!");
                }
            } else if (collectingGadget == gun) {
                root.getChildren().removeAll(gun, gunLabel);
                gun = null;
                gunLabel = null;
                isgunCollected = true;
                updateFeedback("Gun collected!", 3, "Press SPACE to shoot lasers! Kill the monster to win!!!");
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

        if (gun != null) {
            double gunDistance = Math.sqrt(
                Math.pow(x - gun.getCenterX(), 2) + 
                Math.pow(y - gun.getCenterY(), 2)
            );

            if (gunDistance < CELL_SIZE && gunDistance < minDistance && 
                isPlayerAdjacentTo(gun.getCenterX(), gun.getCenterY())) {
                targetObject = gun;
                minDistance = gunDistance;
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
    
    // Creates a gun at a valid white cell
    private void creategun() {
        if (gun != null || collectedGadgets < 3) return;
        
        int[] position = findValidWhiteCell(2, 2);
        if (position == null) return;

        int row = position[0];
        int col = position[1];

        double centerX = (col + 0.5) * CELL_SIZE;
        double centerY = (row + 0.5) * CELL_SIZE;

        gun = new Circle(centerX, centerY, GADGET_SIZE / 2);
        gun.setFill(Color.TRANSPARENT);

        gunLabel = new Text("🔫️");
        gunLabel.setFill(Color.BLACK);
        gunLabel.setFont(Font.font(15));

        gunLabel.setX(centerX - (gunLabel.getLayoutBounds().getWidth() / 2));
        gunLabel.setY(centerY + (gunLabel.getLayoutBounds().getHeight() / 4));

        root.getChildren().addAll(gun, gunLabel);
    }
    
    // Starts the laser update loop using an AnimationTimer to continuously update lasers.
    private void startLaserUpdateLoop(Stage primaryStage) {
        AnimationTimer laserTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateLasers(primaryStage);
            }
        };
        laserTimer.start();
    }

    // Shoots a laser in the last direction the player moved
    private void shootLaser() {
        long currentTime = System.nanoTime();
        if (currentTime - lastShotTime < SHOT_COOLDOWN) {
            return;
        }
        lastShotTime = currentTime;
        
        double startX = player.getCenterX();
        double startY = player.getCenterY();
        
        Line laser = new Line(startX, startY, startX, startY);
        laser.setStroke(Color.GREEN);
        laser.setStrokeWidth(2);
        
        double directionX = 0;
        double directionY = 0;
        
        switch (lastDirection) {
            case "UP":    directionY = -1; break;
            case "DOWN":  directionY = 1; break;
            case "LEFT":  directionX = -1; break;
            case "RIGHT": directionX = 1; break;
        }
        
        laser.setUserData(new double[]{directionX, directionY});
        
        laser.setEndX(startX + (directionX * LASER_LENGTH));
        laser.setEndY(startY + (directionY * LASER_LENGTH));
        
        activeLasers.add(laser);
        root.getChildren().add(laser);
    }
    
    // Updates the position of active lasers and handles collisions with the monster.
    private void updateLasers(Stage primaryStage) {
        Iterator<Line> iterator = activeLasers.iterator();
        while (iterator.hasNext()) {
            Line laser = iterator.next();
            double[] direction = (double[]) laser.getUserData();
            
            laser.setStartX(laser.getStartX() + direction[0] * 5);
            laser.setStartY(laser.getStartY() + direction[1] * 5);
            laser.setEndX(laser.getEndX() + direction[0] * 5);
            laser.setEndY(laser.getEndY() + direction[1] * 5);
            
            // Check if laser is out of bounds
            if (isLaserOutOfBounds(laser)) {
                root.getChildren().remove(laser);
                iterator.remove();
                continue;
            }
            
            if (isLaserHittingMonster(laser)) {
                handleMonsterHit(primaryStage);
                root.getChildren().remove(laser);
                iterator.remove();
            }
        }
    }
    
    // Checks if the laser is out of bounds based on its starting coordinates.
    private boolean isLaserOutOfBounds(Line laser) {
        return laser.getStartX() < 0 || laser.getStartX() > MAZE_SIZE * CELL_SIZE ||
               laser.getStartY() < 0 || laser.getStartY() > MAZE_SIZE * CELL_SIZE;
    }
    
    // Determines if the laser intersects with the monster's bounds.
    private boolean isLaserHittingMonster(Line laser) {
        return monster.getBoundsInParent().intersects(laser.getBoundsInParent());
    }
    
    // Handles the logic when the monster is hit
    private void handleMonsterHit(Stage primaryStage) {
        if (!isMonsterAlive) return;
        
        monsterHitCount++;
        
        if (monsterHitCount >= MONSTER_DEATH_HITS) {
            // Monster dies
            root.getChildren().remove(monster);
            isMonsterAlive = false;
            updateFeedback("You defeated the monster!", 1, "YOU WIN!!!");
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> youWin(primaryStage));
            pause.play();
            return;
        }

        monster.setFill(Color.ORANGE);
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            if (isMonsterAlive) {
                monster.setFill(Color.RED);
            }
        });
        pause.play();
        
        updateFeedback("Monster hit! " + (MONSTER_DEATH_HITS - monsterHitCount) + " hits remaining!", 1, "");
        
        double dx = monster.getCenterX() - player.getCenterX();
        double dy = monster.getCenterY() - player.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            double pushX = (dx / distance) * CELL_SIZE;
            double pushY = (dy / distance) * CELL_SIZE;
            
            int newMonsterRow = monsterRow + (int) Math.round(pushY / CELL_SIZE);
            int newMonsterCol = monsterCol + (int) Math.round(pushX / CELL_SIZE);
            
            if (isValidMove(newMonsterRow, newMonsterCol)) {
                monsterRow = newMonsterRow;
                monsterCol = newMonsterCol;
                monster.setCenterX((monsterCol + 0.5) * CELL_SIZE);
                monster.setCenterY((monsterRow + 0.5) * CELL_SIZE);
            }
        }
    }
    
    // Validates if the move to the specified cell is within bounds and is a white cell.
    private boolean isValidMove(int row, int col) {
        if (row < 0 || row >= MAZE_SIZE || col < 0 || col >= MAZE_SIZE) {
            return false;
        }
        
        if (maze[row][col].getFill() != Color.WHITE) {
            return false;
        }
        
        return true;
    }

    // Gets rid of trapped spaces
    private void ensureNoIsolatedCells() {
        for (int row = 1; row < MAZE_SIZE-1; row++) {
            for (int col = 1; col < MAZE_SIZE-1; col++) {
                if (maze[row][col].getFill() == Color.WHITE) {
                    boolean top = maze[row-1][col].getFill() == Color.BLACK;
                    boolean bottom = maze[row+1][col].getFill() == Color.BLACK;
                    boolean left = maze[row][col-1].getFill() == Color.BLACK;
                    boolean right = maze[row][col+1].getFill() == Color.BLACK;

                    if (top && bottom && left && right) {
                        // If trapped then remove a random wall
                        int wall = random.nextInt(4);
                        switch (wall) {
                            case 0: maze[row-1][col].setFill(Color.WHITE); break;
                            case 1: maze[row+1][col].setFill(Color.WHITE); break;
                            case 2: maze[row][col-1].setFill(Color.WHITE); break;
                            case 3: maze[row][col+1].setFill(Color.WHITE); break;
                        }
                    }
                }
            }
        }
    }

    // Check if a cell has white neighbors on all sides
    private boolean hasWhiteNeighbors(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newRow = row + i;
                int newCol = col + j;
                
                if (newRow >= 0 && newRow < MAZE_SIZE && 
                    newCol >= 0 && newCol < MAZE_SIZE && 
                    !isWhiteCell(newRow, newCol)) {
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
        } while (!isWhiteCell(playerRow, playerCol) || !hasWhiteNeighbors(playerRow, playerCol) || !isCellEmpty(playerRow, playerCol));
        
        player = new Circle((playerCol + 0.5) * CELL_SIZE, (playerRow + 0.5) * CELL_SIZE, PLAYER_SIZE / 2, Color.BLUE);
        root.getChildren().add(player);
    }

    // Create and place the monster (similar to player creation)
    private void createMonster() {
        do {
            monsterRow = random.nextInt(MAZE_SIZE);
            monsterCol = random.nextInt(MAZE_SIZE);
        } while (!isWhiteCell(monsterRow, monsterCol) || 
                 !hasWhiteNeighbors(monsterRow, monsterCol) ||
                 getMazeDistance(monsterRow, monsterCol, playerRow, playerCol) < 4 ||
                 !isCellEmpty(monsterRow, monsterCol));
        
        monster = new Circle((monsterCol + 0.5) * CELL_SIZE, (monsterRow + 0.5) * CELL_SIZE, MONSTER_SIZE / 2, Color.RED);
        root.getChildren().add(monster);
    }
    
    // Checks if a cell is white
    private boolean isWhiteCell(int row, int col) {
        return maze[row][col].getFill() == Color.WHITE;
    }
    
    // Calculates the distance between two cells.
    private int getMazeDistance(int row1, int col1, int row2, int col2) {
        return Math.abs(row1 - row2) + Math.abs(col1 - col2);
    }
    
    // Finds a random valid white cell in the maze that is a specified distance from both the player and the monster.
    private int[] findValidWhiteCell(int minDistanceFromPlayer, int minDistanceFromMonster) {
        ArrayList<int[]> validCells = new ArrayList<>();
        for (int row = 0; row < MAZE_SIZE; row++) {
            for (int col = 0; col < MAZE_SIZE; col++) {
                if (isWhiteCell(row, col) &&
                    getMazeDistance(row, col, playerRow, playerCol) >= minDistanceFromPlayer &&
                    getMazeDistance(row, col, monsterRow, monsterCol) >= minDistanceFromMonster) {
                    validCells.add(new int[]{row, col});
                }
            }
        }
        if (validCells.isEmpty()) return null;
        return validCells.get(random.nextInt(validCells.size()));
    }

    // Move the player based on input
    private void movePlayer(String direction) {
        int newRow = playerRow;
        int newCol = playerCol;

        switch (direction) {
            case "UP":    
                newRow--; 
                lastDirection = "UP";
                break;
            case "DOWN":  
                newRow++; 
                lastDirection = "DOWN";
                break;
            case "LEFT":  
                newCol--; 
                lastDirection = "LEFT";
                break;
            case "RIGHT": 
                newCol++; 
                lastDirection = "RIGHT";
                break;
            case "SPACE":
                if (isgunCollected) {
                    shootLaser();
                }
                return;
            default: 
                return;
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
                if (now - lastUpdate >= 300_000_000) {
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

    // Update the checkGameOver method
    private boolean checkGameOver(Stage primaryStage) {
        if (!isMonsterAlive) return false;
        
        if (player.getBoundsInParent().intersects(monster.getBoundsInParent())) {
            updateFeedback("Game Over! The monster caught you!", 2, "");
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> mainMenu(primaryStage));
            pause.play();
            return true;
        }
        return false;
    }

    // Highlights nearby gadgets, the key, and the portal by changing their stroke when the player is adjacent (just to style things a bit)
    private void checkProximityHighlights() {
        // Highlight gadgets
        for (Circle gadget : gadgets) {
            if (isPlayerAdjacentTo(gadget.getCenterX(), gadget.getCenterY())) {
                gadget.setStroke(Color.WHITE);
                gadget.setStrokeWidth(2);
            } else {
                gadget.setStroke(null);
            }
        }

        // Highlight gun if it exists
        if (gun != null) {
            if (isPlayerAdjacentTo(gun.getCenterX(), gun.getCenterY())) {
                gun.setStroke(Color.WHITE);
                gun.setStrokeWidth(2);
            } else {
                gun.setStroke(null);
            }
        }

        // Only highlight the portal if it exists (after gun collection)
        if (portal != null) {
            if (isPlayerAdjacentTo(portal.getCenterX(), portal.getCenterY())) {
                portal.setStroke(Color.WHITE);
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
    
    // Go to Win Screen
    private void youWin(Stage mainStage) {
        mainStage.close();
        YouWon win = new YouWon();
        win.display(mainStage);
    }

    // Restart the game
    private void restart(Stage mainStage) {
        mainStage.close();
        Level2 level = new Level2();
        level.start(mainStage);
    }
}