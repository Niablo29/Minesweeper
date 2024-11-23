package inkball;
import processing.core.PApplet;
import processing.core.PImage;

public class MovingTile {
    public int x, y;
    public PApplet app;
    public int cellSize;
    public PImage tileImage;
    public boolean isComplete = false;
    public int startX, startY;

    public MovingTile(int startX, int startY, PImage tileImage, PApplet app) {
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
        this.app = app;
        this.cellSize = App.CELLSIZE;
        this.tileImage = tileImage;
    }

    public void move() {
        if (!isComplete) {
            if (startX == 0 && startY == App.TOPBAR) {
                // Tile starting at top-left corner, moves right along the top edge
                if (x < App.WIDTH - cellSize) {
                    x += cellSize; // Move right
                } else {
                    isComplete = true; // Reached top-right corner
                }
            } else if (startX == App.WIDTH - cellSize && startY == App.HEIGHT - cellSize) {
                // Tile starting at bottom-right corner, moves left along the bottom edge
                if (x > 0) {
                    x -= cellSize; // Move left
                } else {
                    isComplete = true; // Reached bottom-left corner
                }
            }
        }
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void render() {
        app.image(tileImage, x, y, cellSize, cellSize);
    }

}
