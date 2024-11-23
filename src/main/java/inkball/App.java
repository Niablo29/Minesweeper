package inkball;

import processing.core.PApplet;
import processing.core.PVector;
import processing.core.PImage;
import processing.data.JSONObject;
import processing.data.JSONArray;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.io.*;
import java.util.*;

import inkball.MovingTile;

public class App extends PApplet {

    public static final int CELLSIZE = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 576;
    public static int HEIGHT = 640;
    public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
    public static final int BOARD_HEIGHT = 20;
    public static final int FPS = 30;

    public JSONObject config;
    public JSONArray levelsConfig;
    public int currentLevelIndex = 0;

    public static Random random = new Random();

    public String[][] levelOutline;
    public Object[][] levelObjects;
    public PImage[] ballImage = new PImage[5];
    public PImage[] holeImage = new PImage[5];
    public PImage[] wallImage = new PImage[5];
    public PImage tileImage;
    public PImage entryImage;
    public PImage oneWayImage;
    public List<Ball> balls = new ArrayList<>();
    public List<Spawner> spawners = new ArrayList<>();

    public List<Line> lines = new ArrayList<>();  // List to store all active lines
    public Line currentLine = null;  // The line currently being drawn

    public Queue<Integer> upcomingBallsQueue = new LinkedList<>();  // Store indices of ball colors
    public float spawnTimer;  // Spawn interval from config
    public int score = 0;  // Player's score
    public int levelTime;  // Total time for the level in seconds
    public float timeRemaining;  // Time remaining in the current level
    public boolean isPaused = false;  // Game pause state

    public Map<String, Integer> scoreIncreaseMap = new HashMap<>();
    public Map<String, Integer> scoreDecreaseMap = new HashMap<>();
    public float scoreIncreaseModifier;
    public float scoreDecreaseModifier;

    public Map<String, Integer> colorNameToIndex = new HashMap<>();

    // Define game states
    public enum GameState {
        PLAYING,
        PAUSED,
        LEVEL_COMPLETE,
        TIME_UP,
        GAME_OVER
    }

    public GameState gameState = GameState.PLAYING;

    // Variables for level completion animation
    public List<MovingTile> movingTiles = new ArrayList<>();
    public float tileMoveTimer = 0; // Timer to control tile movement
    public float timeToScoreTimer = 0; // Timer to control time-to-score conversion

    // Constructor
    public App() {
        // Do not load config here; move to setup()
    }

