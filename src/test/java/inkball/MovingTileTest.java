package inkball;

import org.junit.jupiter.api.*;

import inkball.App;
import inkball.MovingTile;
import processing.core.PImage;

import static org.junit.jupiter.api.Assertions.*;

public class MovingTileTest {

    private MovingTile movingTile;
    private MockApp app;
    private PImage tileImage;

    @BeforeAll
    public static void setUpClass() {
        App.WIDTH = 576;
        App.HEIGHT = 640;
    }

    @BeforeEach
    public void setup() {
        app = new MockApp();
        tileImage = app.wallImage[4]; // Use a dummy wall image for moving tile
        movingTile = new MovingTile(0, App.TOPBAR, tileImage, app);
    }

    @Test
    public void testTileMovement() {
        // Move the top-left tile to the top-right
        while (!movingTile.isComplete()) {
            movingTile.move();
        }
        assertEquals(App.WIDTH - App.CELLSIZE, movingTile.x, "Tile should reach the top-right corner.");
        // Ensure it reports as complete when reaching the edge
        assertTrue(movingTile.isComplete(), "Tile should be marked as complete after reaching the top-right corner.");
    }

    @Test
    public void testSimultaneousTileMovement() {
        // Simulate two tiles moving in parallel
        tileImage = app.wallImage[4];
        MovingTile topLeftTile = new MovingTile(0, App.TOPBAR, tileImage, app);
        MovingTile bottomRightTile = new MovingTile(App.WIDTH - App.CELLSIZE, App.HEIGHT - App.CELLSIZE, tileImage, app);

        // Move both tiles simultaneously to their target edges
        while (!topLeftTile.isComplete() || !bottomRightTile.isComplete()) {
            if (!topLeftTile.isComplete()) {
                topLeftTile.move();
            }
            if (!bottomRightTile.isComplete()) {
                bottomRightTile.move();
            }
        }

        // Check that both tiles have reached their target positions
        assertEquals(App.WIDTH - App.CELLSIZE, topLeftTile.x, "Top-left tile should reach the top-right corner");
        assertEquals(0, bottomRightTile.x, "Bottom-right tile should reach the bottom-left corner");

        assertTrue(topLeftTile.isComplete(), "Top-left tile should be complete.");
        assertTrue(bottomRightTile.isComplete(), "Bottom-right tile should be complete.");
    }
}
