package inkball;

public abstract class Tile {
    protected int x, y;        // Position on the screen
    protected int cellSize;    // Size of the tile (typically 32x32)

    // Constructor to initialize position and size
    public Tile(int x, int y, int cellSize) {
        this.x = x;
        this.y = y;
        this.cellSize = cellSize;
    }

    // Abstract render method to be implemented by subclasses
    public abstract void render();
}
