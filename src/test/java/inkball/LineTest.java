package inkball;

import org.junit.jupiter.api.*;
import processing.core.PApplet;
import processing.core.PVector;

import static org.junit.jupiter.api.Assertions.*;

public class LineTest {

    private Line line;
    private PApplet app;

    @BeforeEach
    public void setup() {
        app = new PApplet();
        line = new Line(app);
    }

    @Test
    public void testLineCreation() {
        // Test that a new line is created successfully
        assertNotNull(line.getPoints(), "Line should have a list of points");
        assertTrue(line.getPoints().isEmpty(), "New line should start with no points");
    }

    @Test
    public void testAddingPointsToLine() {
        // Verify that points are added to the line in order
        line.addPoint(10, 20);
        line.addPoint(30, 40);
        assertEquals(2, line.getPoints().size(), "Line should have two points after adding");
        PVector firstPoint = line.getPoints().get(0);
        PVector secondPoint = line.getPoints().get(1);
        assertEquals(10, firstPoint.x, "First point x should be 10");
        assertEquals(20, firstPoint.y, "First point y should be 20");
        assertEquals(30, secondPoint.x, "Second point x should be 30");
        assertEquals(40, secondPoint.y, "Second point y should be 40");
    }

    @Test
    public void testLineDataIntegrity() {
        // Ensure the line maintains correct data after multiple operations
        for (int i = 0; i < 100; i++) {
            line.addPoint(i, i * 2);
        }
        assertEquals(100, line.getPoints().size(), "Line should have 100 points");
        for (int i = 0; i < 100; i++) {
            PVector point = line.getPoints().get(i);
            assertEquals(i, point.x, "Point x should match index");
            assertEquals(i * 2, point.y, "Point y should be double the index");
        }
    }
}
