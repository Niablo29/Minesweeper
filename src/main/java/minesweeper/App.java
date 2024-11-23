package minesweeper;

import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32; 
    public static final int TOPBAR = 64; 
    public static final int WIDTH = 864; 
    public static final int HEIGHT = 640; 
    public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
    public static final int BOARD_HEIGHT = 18;
    public static final int FPS = 30;

    public static final int DEFAULT_MINE_COUNT = 100;

    public static Random random = new Random();

    private Tile[][] board;
    private ArrayList<Tile> mines = new ArrayList<>();
    private PImage[] mineImages = new PImage[10]; 
    private PImage[] tileImages = new PImage[5];
    private static int mineCount;
    private boolean gameOver;
    private boolean gameWon;
    private int timeElapsed;
    private int frameCounter;

    private boolean triggers;
    private int minenumber;

    

    public App() {
        // this.mineCount = DEFAULT_MINE_COUNT;
    }

    /**
     * Initialise the setting of the window size.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);    
    }

    public static int[][] mineCountColour = new int[][] {
        {0,0,0}, // 0 is not shown
        {0,0,255},
        {0,133,0},
        {255,0,0},
        {0,0,132},
        {132,0,0},
        {0,132,132},
        {132,0,132},
        {32,32,32}
    };
    

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
    @Override
    public void setup() {
        frameRate(FPS);
        loadImages();
        initialiseBoard(BOARD_WIDTH, BOARD_HEIGHT, mineCount);
        timeElapsed = 0;
        frameCounter = 0;
        gameOver = false;
        gameWon = false;
        triggers = false;
        minenumber = 0;
    }

    /**
     * Load all necessary images for the game.
     */
    private void loadImages() {
        for (int i = 0; i < mineImages.length; i++) {
            mineImages[i] = loadImage("minesweeper/mine" + i + ".png");
        }
    
        // tileImages[0] = loadImage("minesweeper/tile.png");
        tileImages[1] = loadImage("minesweeper/tile1.png");
        tileImages[2] = loadImage("minesweeper/tile2.png");
        tileImages[3] = loadImage("minesweeper/tile.png");
        tileImages[4] = loadImage("minesweeper/flag.png");
    }

 


    private void initialiseBoard(int width, int height, int numMines) {
        board = new Tile[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                board[i][j] = new Tile();
            }
        }

        placeMines(numMines);
        calculateAdjacentMines();
    }



    private void placeMines(int numMines) {
        int count = 0;

        while (count < numMines) {
            int i = random.nextInt(BOARD_WIDTH);
            int j = random.nextInt(BOARD_HEIGHT);

            if (!board[i][j].getIsMine()) {
                board[i][j].setMine(true);
                mines.add(board[i][j]);
                count++;
            }
        }
    }

 


    private void calculateAdjacentMines() {
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                if (!board[i][j].getIsMine()) {
                    int mineCount = countAdjacentMines(i, j);
                    board[i][j].setAdjCount(mineCount);
                }
            }
        }
    }


    
    private int countAdjacentMines(int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nx = x + i;
                int ny = y + j;
                if (nx >= 0 && ny >= 0 && nx < BOARD_WIDTH && ny < BOARD_HEIGHT) {
                    if (board[nx][ny].getIsMine()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKey() == 'r' && gameOver == true) {
            resetGame();
        }
    }


    
    private void resetGame() {
        initialiseBoard(BOARD_WIDTH, BOARD_HEIGHT, mineCount);
        timeElapsed = 0;
        frameCounter = 0;
        frameCount = 0;
        gameOver = false;
        gameWon = false;
        triggers = false;
        minenumber = 0;

    }

    /**
     * Handle mouse pressed events.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver || gameWon) {
            return;
        }

        int x = mouseX / CELLSIZE;
        int y = (mouseY - TOPBAR) / CELLSIZE;

        if (x >= 0 && y >= 0 && x < BOARD_WIDTH && y < BOARD_HEIGHT) {
            if (e.getButton() == LEFT) {
                if (!board[x][y].getIsFlagged()) {
                    revealTile(x, y);
                }
            } else if (e.getButton() == RIGHT) {
                board[x][y].setFlagged(!board[x][y].getIsFlagged());
            }
        }
    }

    public void mouseHovering(int i, int j) {
        int tileX = i * CELLSIZE;
        int tileY = j * CELLSIZE + TOPBAR;

        if (mouseX > tileX && mouseX < tileX + CELLSIZE && mouseY > tileY && mouseY < tileY + CELLSIZE) {
            board[i][j].checkHovering(true);
        } else {
            board[i][j].checkHovering(false);
        }
    }


    
    private void revealTile(int x, int y) {
        if (board[x][y].getIsRevealed() || board[x][y].getIsFlagged()) {
            return;
        }

        board[x][y].setRevealed(true);

        if (board[x][y].getIsMine()) {
            gameOver = true;
            triggers = true;
        } else {
            if (board[x][y].getAdjCount() == 0) {
                revealAdjacentTiles(x, y);
            }
            checkWinCondition();
        }
    }



    private void revealAdjacentTiles(int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nx = x + i;
                int ny = y + j;
                if (nx >= 0 && ny >= 0 && nx < BOARD_WIDTH && ny < BOARD_HEIGHT) {
                    revealTile(nx, ny);
                }
            }
        }
    }

  

    private void checkWinCondition() {
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                if (!board[i][j].getIsMine() && !board[i][j].getIsRevealed()) {
                    return;
                }
            }
        }
        gameWon = true;
    }


    private void triggerExplosion() {
        if (minenumber < mines.size()) {
            mines.get(minenumber).setRevealed(true);
            minenumber++;  
        } else{
            mines.clear();

        }
    
    }

    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        background(255);

        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                mouseHovering(i, j);
                board[i][j].draw(this, i, j, mineImages, tileImages);
            }
        }

        if (triggers == true){
            triggerExplosion();
        }

        drawTopBar();

        if (!gameOver && !gameWon) {
            frameCounter++;
            if (frameCounter >= FPS) {
                timeElapsed++;
                frameCounter = 0;
            }
        }

        
    }

    private void drawTopBar() {
        fill(0);
        textSize(24);
        textAlign(LEFT, CENTER); 
    
        if (gameOver) {
            textAlign(CENTER, CENTER);
            text("You Lost!", WIDTH / 2, TOPBAR / 2);
        } else if (gameWon) {
            textAlign(CENTER, CENTER);
            text("You Win!", WIDTH / 2, TOPBAR / 2);
        }
    
        textAlign(RIGHT, CENTER); 
        text("Time: " + timeElapsed, WIDTH - 10, TOPBAR / 2);
    }
    

    public static void main(String[] args) {
        PApplet.main("minesweeper.App", args);

        if (args.length > 0) {
            try {
                mineCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                mineCount = DEFAULT_MINE_COUNT;
            }
        } else {
            mineCount = DEFAULT_MINE_COUNT;
        }
    }
}

