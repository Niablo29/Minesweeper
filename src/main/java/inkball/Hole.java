package inkball;

import processing.core.PApplet;
import processing.core.PImage;

public class Hole extends Tile {
    private PImage holeImage;  // Image to represent the hole
    private PApplet app;
    private int color;
    private float captureRadius;

    public Hole(int x, int y, int cellSize, int color, PImage holeImage, PApplet app) {
        super(x, y, cellSize);
        this.holeImage = holeImage;
        this.app = app;
        this.color = color;
        this.captureRadius = cellSize / 2;  // Define capture radius
    }

    @Override
    public void render() {
        // Draw the hole image at position (x, y) with the given size
        app.image(holeImage, x, y + 64, cellSize * 2, cellSize * 2);
    }

    public int getColor() {
        return this.color;
    }

    public float getCenterX() {
        return x + cellSize;
    }

    public float getCenterY() {
        return y + cellSize;
    }

    public float getCaptureRadius() {
        return captureRadius;
    }
}
