package inkball;

import org.junit.jupiter.api.*;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class BallTest {

    private MockApp app;
    private Ball ball;
    private PImage ballImage;

    @BeforeEach
    public void setup() {
        app = new MockApp();
        ballImage = app.ballImage[1]; // Use the dummy image from the mock app
        ball = new Ball(100, 100, 2, 2, 1, ballImage, app);
    }

    @BeforeAll
    public static void setUpClass() {
        App.WIDTH = 576;
        App.HEIGHT = 640;
    }


    @Test
    public void testBallMovement() {
        // Test that the ball updates its position based on velocity
        float initialX = ball.x;
        float initialY = ball.y;
        Object[][] levelObjects = new Object[20][18];
        ball.update(levelObjects, new ArrayList<>());
        assertEquals(initialX + ball.vx, ball.x, 0.001, "Ball x position should update by vx");
        assertEquals(initialY + ball.vy, ball.y, 0.001, "Ball y position should update by vy");
    }

    @Test
    public void testBallReflectionOffWalls() {
        // Test collision with walls and verify reflection logic
        Object[][] levelObjects = new Object[20][18];
        // Place a wall at position (3,3)
        Wall wall = new Wall(96, 96, 32, app.wallImage[0], app);
        levelObjects[3][3] = wall;
        ball.x = 96;
        ball.y = 90;
        ball.vx = 0;
        ball.vy = 2;
        ball.update(levelObjects, new ArrayList<>());
        assertEquals(-2, ball.vy, 0.001, "Ball vy should reverse after hitting a wall");
    }

    @Test
    public void testBallChangesColorOnColoredWallHit() {
        // Verify that the ball changes color when hitting a colored wall
        Object[][] levelObjects = new Object[20][18];
        // Place a colored wall at position (3,3)
        ColoredWall coloredWall = new ColoredWall(96, 96, 32, 2, app.wallImage[2], app);
        levelObjects[3][3] = coloredWall;
        ball.x = 96;
        ball.y = 90;
        ball.vx = 0;
        ball.vy = 2;
        ball.update(levelObjects, new ArrayList<>());
        assertEquals(2, ball.getColor(), "Ball color should change to the wall's color after collision");
    }

    @Test
    public void testBallCollisionWithPlayerLine() {
        // Test collision with a player-drawn line and line removal
        List<Line> lines = new ArrayList<>();
        Line line = new Line(app);
        line.addPoint(110, 100);
        line.addPoint(110, 200);
        lines.add(line);

        ball.x = 100;
        ball.y = 150;
        ball.vx = 2; // Moving towards the line
        ball.vy = 0;

        Object[][] levelObjects = new Object[20][18];
        app.levelObjects = levelObjects;

        ball.update(levelObjects, lines);

        assertTrue(lines.isEmpty(), "Line should be removed after collision with the ball");
        assertNotEquals(2, ball.vx, "Ball vx should change after collision with the line");
    }


    @Test
    public void testBallSizeReductionNearHole() {
        // Test that the ball size reduces as it approaches a hole
        Object[][] levelObjects = new Object[20][18];
        Hole hole = new Hole(100, 100, 32, 1, app.holeImage[1], app);
        levelObjects[3][3] = hole;
        app.levelObjects = levelObjects;

        ball.x = hole.getCenterX() - ball.ballSize / 2;
        ball.y = hole.getCenterY() - ball.ballSize / 2;
        ball.ballSize = 32;
        ball.isCaptured = false;

        // Run ball.update() multiple times to allow the ball to start shrinking
        for (int i = 0; i < 5; i++) {
            ball.update(levelObjects, new ArrayList<>());
        }

        assertTrue(ball.ballSize < 32, "Ball size should reduce as it approaches the hole");
    }


    @Test
    public void testBallCaptureByHole() {
        // Verify that the ball is captured when it reaches the hole's center
        Object[][] levelObjects = new Object[20][18];
        Hole hole = new Hole(100, 100, 32, 1, app.holeImage[1], app);
        levelObjects[3][3] = hole;
        ball.x = hole.getCenterX() - ball.ballSize / 2;
        ball.y = hole.getCenterY() - ball.ballSize / 2;
        // Update the ball multiple times to simulate shrinking
        for (int i = 0; i < 20; i++) {
            ball.update(levelObjects, new ArrayList<>());
        }
        assertTrue(ball.isCaptured(), "Ball should be marked as captured when at the hole's center");
    }

    @Test
    public void testScoreAdjustmentOnCorrectHoleEntry() {
        app.score = 0;
        app.scoreIncreaseMap.put("orange", 50);
        app.scoreIncreaseModifier = 1.0f;
    
        // Set up levelObjects with a hole of the same color as the ball
        Object[][] levelObjects = new Object[20][18];
        Hole hole = new Hole(100, 100, 32, 1, app.holeImage[1], app); // Orange hole
        levelObjects[3][3] = hole;
        app.levelObjects = levelObjects;
    
        ball.color = 1; // Orange ball
        ball.x = hole.getCenterX() - ball.ballSize / 2;
        ball.y = hole.getCenterY() - ball.ballSize / 2;
        app.balls = new ArrayList<>();
        app.balls.add(ball);
    
        // Simulate multiple updates to allow the ball to be captured
        for (int i = 0; i < 20; i++) {
            app.updateBalls();
        }
    
        assertEquals(50, app.score, "Score should increase by 50 when ball enters correct hole");
        assertTrue(app.balls.isEmpty(), "Ball should be removed from play after being captured");
    }
        

    @Test
    public void testScoreAdjustmentOnWrongHoleEntry() {
        app.score = 100;
        app.scoreDecreaseMap.put("orange", 25);
        app.scoreDecreaseModifier = 1.0f;

        // Set up levelObjects with a hole of a different color than the ball
        Object[][] levelObjects = new Object[20][18];
        Hole hole = new Hole(100, 100, 32, 2, app.holeImage[2], app); // Blue hole
        levelObjects[3][3] = hole;
        app.levelObjects = levelObjects;

        ball.color = 1; // Orange ball
        ball.x = hole.getCenterX() - ball.ballSize / 2;
        ball.y = hole.getCenterY() - ball.ballSize / 2;
        app.balls = new ArrayList<>();
        app.balls.add(ball);

        // Simulate multiple updates to allow the ball to be captured
        for (int i = 0; i < 20; i++) {
            app.updateBalls();
        }

        assertEquals(75, app.score, "Score should decrease by 25 when ball enters wrong hole");
        assertTrue(app.balls.isEmpty(), "Ball should be removed from play after being captured");
    }


    @Test
    public void testGreyBallInteractions() {
        // Test that grey balls can enter any hole successfully
        Object[][] levelObjects = new Object[20][18];
        Hole hole = new Hole(100, 100, 32, 2, app.holeImage[2], app); // Blue hole
        levelObjects[3][3] = hole;
        ball.color = 0; // Grey ball
        ball.x = hole.getCenterX() - ball.ballSize / 2;
        ball.y = hole.getCenterY() - ball.ballSize / 2;
        ball.update(levelObjects, new ArrayList<>());
        assertTrue(ball.enteredCorrectHole(), "Grey ball should be allowed to enter any hole successfully");
    }
}