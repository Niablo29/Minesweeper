package inkball;

import org.junit.jupiter.api.*;

import inkball.App;
import inkball.Ball;
import inkball.OneWay;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class OneWayTest {

    private OneWay oneWay;
    private PImage oneWayImage;
    private App app;

    @BeforeEach
    public void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup();
        // Allow some time for the app to initialize
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //
        }
        oneWayImage = app.oneWayImage;
        oneWay = new OneWay(100, 100, 32, oneWayImage, app);
    }

    @Test
    public void testOneWayInitialization() {
        // Test that the one-way tile is initialized correctly
        assertEquals(100, oneWay.x, "OneWay x position should be 100");
        assertEquals(100, oneWay.y, "OneWay y position should be 100");
    }

    @Test
    public void testBallPassesThroughAllowedDirection() {
        // Test that the ball passes through when moving in the allowed direction
        Object[][] levelObjects = new Object[20][18];
        levelObjects[3][3] = oneWay;

        // Initialize the ball for this test
        Ball ball = new Ball(100, 100, 2, 0, 1, app.ballImage[1], app);

        for (int i = 0; i < 20; i++) {
            ball.update(levelObjects, new ArrayList<>());
        }
        assertEquals(2, ball.vx, "Ball should pass through OneWay tile moving right");
    }

    @Test
    public void testBallBlockedInOppositeDirection() {
        // Verify that the ball is blocked when moving against the allowed direction
        Object[][] levelObjects = new Object[20][18];
        levelObjects[3][3] = oneWay;

        // Initialize the ball for this test
        Ball ball = new Ball(100, 100, -2, 0, 1, app.ballImage[1], app);

        for (int i = 0; i < 20; i++) {
            ball.update(levelObjects, new ArrayList<>());
        }
        assertEquals(2, ball.vx, "Ball vx should reverse after hitting OneWay tile from opposite direction");
    }
}
