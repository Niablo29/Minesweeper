package inkball;
import processing.core.PApplet;
import processing.core.PImage;

public class OneWay extends Tile {
    private PImage oneImage;
    private PApplet app;

    public OneWay(int x, int y, int cellSize, PImage oneImage, PApplet app) {
        super(x, y, cellSize);
        this.oneImage = oneImage;
        this.app = app;
    }

    @Override
    public void render() {
        app.image(oneImage, x, y + 64, cellSize, cellSize);  // Draw the wall image
    }
}

