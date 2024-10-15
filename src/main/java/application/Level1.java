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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Level1 {
    // Game configuration
    private static final int MAZE_SIZE = 18;
    private static final int CELL_SIZE = 40;
    private static final int PLAYER_SIZE = 40;
    private static final int GADGET_SIZE = 25;
    private static final int PORTAL_SIZE = 40;
    private static final int MONSTER_SIZE = 40;
    private static final double DRAG_THRESHOLD = 20.0;  // Adjusts the dragging of brown cells
    private static final double COLLECTION_TIME = 0.2;
    private static final double PORTAL_ACTIVATION_TIME = 0.2;

    // Core game components
    private Pane root;
    private Rectangle[][] maze;
    private Circle player;
    private Circle monster;
    private Circle portal;
    private ArrayList<Circle> gadgets = new ArrayList<>();
    private ArrayList<Rectangle> brownC = new ArrayList<>();
    private Label feedback;
    private Timeline timeCollection;
    private Circle collectingGadget;
    private double collectionProgress;
    private Rectangle progressBar;
    
    // Player and monster positions
    private int playerRow, playerCol;
    private int monsterRow, monsterCol;
    
    private boolean isPortalActivated = false;
    private Timeline portalTimeline;
    private Rectangle portalProgressBar;
    
    private Random random = new Random();
    private int collectedGadgets = 0;
    private boolean portalAdded = false;

    public void start1(Stage primaryStage) {
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
        
        // Progress bar for collection (hidden initially)
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
        menu.setStyle("-fx-border-color: BLACK; -fx-background-color: WHITE; -fx-border-width: 3px;");
        
        Button reload = new Button("↻");
        reload.setOnAction(event -> restart(primaryStage));
        reload.setFocusTraversable(false);
        reload.setPadding(new Insets(15, 20, 15, 20));
        reload.setStyle("-fx-border-color: BLACK; -fx-background-color: WHITE; -fx-border-width: 3px;");

        feedback = new Label("Collect 3 coins to win! Watch out for the monster!");
        feedback.setMaxWidth(Double.MAX_VALUE);
        feedback.setPadding(new Insets(15, 25, 15, 25));
        feedback.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-width: 3px; -fx-font-size: 15px;");

        HBox topbarLayout = new HBox(8);
        HBox.setHgrow(feedback, javafx.scene.layout.Priority.ALWAYS);
        topbarLayout.getChildren().addAll(feedback, menu, reload);
        topbarLayout.setAlignment(Pos.CENTER);

        generateMaze();
        createPlayer();
        createMonster();
        createGadgets(3);
        addbrownC(3);
        startProximityCheck();

        VBox mainLayout = new VBox(8, topbarLayout, root);
        mainLayout.setPadding(new Insets(35, 50, 35, 50));
        
        // Set the background color of the root pane
        mainLayout.setStyle("-fx-background-color: LIGHTYELLOW;");

        Scene scene = new Scene(mainLayout, 20 * 41, 12 * 70);
        root.setFocusTraversable(true);
        scene.setOnMouseClicked(event -> root.requestFocus());
        scene.setOnKeyPressed(e -> movePlayer(e.getCode().toString()));
        
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Maze Runner");
        primaryStage.show();
        
        // Request focus for the root pane
        root.requestFocus();

        startGameLoop(primaryStage);
    }

    // Generate the maze layout
    private void generateMaze() {
        for (int row = 0; row < MAZE_SIZE; row++) {
            for (int col = 0; col < MAZE_SIZE; col++) {
                maze[row][col] = new Rectangle(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                maze[row][col].setFill(Color.GOLD);
                maze[row][col].setStroke(Color.BROWN);
                root.getChildren().add(maze[row][col]);
            }
        }
        
        // Create a border for the entire maze
        Rectangle mazeBorder = new Rectangle(-2, -1, (MAZE_SIZE * CELL_SIZE) + 4, (MAZE_SIZE * CELL_SIZE) + 4);
        mazeBorder.setFill(Color.TRANSPARENT);
        mazeBorder.setStroke(Color.web("#753A34"));
        mazeBorder.setStrokeWidth(3);
        root.getChildren().add(mazeBorder);

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
    
    // Load the image for the gadgets
    Image gadgetImage = new Image(getClass().getResourceAsStream("/assets/coins1.png"));

    // Creates a specified number of gadgets in valid GOLD cells.
    private void createGadgets(int count) {
        int gadgetsCreated = 0;
        while (gadgetsCreated < count) {
            int[] position = findValidGOLDCell(2, 2);
            if (position == null) break;

            int row = position[0];
            int col = position[1];

            if (isGOLDCell(row, col) && isCellEmpty(row, col)) {
                Circle gadget = new Circle((col + 0.5) * CELL_SIZE, (row + 0.5) * CELL_SIZE, GADGET_SIZE / 2);
                gadget.setFill(Color.TRANSPARENT);
                gadgets.add(gadget);
                
                ImageView gadgetView = new ImageView(gadgetImage);
                gadgetView.setFitWidth(GADGET_SIZE);
                gadgetView.setFitHeight(GADGET_SIZE);

                gadgetView.setX(col * CELL_SIZE + (CELL_SIZE - GADGET_SIZE) / 2);
                gadgetView.setY(row * CELL_SIZE + (CELL_SIZE - GADGET_SIZE) / 2);
                
                gadgetView.setOnMouseClicked(e -> {

                    root.getChildren().remove(gadgetView);
                    gadgets.remove(gadget);

                });
                
                root.getChildren().addAll(gadget, gadgetView);
                gadgetsCreated++;
            }
        }
    }

    // Checks if a specific cell is empty (not occupied by gadgets or brown cells).
    private boolean isCellEmpty(int row, int col) {
        for (Circle gadget : gadgets) {
            int gadgetRow = (int)(gadget.getCenterY() / CELL_SIZE);
            int gadgetCol = (int)(gadget.getCenterX() / CELL_SIZE);
            if (row == gadgetRow && col == gadgetCol) {
                return false;
            }
        }

        for (Rectangle brownCell : brownC) {
            int brownRow = (int)(brownCell.getY() / CELL_SIZE);
            int brownCol = (int)(brownCell.getX() / CELL_SIZE);
            if (row == brownRow && col == brownCol) {
                return false;
            }
        }

        return true; // Cell is empty
    }

    // Starts the collection process for the nearest gadget to the player.
    private void startGadgetCollection(double x, double y) {
        if (timeCollection != null) return;

        Circle closestGadget = null;
        double minDistance = Double.MAX_VALUE;

        for (Circle gadget : gadgets) {
            double distance = Math.sqrt(
                Math.pow(x - gadget.getCenterX(), 2) + 
                Math.pow(y - gadget.getCenterY(), 2)
            );

            if (distance < CELL_SIZE && distance < minDistance && 
                isPlayerAdjacentTo(gadget.getCenterX(), gadget.getCenterY())) {
                closestGadget = gadget;
                minDistance = distance;
            }
        }

        if (closestGadget != null) {
            collectingGadget = closestGadget;
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

    // Collects the gadget and updates the game state accordingly.
    private void collectGadget() {
        if (collectingGadget != null && gadgets.contains(collectingGadget)) {
            root.getChildren().remove(collectingGadget);
            gadgets.remove(collectingGadget);
            collectedGadgets++;

            String gadgetMessage = "Gadget collected! Gadgets left: " + (3 - collectedGadgets);
            updateFeedback(gadgetMessage, 2, "Hold-click on gadgets to collect them! Watch out for the monster!");

            // Create portal if all gadgets are collected
            if (collectedGadgets == 3 && !portalAdded) {
                createPortal();
                updateFeedback("All 3 gadgets collected!", 2, "Enter the portal to teleport to the next level!");
            }
        }
        cancelGadgetCollection();
    }

    // Moves a brown cell to a new position if the move is valid.
    private void moveBrownCell(Rectangle brownCell, int rowOffset, int colOffset) {
        int row = (int)(brownCell.getY() / CELL_SIZE);
        int col = (int)(brownCell.getX() / CELL_SIZE);

        int newRow = row + rowOffset;
        int newCol = col + colOffset;

        if (isValidMove(newRow, newCol)) {
            brownCell.setX(newCol * CELL_SIZE);
            brownCell.setY(newRow * CELL_SIZE);
        }
    }

    // Adds a specified number of brown cells to the game.
    private void addbrownC(int count) {
        int added = 0;
        while (added < count) {
            int row = random.nextInt(MAZE_SIZE);
            int col = random.nextInt(MAZE_SIZE);

            if (maze[row][col].getFill() == Color.GOLD && 
                !(row == 0 && col == 0) && 
                !(row == MAZE_SIZE - 1 && col == MAZE_SIZE - 1)) {

                Rectangle brownCell = new Rectangle(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                brownCell.setFill(Color.BROWN);
                root.getChildren().add(brownCell);
                brownC.add(brownCell);
                added++;

                addDragFunctionality(brownCell);
            }
        }
    }

    // Adds drag-and-drop functionality to a brown cell.
    private void addDragFunctionality(Rectangle brownCell) {
        brownCell.setOnMousePressed(event -> {
            brownCell.setUserData(new double[]{
                event.getSceneX(), 
                event.getSceneY(),
                brownCell.getX(),
                brownCell.getY()
            });
        });

        brownCell.setOnMouseDragged(event -> {
            double[] initialData = (double[]) brownCell.getUserData();
            double initialX = initialData[0];
            double initialY = initialData[1];

            double deltaX = event.getSceneX() - initialX;
            double deltaY = event.getSceneY() - initialY;

            if (Math.abs(deltaX) > DRAG_THRESHOLD || Math.abs(deltaY) > DRAG_THRESHOLD) {
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 0) {
                        moveBrownCell(brownCell, 0, 1);
                    } else {
                        moveBrownCell(brownCell, 0, -1);
                    }
                } else {
                    if (deltaY > 0) {
                        moveBrownCell(brownCell, 1, 0);
                    } else {
                        moveBrownCell(brownCell, -1, 0);
                    }
                }

                brownCell.setUserData(new double[]{
                    event.getSceneX(), 
                    event.getSceneY(),
                    brownCell.getX(),
                    brownCell.getY()
                });
            }
        });
    }

    // This method to check for brown cells
    private boolean isValidMove(int row, int col) {
        if (row < 0 || row >= MAZE_SIZE || col < 0 || col >= MAZE_SIZE) {
            return false;
        }
        
        if (maze[row][col].getFill() != Color.GOLD) {
            return false;
        }
        
        for (Rectangle brownCell : brownC) {
            if (brownCell.getX() / CELL_SIZE == col && brownCell.getY() / CELL_SIZE == row) {
                return false;
            }
        }
        
        return true;
    }


    // Make sure the maze can be solved
    private void ensureValidPath() {
        boolean[][] visited = new boolean[MAZE_SIZE][MAZE_SIZE];
        createPathDFS(0, 0, visited);
    }

    private boolean createPathDFS(int row, int col, boolean[][] visited) {
        if (row == MAZE_SIZE-1 && col == MAZE_SIZE-1) return true;

        visited[row][col] = true;
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        Collections.shuffle(Arrays.asList(directions));

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isValidCell(newRow, newCol, visited)) {
                maze[newRow][newCol].setFill(Color.GOLD);
                if (createPathDFS(newRow, newCol, visited)) {
                    return true;
                }
            }
        }
        return false;
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
    
    // Helper method to check if a cell is valid and unvisited
    private boolean isValidCell(int row, int col, boolean[][] visited) {
        return row >= 0 && row < MAZE_SIZE && 
               col >= 0 && col < MAZE_SIZE &&
               maze[row][col].getFill() == Color.GOLD &&
               !visited[row][col];
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

        Image monsterGif = new Image(getClass().getResourceAsStream("/assets/lvl1 - monstr.png"));
        ImageView monsterView = new ImageView(monsterGif);
        monsterView.setFitWidth(MONSTER_SIZE);
        monsterView.setFitHeight(MONSTER_SIZE);

        monsterView.xProperty().bind(monster.centerXProperty().subtract(MONSTER_SIZE / 2));
        monsterView.yProperty().bind(monster.centerYProperty().subtract(MONSTER_SIZE / 2));

        root.getChildren().addAll(monster, monsterView);
    }
    
    private boolean isGOLDCell(int row, int col) {
        return maze[row][col].getFill() == Color.GOLD;
    }

    private int getMazeDistance(int row1, int col1, int row2, int col2) {
        return Math.abs(row1 - row2) + Math.abs(col1 - col2);
    }

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

    // Create and place the portal (appears after collecting all gadgets)
    private void createPortal() {
        int[] position = findValidGOLDCell(3, 3);
        if (position == null) {
            do {
                position = new int[]{random.nextInt(MAZE_SIZE), random.nextInt(MAZE_SIZE)};
            } while (!isGOLDCell(position[0], position[1]));
        }

        int row = position[0];
        int col = position[1];

        portal = new Circle((col + 0.5) * CELL_SIZE, (row + 0.5) * CELL_SIZE, PORTAL_SIZE / 2);
        portal.setFill(Color.TRANSPARENT);

        Image portalImage = new Image(getClass().getResourceAsStream("/assets/Lvl1.1.png"));
        ImageView portalView = new ImageView(portalImage);
        portalView.setFitWidth(PORTAL_SIZE);
        portalView.setFitHeight(PORTAL_SIZE);

        portalView.xProperty().bind(portal.centerXProperty().subtract(PORTAL_SIZE / 2));
        portalView.yProperty().bind(portal.centerYProperty().subtract(PORTAL_SIZE / 2));

        root.getChildren().addAll(portal, portalView);

        portalAdded = true;
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

    // Main game loop (Games are dynamic and need to constantly update)
    private void startGameLoop(Stage primaryStage) {
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 500_000_000) {
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

    // Check if the game is over (player caught or reached portal)
    private boolean checkGameOver(Stage primaryStage) {
        if (playerRow == monsterRow && playerCol == monsterCol) {
            updateFeedback("Game Over! The monster caught you!", 2, "");
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> mainMenu(primaryStage));
            pause.play();
            return true;
        }
        return false;
    }

    // Update the moveMonster method to allow the monster to occupy the same cell as the player
    private void moveMonster() {
        int dx = Integer.compare(playerCol, monsterCol);
        int dy = Integer.compare(playerRow, monsterRow);

        // 50% chance to move horizontally or vertically towards the player
        if (random.nextBoolean()) {
            if (isValidMoveForMonster(monsterRow + dy, monsterCol)) {
                monsterRow += dy;
            }
        } else {
            if (isValidMoveForMonster(monsterRow, monsterCol + dx)) {
                monsterCol += dx;
            }
        }

        monster.setCenterX((monsterCol + 0.5) * CELL_SIZE);
        monster.setCenterY((monsterRow + 0.5) * CELL_SIZE);
    }

    // New method to check if a move is valid for the monster
    private boolean isValidMoveForMonster(int row, int col) {
        if (row < 0 || row >= MAZE_SIZE || col < 0 || col >= MAZE_SIZE) {
            return false;
        }
        
        if (maze[row][col].getFill() != Color.GOLD) {
            return false;
        }
        
        for (Rectangle brownCell : brownC) {
            if (brownCell.getX() / CELL_SIZE == col && brownCell.getY() / CELL_SIZE == row) {
                return false;
            }
        }
        
        return true;
    }
    
    // Initiates portal activation when the player is close enough.
    private void startPortalActivation(double x, double y, Stage primaryStage) {
        if (portalTimeline != null || !portalAdded) return;

        double distance = Math.sqrt(
            Math.pow(x - portal.getCenterX(), 2) + 
            Math.pow(y - portal.getCenterY(), 2)
        );

        // Check if the player is close to the portal
        if (distance < CELL_SIZE && isPlayerAdjacentTo(portal.getCenterX(), portal.getCenterY())) {
            isPortalActivated = true;
            
            portalProgressBar.setX(portal.getCenterX() - PORTAL_SIZE);
            portalProgressBar.setY(portal.getCenterY() - PORTAL_SIZE * 2);
            portalProgressBar.setWidth(0);
            portalProgressBar.setVisible(true);

            portalTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.05), e -> {
                    double progress = portalProgressBar.getWidth() / (PORTAL_SIZE * 2);
                    progress += 0.05 / PORTAL_ACTIVATION_TIME;
                    portalProgressBar.setWidth(progress * (PORTAL_SIZE * 2));

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

    // Highlights gadgets and the portal if the player is adjacent.
    private void checkProximityHighlights() {
        for (Circle gadget : gadgets) {
            if (isPlayerAdjacentTo(gadget.getCenterX(), gadget.getCenterY())) {
                gadget.setStroke(Color.GOLD);
                gadget.setStrokeWidth(2);
            } else {
                gadget.setStroke(null);
            }
        }

        if (portalAdded) {
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
        Level2 level2 = new Level2();
        level2.start2(mainStage);
    }

    // Restart the game
    private void restart(Stage mainStage) {
        mainStage.close();
        Level1 level = new Level1();
        level.start1(mainStage);
    }
}