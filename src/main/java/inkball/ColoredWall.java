package inkball;
import processing.core.PApplet;
import processing.core.PImage;

public class ColoredWall extends Wall {
    private int color; // Color index
    private PImage coloredWallImage;
    private PApplet app;

    public ColoredWall(int x, int y, int cellSize, int color, PImage image, PApplet app) {
        super(x, y, cellSize, image, app);
        this.color = color;
        this.coloredWallImage = image;
        this.app = app;
    }

    @Override
    public void render() {
        app.image(coloredWallImage, x, y + 64, cellSize, cellSize);
    }

    public int getColor() {
        return this.color;
    }
}
