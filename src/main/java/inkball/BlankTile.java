package inkball;
import processing.core.PApplet;
import processing.core.PImage;

public class BlankTile extends Tile {
    private PImage tileImage;  // Image for blank tiles
    private PApplet app;

    public BlankTile(int x, int y, int cellSize, PImage tileImage, PApplet app) {
        super(x, y, cellSize);
        this.tileImage = tileImage;
        this.app = app;
    }

    @Override
    public void render() {
        // Use the PApplet reference to draw the blank tile image
        app.image(tileImage, x, y + 64, cellSize, cellSize);
    }
}
