package inkball;

import org.junit.jupiter.api.*;

import inkball.App;
import processing.core.PVector;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class AppTest {

    public App app;

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
    }


    @Test
    public void testSetupInitialization() {
        // Test that the setup method initializes game configurations correctly
        assertNotNull(app.ballImage, "Ball images should be initialized");
        assertNotNull(app.holeImage, "Hole images should be initialized");
        assertNotNull(app.wallImage, "Wall images should be initialized");
        assertNotNull(app.config, "Config should be loaded");
        assertNotNull(app.levelsConfig, "Levels config should be loaded");
    }

    @Test
    public void testLoadLevelWithValidIndex() {
        // Test loading a level with a valid index
        app.loadLevel(0);
        assertEquals(App.GameState.PLAYING, app.gameState, "Game state should be PLAYING after loading a level");
    }

    @Test
    public void testLoadLevelWithInvalidIndex() {
        // Test behavior when loading a level with an invalid index
        app.setup();
        app.loadLevel(999);
        assertEquals(App.GameState.GAME_OVER, app.gameState, "Game state should be GAME_OVER when no levels are left");
    }

    @Test
    public void testKeyPressRestart() {
        // Simulate pressing 'r' to restart and verify the game resets
        app.gameState = App.GameState.TIME_UP;
        KeyEvent keyEvent = new KeyEvent(null, System.currentTimeMillis(), 0, 0, 'r', 0);
        app.keyPressed(keyEvent);
        assertEquals(App.GameState.PLAYING, app.gameState, "Game state should be PLAYING after pressing 'r'");
    }

    @Test
    public void testPauseAndResume() {
        app.isPaused = false;
        KeyEvent keyEvent = new KeyEvent(app, System.currentTimeMillis(), KeyEvent.PRESS, 0, ' ', ' ');
        app.keyPressed(keyEvent);
        assertTrue(app.isPaused, "Game should be paused after pressing spacebar");

        // Simulate pressing spacebar again to unpause
        app.keyPressed(keyEvent);
        assertFalse(app.isPaused, "Game should resume after pressing spacebar again");
    }



    @Test
    public void testGameStateTransitions() {
        // Test transitions between different game states
        app.gameState = App.GameState.PLAYING;
        app.timeRemaining = 0;
        app.draw();
        assertEquals(App.GameState.TIME_UP, app.gameState, "Game state should be TIME_UP when time runs out");

        app.loadLevel(0);
        app.balls.clear();
        app.upcomingBallsQueue.clear();
        app.draw();
        assertEquals(App.GameState.LEVEL_COMPLETE, app.gameState, "Game state should be LEVEL_COMPLETE when level is finished");
    }

    @Test
    public void testTimeUpState() {
        // Verify behavior when the level time runs out
        app.timeRemaining = 0;
        app.draw();
        assertEquals(App.GameState.TIME_UP, app.gameState, "Game state should be TIME_UP when time runs out");
        // Ensure balls stop moving
        int ballCountBefore = app.balls.size();
        app.updateBalls();
        int ballCountAfter = app.balls.size();
        assertEquals(ballCountBefore, ballCountAfter, "Balls should not move when game state is TIME_UP");
    }

    @Test
    public void testMouseInputWhenTimeUp() {
        // Ensure that no lines can be drawn when time is up
        app.gameState = App.GameState.TIME_UP;
        MouseEvent pressEvent = new MouseEvent(app, System.currentTimeMillis(), MouseEvent.PRESS, 0, 100, 100, app.LEFT, 1);
        app.mousePressed(pressEvent);
        assertNull(app.currentLine, "No line should be started when game state is TIME_UP");
    }

    @Test
    public void testDistancePointToSegmentSimple() {
        // Test Case 1: Point projection falls on the segment
        // Segment from (0,0) to (6,8), point at (3,4) which lies on the segment
        float distanceOnSegment = app.distancePointToSegment(3, 4, 0, 0, 6, 8);
        assertEquals(0.0f, distanceOnSegment, 0.001f, "Point is on the segment, distance should be 0");

        // Test Case 2: Point projection falls beyond the segment end
        // Segment from (0,0) to (6,8), point at (7,9) which is beyond (6,8)
        float distanceBeyondEnd = app.distancePointToSegment(7, 9, 0, 0, 6, 8);
        // Expected distance: sqrt((7-6)^2 + (9-8)^2) = sqrt(1 + 1) = 1.414
        assertEquals(1.414f, distanceBeyondEnd, 0.001f, "Point beyond the segment end, distance should be approximately 1.414");
    }

    @Test
    public void testLineContainsPointSimple() {
        // Test Case 1: Point lies exactly on one of the line's segments
        // Create a Line with two points forming a horizontal segment from (0,0) to (10,0)
        Line horizontalLine = new Line(app);
        
        // Add points to form a horizontal segment from (0,0) to (10,0)
        horizontalLine.addPoint(0, 0);
        horizontalLine.addPoint(10, 0);
        // Point exactly on the segment
        boolean containsPointOnSegment = app.lineContainsPoint(horizontalLine, 5, 0);
        assertTrue(containsPointOnSegment, "Point exactly on the horizontal segment should be contained");

        // Test Case 2: Point lies near the line segment within threshold
        // Point (5, 3) is 3 units above the horizontal segment, threshold is 5
        boolean containsPointNearSegment = app.lineContainsPoint(horizontalLine, 5, 3);
        assertTrue(containsPointNearSegment, "Point near the horizontal segment within threshold should be contained");

        // Test Case 3: Point lies outside the threshold
        // Point (5, 6) is 6 units above the horizontal segment, threshold is 5
        boolean containsPointOutsideThreshold = app.lineContainsPoint(horizontalLine, 5, 6);
        assertFalse(containsPointOutsideThreshold, "Point near the horizontal segment outside threshold should not be contained");
    }
    
}
