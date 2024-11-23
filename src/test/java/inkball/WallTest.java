package inkball;

import org.junit.jupiter.api.*;
import processing.core.PApplet;
import processing.core.PImage;

import static org.junit.jupiter.api.Assertions.*;

public class WallTest {

    private App app;
    private Wall wall;
    private PImage wallImage;

    @BeforeEach
    public void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup();
        wallImage = app.wallImage[0];
        wall = new Wall(100, 100, 32, wallImage, app);
    }

    @Test
    public void testWallInitialization() {
        // Test that the wall is initialized with correct position and image
        assertEquals(100, wall.x, "Wall x position should be 100");
        assertEquals(100, wall.y, "Wall y position should be 100");
        assertEquals(32, wall.cellSize, "Wall cell size should be 32");
        assertEquals(wallImage, wall.wallImage, "Wall image should be set correctly");
    }

    @Test
    public void testColoredWallChangesBallColor() {
        // Test that the ball changes color when colliding with a colored wall
        ColoredWall coloredWall = new ColoredWall(100, 100, 32, 2, app.wallImage[2], app);
        Ball ball = new Ball(100, 100, 0, 0, 1, app.ballImage[1], app);
        ball.handleCollision(coloredWall);
        assertEquals(2, ball.getColor(), "Ball color should change to colored wall's color");
    }
}
