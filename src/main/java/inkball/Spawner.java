package inkball;
import processing.core.PApplet;
import processing.core.PImage;

public class Spawner extends Tile {
    public PImage spawnerImage;  // Image to represent the spawner
    public PApplet app;

    public Spawner(int x, int y, int cellSize, PImage spawnerImage, PApplet app) {
        super(x, y, cellSize);
        this.spawnerImage = spawnerImage;
        this.app = app;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    @Override
    public void render() {
        // Draw the spawner image at position (x, y) with the given size
        app.image(spawnerImage, x, y + 64, cellSize, cellSize);
    }
}

