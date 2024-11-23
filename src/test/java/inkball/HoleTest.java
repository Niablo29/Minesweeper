package inkball;

import org.junit.jupiter.api.*;

import inkball.App;
import inkball.Ball;
import inkball.Hole;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class HoleTest {

    private Hole hole;
    private PImage holeImage;
    private App app;

    @BeforeEach
    public void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup();
        holeImage = app.holeImage[1]; // Use the hole images loaded in app.setup()
        hole = new Hole(100, 100, 32, 1, holeImage, app);
    }

    @Test
    public void testHoleInitialization() {
        // Test that the hole is initialized with correct properties
        assertEquals(100, hole.x, "Hole x position should be 100");
        assertEquals(100, hole.y, "Hole y position should be 100");
        assertEquals(1, hole.getColor(), "Hole color should be 1");
    }

    @Test
    public void testHoleCaptureRadius() {
        // Test that the hole's capture radius is set correctly
        assertEquals(16, hole.getCaptureRadius(), "Hole capture radius should be half the cell size");
    }
}
