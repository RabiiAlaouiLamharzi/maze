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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Level1 {
    // Game settings - might need tweaking later
    private static final int MAZE_SIZE = 20;
    private static final int CELL_SIZE = 30;
    private static final int PLAYER_SIZE = 20;
    private static final int GADGET_SIZE = 10;
    private static final int MONSTER_SIZE = 25;  // bigger = harder
    private static final double DRAG_THRESHOLD = 15.0;  // controls the sensitivity of the dragging

    // Game elements
    private Pane root;
    private Rectangle[][] maze;
    private Circle player;
    private Circle monster;
    private Circle portal;
    private ArrayList<Circle> gadgets = new ArrayList<>();
    private ArrayList<Rectangle> brownCells = new ArrayList<>();
    private Label feedback;
    
    // Positions
    private int playerRow, playerCol;
    private int monsterRow, monsterCol;
    
    private Random random = new Random();
    private int collectedGadgets = 0;
    private boolean portalAdded = false;

    // TODO: Add difficulty levels?
    public void start(Stage primaryStage) {
        root = new Pane();
        maze = new Rectangle[MAZE_SIZE][MAZE_SIZE];

        // UI stuff
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
        addBrownCells(3);

        VBox mainLayout = new VBox(8, topbarLayout, root);
        mainLayout.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(mainLayout, 20 * 32, 12 * 58);
        root.setFocusTraversable(true);
        scene.setOnMouseClicked(event -> root.requestFocus());
        scene.setOnKeyPressed(e -> movePlayer(e.getCode().toString()));

        primaryStage.setTitle("Maze Runner");
        primaryStage.setScene(scene);
        primaryStage.show();
        root.requestFocus();

        startGameLoop(primaryStage);
    }

    // Creates a random maze
    private void generateMaze() {
        for (int row = 0; row < MAZE_SIZE; row++) {
            for (int col = 0; col < MAZE_SIZE; col++) {
                Rectangle cell = new Rectangle(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                if (random.nextDouble() < 0.3) {
                    cell.setFill(Color.BLACK);
                } else {
                    cell.setFill(Color.WHITE);
                }
                cell.setStroke(Color.GRAY);
                maze[row][col] = cell;
                root.getChildren().add(cell);
            }
        }

        maze[0][0].setFill(Color.WHITE);
        maze[MAZE_SIZE-1][MAZE_SIZE-1].setFill(Color.WHITE);
        ensureValidPath();
        ensureNoIsolatedCells();
        addBrownCells(3);
    }

    // Moves a brown cell
    private void moveBrownCell(Rectangle brownCell, int rowOffset, int colOffset) {
        int row = (int) (brownCell.getY() / CELL_SIZE);
        int col = (int) (brownCell.getX() / CELL_SIZE);

        int newRow = row + rowOffset;
        int newCol = col + colOffset;

        if (isValidMove(newRow, newCol)) {
            brownCell.setX(newCol * CELL_SIZE);
            brownCell.setY(newRow * CELL_SIZE);
        }
    }

    private void addBrownCells(int count) {
        int added = 0;
        while (added < count) {
            int row = random.nextInt(MAZE_SIZE);
            int col = random.nextInt(MAZE_SIZE);

            if (maze[row][col].getFill() == Color.WHITE && 
                !(row == 0 && col == 0) && 
                !(row == MAZE_SIZE-1 && col == MAZE_SIZE-1)) {
                
                Rectangle brownCell = new Rectangle(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                brownCell.setFill(Color.BROWN);
                root.getChildren().add(brownCell);
                brownCells.add(brownCell); // Add to our list of brown cells
                added++;

                addDragFunctionality(brownCell);
            }
        }
    }

    // Let player drag brown cells
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

            // Determine if and how to move the brown cell based on drag gesture
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
        
        if (maze[row][col].getFill() != Color.WHITE) {
            return false;
        }
        
        // Check if there's a brown cell at this position
        for (Rectangle brownCell : brownCells) {
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
                maze[newRow][newCol].setFill(Color.WHITE);
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
    
    // Helper method to check if a cell is valid and unvisited
    private boolean isValidCell(int row, int col, boolean[][] visited) {
        return row >= 0 && row < MAZE_SIZE && 
               col >= 0 && col < MAZE_SIZE &&
               maze[row][col].getFill() == Color.WHITE &&
               !visited[row][col];
    }

    // Check if a cell has white neighbors on all sides
    private boolean hasWhiteNeighbors(int row, int col) {
        boolean up = row > 0 && maze[row - 1][col].getFill() == Color.WHITE;
        boolean down = row < MAZE_SIZE - 1 && maze[row + 1][col].getFill() == Color.WHITE;
        boolean left = col > 0 && maze[row][col - 1].getFill() == Color.WHITE;
        boolean right = col < MAZE_SIZE - 1 && maze[row][col + 1].getFill() == Color.WHITE;
        return up && down && left && right;
    }

    // Create and place the player
    private void createPlayer() {
        do {
            playerRow = random.nextInt(MAZE_SIZE);
            playerCol = random.nextInt(MAZE_SIZE);
        } while (maze[playerRow][playerCol].getFill() == Color.BLACK || !hasWhiteNeighbors(playerRow, playerCol));
        
        player = new Circle((playerCol + 0.5) * CELL_SIZE, (playerRow + 0.5) * CELL_SIZE, PLAYER_SIZE / 2, Color.BLUE);
        root.getChildren().add(player);
    }

    // Create and place the monster (similar to player creation)
    private void createMonster() {
        do {
            monsterRow = random.nextInt(MAZE_SIZE);
            monsterCol = random.nextInt(MAZE_SIZE);
        } while (maze[monsterRow][monsterCol].getFill() == Color.BLACK || !hasWhiteNeighbors(monsterRow, monsterCol));
        
        monster = new Circle((monsterCol + 0.5) * CELL_SIZE, (monsterRow + 0.5) * CELL_SIZE, MONSTER_SIZE / 2, Color.RED);
        root.getChildren().add(monster);
    }

    // Create and place gadgets (hieroglyphs)
    private void createGadgets(int count) {
        for (int i = 0; i < count; i++) {
            int row, col;
            do {
                row = random.nextInt(MAZE_SIZE);
                col = random.nextInt(MAZE_SIZE);
            } while (maze[row][col].getFill() == Color.BLACK || !hasWhiteNeighbors(row, col));

            Circle gadget = new Circle((col + 0.5) * CELL_SIZE, (row + 0.5) * CELL_SIZE, GADGET_SIZE / 2, Color.GOLD);
            gadgets.add(gadget);
            root.getChildren().add(gadget);
        }
    }

    // Create and place the portal (appears after collecting all gadgets)
    private void createPortal() {
        int row, col;
        do {
            row = random.nextInt(MAZE_SIZE);
            col = random.nextInt(MAZE_SIZE);
        } while (maze[row][col].getFill() == Color.BLACK || !hasWhiteNeighbors(row, col));

        portal = new Circle((col + 0.5) * CELL_SIZE, (row + 0.5) * CELL_SIZE, GADGET_SIZE / 2);
        portal.setFill(Color.rgb(255, 0, 0, 0.6));
        root.getChildren().add(portal);
        
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
            checkGadgetCollision();
        }
    }

    // Check if player has collected a gadget
    private void checkGadgetCollision() {
        gadgets.removeIf(gadget -> {
            if (player.getBoundsInParent().intersects(gadget.getBoundsInParent())) {
                root.getChildren().remove(gadget);
                collectedGadgets++;
                String gadgetMessage = "Gadget collected! Gadgets left: " + (3 - collectedGadgets);
                updateFeedback(gadgetMessage, 2, "Collect 3 coins to win! Watch out for the monster!");

                if (collectedGadgets == 3 && !portalAdded) {
                    createPortal();
                    updateFeedback("All 3 gadgets collected!", 2, "Enter the portal to teleport to the next level!");
                }
                return true;
            }
            return false;
        });
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
        // Check if the player is caught by the monster
        if (player.getBoundsInParent().intersects(monster.getBoundsInParent())) {
            updateFeedback("Game Over! The monster caught you!", 2, "");
            
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> mainMenu(primaryStage));
            pause.play();

            return true;
        }
        
        // Check if the player has reached the portal
        if (portalAdded && player.getBoundsInParent().intersects(portal.getBoundsInParent())) {
            updateFeedback("You have won! Teleporting you to the next level ...", 2, "");
            
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> mainMenu(primaryStage));
            pause.play();

            return true;
        }
        return false;
    }

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

    // Restart the game
    private void restart(Stage mainStage) {
        mainStage.close();
        start(new Stage());
    }
}