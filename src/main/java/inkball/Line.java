package inkball;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class Line {
    public List<PVector> points;  // List of points that make up the line
    public PApplet app;

    public Line(PApplet app) {
        this.app = app;
        this.points = new ArrayList<>();
    }

    // Add a point to the line
    public void addPoint(float x, float y) {
        points.add(new PVector(x, y));
    }

    // Render the line
    public void render() {
        app.stroke(0);  // Set line color (black)
        app.strokeWeight(10);  // Set line thickness
        app.noFill();
        app.beginShape();
        for (PVector point : points) {
            app.vertex(point.x, point.y + 64);
        }
        app.endShape();
    }

    // Get the list of points
    public List<PVector> getPoints() {
        return points;
    }
}