    /**
     * Initialise the setting of the window size.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
    @Override
    public void setup() {
        frameRate(FPS);

        // Load config.json here
        this.config = loadJSONObject("config.json");

        // Initialize color name to index mapping
        colorNameToIndex.put("grey", 0);
        colorNameToIndex.put("orange", 1);
        colorNameToIndex.put("blue", 2);
        colorNameToIndex.put("green", 3);
        colorNameToIndex.put("yellow", 4);

        // Load images
        for (int i = 0; i < ballImage.length; i++) {
            ballImage[i] = loadImage("inkball/ball" + i + ".png");
        }
        for (int i = 0; i < holeImage.length; i++) {
            holeImage[i] = loadImage("inkball/hole" + i + ".png");
        }
        for (int i = 0; i < wallImage.length; i++) {
            wallImage[i] = loadImage("inkball/wall" + i + ".png");
        }
        tileImage = loadImage("inkball/tile.png");
        entryImage = loadImage("inkball/entrypoint.png");
        oneWayImage = loadImage("inkball/one_way.png");

        // Load scoring configurations
        loadScoringConfig();

        // Load levels configuration
        levelsConfig = config.getJSONArray("levels");

        // Load the first level
        loadLevel(currentLevelIndex);
    }

    public void loadScoringConfig() {
        JSONObject increaseScores = config.getJSONObject("score_increase_from_hole_capture");
        JSONObject decreaseScores = config.getJSONObject("score_decrease_from_wrong_hole");

        // Adjusted loop for increaseScores
        for (Object keyObj : increaseScores.keys()) {
            String colorName = (String) keyObj;
            int scoreValue = increaseScores.getInt(colorName);
            scoreIncreaseMap.put(colorName, scoreValue);
        }

        // Adjusted loop for decreaseScores
        for (Object keyObj : decreaseScores.keys()) {
            String colorName = (String) keyObj;
            int scoreValue = decreaseScores.getInt(colorName);
            scoreDecreaseMap.put(colorName, scoreValue);
        }
    }

    public void loadLevel(int levelIndex) {
        if (levelIndex >= levelsConfig.size()) {
            // No more levels
            gameState = GameState.GAME_OVER;
            return;
        }

        JSONObject levelConfig = levelsConfig.getJSONObject(levelIndex);
        String layoutFile = levelConfig.getString("layout");
        levelTime = levelConfig.getInt("time");
        score = 0;
        timeRemaining = (float) levelTime;
        spawnTimer = levelConfig.getFloat("spawn_interval");
        scoreIncreaseModifier = levelConfig.getFloat("score_increase_from_hole_capture_modifier");
        scoreDecreaseModifier = levelConfig.getFloat("score_decrease_from_wrong_hole_modifier");

        // Load the ball queue for this level
        upcomingBallsQueue.clear();
        JSONArray ballsArray = levelConfig.getJSONArray("balls");
        for (int i = 0; i < ballsArray.size(); i++) {
            String colorName = ballsArray.getString(i);
            int colorIndex = colorNameToIndex.getOrDefault(colorName.toLowerCase(), 0);
            upcomingBallsQueue.add(colorIndex);
        }

        // Load the level layout
        loadLevels(layoutFile);
        initializeLevel();

        gameState = GameState.PLAYING;
    }

    public void loadLevels(String filePath) {
        List<String> levelLines = new ArrayList<>();
        String[] templines;

        // Load the file using createReader to get the correct path
        try (BufferedReader br = createReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                levelLines.add(line);  // Add each line to the list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize the 2D array based on the level size
        int rows = levelLines.size();
        int cols = levelLines.get(0).length();  // Get the number of columns from the first line
        levelOutline = new String[rows][cols];  // Initialize the levelOutline array

        // Initialize all values to empty strings to avoid NullPointerException
        for (int row = 0; row < rows; row++) {
            Arrays.fill(levelOutline[row], "");
        }

        for (int row = 0; row < rows; row++) {
            templines = levelLines.get(row).split("");  // Split the row into individual characters
            for (int col = 0; col < cols; col++) {
                if (templines[col].equals("H") && col + 1 < cols) {
                    // Process 'H' and the next character
                    levelOutline[row][col] = "H" + templines[col + 1];

                    // Mark adjacent tiles as blank ("bl")
                    if (col + 1 < cols) {
                        levelOutline[row][col + 1] = "bl";  // Mark right tile as blank
                    }
                    if (row + 1 < rows) {
                        levelOutline[row + 1][col] = "bl";  // Mark below tile as blank
                    }
                    if (col + 1 < cols && row + 1 < rows) {
                        levelOutline[row + 1][col + 1] = "bl";  // Mark bottom-right tile as blank
                    }

                    // Skip the next column because it has already been processed
                    col++;
                } else if (templines[col].equals("B") && col + 1 < cols) {
                    levelOutline[row][col] = "B" + templines[col + 1];
                    if (col + 1 < cols) {
                        levelOutline[row][col + 1] = " ";  // Mark right tile as blank
                    }
                    col++;
                } else {
                    // If the tile is not marked as "bl", set the value
                    if (!levelOutline[row][col].equals("bl")) {
                        levelOutline[row][col] = templines[col];
                    }
                }
            }
        }
    }

    public void initializeLevel() {
        int rows = levelOutline.length;
        int cols = levelOutline[0].length;

        levelObjects = new Object[rows][cols];
        balls.clear();  // Clear existing balls
        lines.clear();  // Clear existing lines
        spawners.clear(); // Clear existing spawners

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String cell = levelOutline[row][col];
                // Depending on the character, create the appropriate object and store it
                switch (cell) {
                    case "X":  // Wall
                        levelObjects[row][col] = new Wall(col * CELLSIZE, row * CELLSIZE, CELLSIZE, wallImage[0], this);
                        break;
                    case "H0":
                        levelObjects[row][col] = new Hole(col * CELLSIZE, row * CELLSIZE, 32, 0, holeImage[0], this);
                        break;
                    case "H1":
                        levelObjects[row][col] = new Hole(col * CELLSIZE, row * CELLSIZE, 32, 1, holeImage[1], this);
                        break;
                    case "H2":
                        levelObjects[row][col] = new Hole(col * CELLSIZE, row * CELLSIZE, 32, 2, holeImage[2], this);
                        break;
                    case "H3":
                        levelObjects[row][col] = new Hole(col * CELLSIZE, row * CELLSIZE, 32, 3, holeImage[3], this);
                        break;
                    case "H4":
                        levelObjects[row][col] = new Hole(col * CELLSIZE, row * CELLSIZE, 32, 4, holeImage[4], this);
                        break;
                    case "1":
                        levelObjects[row][col] = new ColoredWall(col * CELLSIZE, row * CELLSIZE, CELLSIZE, 1, wallImage[1], this);
                        break;
                    case "2":
                        levelObjects[row][col] = new ColoredWall(col * CELLSIZE, row * CELLSIZE, CELLSIZE, 2, wallImage[2], this);
                        break;
                    case "3":
                        levelObjects[row][col] = new ColoredWall(col * CELLSIZE, row * CELLSIZE, CELLSIZE, 3, wallImage[3], this);
                        break;
                    case "4":
                        levelObjects[row][col] = new ColoredWall(col * CELLSIZE, row * CELLSIZE, CELLSIZE, 4, wallImage[4], this);
                        break;
                    case "S":  // Spawner
                        Spawner spawner = new Spawner(col * CELLSIZE, row * CELLSIZE, CELLSIZE, entryImage, this);
                        levelObjects[row][col] = spawner;
                        spawners.add(spawner);
                        break;
                    case "bl":
                        levelObjects[row][col] = null;
                        break;
                    case "B1":  // Pre-placed ball
                        Ball orangeBall = new Ball(col * CELLSIZE, row * CELLSIZE, 2, 2, 1, ballImage[1], this);
                        balls.add(orangeBall);
                        levelObjects[row][col] = new BlankTile(col * CELLSIZE, row * CELLSIZE, CELLSIZE, tileImage, this);
                        break;
                    case "B2":  // Pre-placed ball
                        Ball blueBall = new Ball(col * CELLSIZE, row * CELLSIZE, -2, 2, 2, ballImage[2], this);
                        balls.add(blueBall);
                        levelObjects[row][col] = new BlankTile(col * CELLSIZE, row * CELLSIZE, CELLSIZE, tileImage, this);
                        break;
                    case "O":
                        levelObjects[row][col] = new OneWay(col * CELLSIZE, row * CELLSIZE, CELLSIZE, oneWayImage, this);
                        break;
                    default:
                        levelObjects[row][col] = new BlankTile(col * CELLSIZE, row * CELLSIZE, CELLSIZE, tileImage, this);  // Empty space
                        break;
                }
            }
        }
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
    @Override
    public void keyPressed(KeyEvent event) {
        key = event.getKey();
        loop();
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED || gameState == GameState.TIME_UP) {
            if (key == 'r' || key == 'R') {
                // Restart the current level
                if(gameState == GameState.TIME_UP){
                    currentLevelIndex = 0;
                    gameState = GameState.PLAYING;
                }
                loadLevel(currentLevelIndex);
                score = 0;
                isPaused = false;
                gameState = GameState.PLAYING;
                loop();  // Restart the draw loop
            } else if (key == ' ') {
                // Toggle pause state
                isPaused = !isPaused;
            }
        } else if (gameState == GameState.GAME_OVER || gameState == GameState.TIME_UP) {
            if (key == 'r' || key == 'R') {
                // Restart the game from level 0
                currentLevelIndex = 0;
                loadLevel(currentLevelIndex);
                score = 0;
                isPaused = false;
                gameState = GameState.PLAYING;
                loop();  // Restart the draw loop
            }
        }
        // Do nothing during LEVEL_COMPLETE
    }

    /**
     * Receive key released signal from the keyboard.
     */
    @Override
    public void keyReleased() {
        // No action needed
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            if (e.getButton() == LEFT) {
                currentLine = new Line(this);
                currentLine.addPoint(mouseX, mouseY - TOPBAR);
            } else if (e.getButton() == RIGHT) {
                // Check if a line is clicked and remove it
                removeLineAtPosition(mouseX, mouseY - TOPBAR);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            if (e.getButton() == LEFT && currentLine != null) {
                currentLine.addPoint(mouseX, mouseY - TOPBAR);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            if (e.getButton() == LEFT && currentLine != null) {
                lines.add(currentLine);
                currentLine = null;
            }
        }
    }

    public void removeLineAtPosition(float x, float y) {
        Iterator<Line> iterator = lines.iterator();
        while (iterator.hasNext()) {
            Line line = iterator.next();
            if (lineContainsPoint(line, x, y)) {
                iterator.remove();
                break; // Remove only one line
            }
        }
    }

    public boolean lineContainsPoint(Line line, float x, float y) {
        List<PVector> points = line.getPoints();
        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);
            if (pointNearLineSegment(p1.x, p1.y, p2.x, p2.y, x, y, 5)) {
                return true;
            }
        }
        return false;
    }

    public float distancePointToSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            dx = px - x1;
            dy = py - y1;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        float t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);

        if (t < 0) {
            dx = px - x1;
            dy = py - y1;
        } else if (t > 1) {
            dx = px - x2;
            dy = py - y2;
        } else {
            float nearX = x1 + t * dx;
            float nearY = y1 + t * dy;
            dx = px - nearX;
            dy = py - nearY;
        }

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public boolean pointNearLineSegment(float x1, float y1, float x2, float y2, float px, float py, float threshold) {
        float distance = distancePointToSegment(px, py, x1, y1, x2, y2);
        return distance <= threshold;
    }

    public PImage getTileImage() {
        return tileImage;
    }

    public void drawTopBar() {
        // Draw the background of the top bar
        noStroke();  // No outline
        fill(200);  // Light grey color
        rect(0, 0, WIDTH, TOPBAR);

        // Draw the black rectangle for the ball queue and spawn timer
        fill(0);  // Black color
        rect(5, 12, 130, 40);

        // Draw the upcoming balls in the queue
        int ballIconSize = 20;
        int ballIconSpacing = 24;
        int startX = 10;
        int startY = (TOPBAR - ballIconSize) / 2;

        int i = 0;
        for (int colorIndex : upcomingBallsQueue) {
            PImage ballIcon = ballImage[colorIndex];
            float posX = startX + i * ballIconSpacing;
            image(ballIcon, posX, startY, ballIconSize, ballIconSize);
            i++;
            if (i >= 5) break;
        }

        // Draw the spawn timer
        fill(0);  // Black color for text
        textSize(15);
        textAlign(LEFT, CENTER);
        text(String.format("%.1f", spawnTimer), WIDTH / 4 + 5, TOPBAR / 2);

        // Draw the score
        fill(0);  // Black color for text
        textSize(16);
        textAlign(RIGHT, TOP);
        text("Score: " + score, WIDTH - 10, 10);

        // Draw the level timer
        text("Time: " + (int) timeRemaining, WIDTH - 10, 30);
    }

    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        background(200);  // Clear the screen

        // Render game elements
        renderGameBoard();
        renderBalls();
        renderLines();
        drawTopBar();

        if (gameState == GameState.PLAYING) {
            if (!isPaused) {
                // Update game logic
                updateTimers();
                updateBalls();

                // Check if time has run out
                if (timeRemaining <= 1
                ) {
                    // Level ends in a loss
                    gameState = GameState.TIME_UP;
                }

                // Check if all balls have been captured and queue is empty
                if (balls.isEmpty() && upcomingBallsQueue.isEmpty()) {
                    // Transition to level complete state
                    gameState = GameState.LEVEL_COMPLETE;
                    startLevelCompletionAnimation();
                }

            } else {
                // Game is paused
                fill(0);
                textSize(20);
                textAlign(CENTER, CENTER);
                text("***Paused***", WIDTH / 2, 32);
            }
        } else if (gameState == GameState.TIME_UP) {
            fill(0);
            textSize(20);
            textAlign(CENTER, CENTER);
            text("=== TIME'S UP ===", WIDTH / 2, 32);
            noLoop();
        } else if (gameState == GameState.LEVEL_COMPLETE) {
            updateLevelCompletionAnimation();
        } else if (gameState == GameState.GAME_OVER) {
            fill(0);
            textSize(20);
            textAlign(CENTER, CENTER);
            text("=== ENDED ===", WIDTH / 2, 32);
            noLoop();  // Stop the draw loop
        }
    }

    public void renderGameBoard() {
        // Render all entities in the level
        for (int row = 0; row < levelObjects.length; row++) {
            for (int col = 0; col < levelObjects[row].length; col++) {
                Object entity = levelObjects[row][col];
                if (entity != null && entity instanceof Tile) {
                    ((Tile) entity).render();  // Call render on any Tile-like object
                }
            }
        }
    }

    public void renderBalls() {
        // Render balls
        for (Ball ball : balls) {
            ball.render();
        }
    }

    private void renderLines() {
        // Render all active lines
        for (Line line : lines) {
            line.render();
        }

        // If a line is currently being drawn, render it
        if (currentLine != null) {
            currentLine.render();
        }
    }

    public void updateBalls() {
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            ball.update(levelObjects, lines);  // Pass the list of lines

            // Check if the ball has been captured by a hole
            if (ball.isCaptured()) {
                // Update score based on whether the ball entered the correct hole
                String colorName = getColorName(ball.getColor());
                if (ball.enteredCorrectHole()) {
                    int increaseScore = (int) (scoreIncreaseMap.get(colorName) * scoreIncreaseModifier);
                    score += increaseScore;  // Increase score
                } else {
                    int decreaseScore = (int) (scoreDecreaseMap.get(colorName) * scoreDecreaseModifier);
                    score -= decreaseScore;  // Decrease score
                }
                // Remove the ball from play
                ballIterator.remove();
            }
        }
    }

    public void updateTimers() {
        // Update spawn timer
        spawnTimer -= 1 / frameRate;
        if (spawnTimer <= 0) {
            spawnNextBall();
            // Reset the spawn timer based on level configuration
            JSONObject levelConfig = levelsConfig.getJSONObject(currentLevelIndex);
            spawnTimer = levelConfig.getFloat("spawn_interval");
        }

        // Update level timer every frame
        timeRemaining -= 1 / frameRate;
        if (timeRemaining <= 0) {
            timeRemaining = 0;
        }
    }

    public void spawnNextBall() {
        if (!upcomingBallsQueue.isEmpty()) {
            int ballColorIndex = upcomingBallsQueue.poll();
            if (!spawners.isEmpty()) {
                // Choose a random spawner
                int spawnerIndex = random.nextInt(spawners.size());
                Spawner spawner = spawners.get(spawnerIndex);
                int spawnX = spawner.getX();
                int spawnY = spawner.getY();

                // Generate random velocities (±2, ±2) for requirement 6
                float vx = (random.nextBoolean() ? 2.0f : -2.0f);
                float vy = (random.nextBoolean() ? 2.0f : -2.0f);

                Ball newBall = new Ball(spawnX, spawnY, vx, vy, ballColorIndex, ballImage[ballColorIndex], this);
                balls.add(newBall);
            }
        }
    }

    public String getColorName(int colorIndex) {
        for (Map.Entry<String, Integer> entry : colorNameToIndex.entrySet()) {
            if (entry.getValue() == colorIndex) {
                return entry.getKey();
            }
        }
        return "grey";  // Default color name
    }

    public void startLevelCompletionAnimation() {
        // Initialize moving tiles
        movingTiles.clear();

        // Create the two yellow tiles starting at the corners
        movingTiles.add(new MovingTile(0, TOPBAR, wallImage[4], this));
        movingTiles.add(new MovingTile(WIDTH - CELLSIZE, HEIGHT - CELLSIZE, wallImage[4], this));
    
        // Reset timer
        tileMoveTimer = 0;
    }

    public void updateLevelCompletionAnimation() {
        // Update timers
        tileMoveTimer += 1 / frameRate;
    
        // Update moving tiles at intervals of 0.067 seconds
        if (tileMoveTimer >= 0.067f) {
            for (MovingTile tile : movingTiles) {
                tile.move();
            }
            tileMoveTimer = 0;
        }
    
        // Render game elements
        renderGameBoard();
        renderBalls();
        renderLines();
        drawTopBar();
    
        // Render moving tiles
        for (MovingTile tile : movingTiles) {
            tile.render();
        }
    
        // Check if the animation is complete
        boolean allTilesComplete = true;
        for (MovingTile tile : movingTiles) {
            if (!tile.isComplete()) {
                allTilesComplete = false;
                break;
            }
        }
    
        if (allTilesComplete) {
            // Proceed to the next level
            currentLevelIndex++;
            if (currentLevelIndex < levelsConfig.size()) {
                loadLevel(currentLevelIndex);
                gameState = GameState.PLAYING;
            } else {
                // No more levels; end the game
                gameState = GameState.GAME_OVER;
            }
        }
    }
    


    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
