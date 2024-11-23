package inkball;
import processing.core.PApplet;
import processing.core.PImage;

public class Wall extends Tile {
    public PImage wallImage;
    private PApplet app;

    public Wall(int x, int y, int cellSize, PImage wallImage, PApplet app) {
        super(x, y, cellSize);
        this.wallImage = wallImage;
        this.app = app;
    }

    @Override
    public void render() {
        app.image(wallImage, x, y + 64, cellSize, cellSize);  // Draw the wall image
    }
}
