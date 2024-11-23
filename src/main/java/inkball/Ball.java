package inkball;

import java.util.List;
import java.util.Iterator;
import processing.core.PImage;
import processing.core.PVector;

public class Ball {
    public float x, y, vx, vy;
    public int color;
    public PImage ballImage;
    public App app;
    public int ballSize = 32;
    public int cellSize = 32;
    public boolean isCaptured = false;
    public boolean enteredCorrectHole = false;
    public Hole capturingHole = null;
    public Object[][] levelObjects;

    public Ball(int x, int y, float vx, float vy, int color, PImage ballImage, App app) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.ballImage = ballImage;
        this.app = app;
        this.color = color;
    }

    public void update(Object[][] levelObjects, List<Line> lines) {
        this.levelObjects = levelObjects;  // Store levelObjects for use in other methods

        if (!isCaptured) {
            // Apply attraction force towards holes
            applyAttractionForce();

            // Move along X-axis and check for collisions
            x += vx;
            checkXCollisions();

            // Move along Y-axis and check for collisions
            y += vy;
            checkYCollisions();

            // Check for collisions with lines
            checkLineCollisions(lines);

            // Check if ball is entering a hole
            checkHoleCapture();
        } else {
            // Ball is being captured by a hole, shrink it
            shrinkTowardsHole();
        }
    }

    public void applyAttractionForce() {
        // Apply attraction force when near a hole
        for (int row = 0; row < levelObjects.length; row++) {
            for (int col = 0; col < levelObjects[0].length; col++) {
                Object obj = levelObjects[row][col];
                if (obj instanceof Hole) {
                    Hole hole = (Hole) obj;
                    float distance = distance(x + ballSize / 2, y + ballSize / 2, hole.getCenterX(), hole.getCenterY());
                    if (distance < 50) {  // Attraction radius
                        float fx = 0.007f * (hole.getCenterX() - (x + ballSize / 2));
                        float fy = 0.007f * (hole.getCenterY() - (y + ballSize / 2));
                        vx += fx;
                        vy += fy;
                    }
                }
            }
        }
    }

    public void checkHoleCapture() {
        for (int row = 0; row < levelObjects.length; row++) {
            for (int col = 0; col < levelObjects[0].length; col++) {
                Object obj = levelObjects[row][col];
                if (obj instanceof Hole) {
                    Hole hole = (Hole) obj;
                    float distance = distance(x + ballSize / 2, y + ballSize / 2, hole.getCenterX(), hole.getCenterY());
                    if (distance < hole.getCaptureRadius()) {
                        isCaptured = true;
                        capturingHole = hole;
                        enteredCorrectHole = (this.color == hole.getColor()) || this.color == 0; // Grey ball can enter any hole
                        return;
                    }
                }
            }
        }
    }

    public void shrinkTowardsHole() {
        if (capturingHole != null) {
            // Move towards the hole center
            x += (capturingHole.getCenterX() - (x + ballSize / 2)) * 0.1f;
            y += (capturingHole.getCenterY() - (y + ballSize / 2)) * 0.1f;
            // Shrink the ball
            ballSize *= 0.9f;
            if (ballSize < 5) {
                // Ball has been fully captured
                ballSize = 0;
            }
        }
    }

    public void checkXCollisions() {
        float leftEdge = x;
        float rightEdge = x + ballSize - 1;

        int leftCol = (int) (leftEdge / cellSize);
        int rightCol = (int) (rightEdge / cellSize);
        int topRow = (int) (y / cellSize);
        int bottomRow = (int) ((y + ballSize - 1) / cellSize);

        boolean collisionX = false;

        if (vx > 0) { // Moving right
            if (rightCol >= 0 && rightCol < levelObjects[0].length) {
                Object objTop = levelObjects[topRow][rightCol];
                Object objBottom = levelObjects[bottomRow][rightCol];
                if (objTop instanceof Wall || objBottom instanceof Wall) {
                    if (objTop instanceof OneWay || objBottom instanceof OneWay) {
                        // Allow passing from left to right through OneWay
                        // Skip collision handling
                    } else {
                        collisionX = true;
                        x = rightCol * cellSize - ballSize; // Align to wall
                        handleCollision(objTop != null ? objTop : objBottom);
                    }
                }
            } else if (rightEdge >= App.WIDTH) {
                // Collision with right screen edge
                collisionX = true;
                x = App.WIDTH - ballSize; // Align to screen edge
            }
        } else if (vx < 0) { // Moving left
            if (leftCol >= 0 && leftCol < levelObjects[0].length) {
                Object objTop = levelObjects[topRow][leftCol];
                Object objBottom = levelObjects[bottomRow][leftCol];
                if (objTop instanceof Wall || objBottom instanceof Wall || objTop instanceof OneWay || objBottom instanceof OneWay) {
                    collisionX = true;
                    x = (leftCol + 1) * cellSize; // Align to wall
                    handleCollision(objTop != null ? objTop : objBottom);
                }
            } else if (leftEdge <= 0) {
                // Collision with left screen edge
                collisionX = true;
                x = 0; // Align to screen edge
            }
        }

        if (collisionX) {
            vx = -vx;
        }
    }

    public void checkYCollisions() {
        float topEdge = y;
        float bottomEdge = y + ballSize - 1;

        int topRow = (int) (topEdge / cellSize);
        int bottomRow = (int) (bottomEdge / cellSize);
        int leftCol = (int) (x / cellSize);
        int rightCol = (int) ((x + ballSize - 1) / cellSize);

        boolean collisionY = false;

        if (vy > 0) { // Moving down
            if (bottomRow >= 0 && bottomRow < levelObjects.length) {
                Object objLeft = levelObjects[bottomRow][leftCol];
                Object objRight = levelObjects[bottomRow][rightCol];
                if (objLeft instanceof Wall || objRight instanceof Wall) {
                    collisionY = true;
                    y = bottomRow * cellSize - ballSize; // Align to wall
                    handleCollision(objLeft != null ? objLeft : objRight);
                }
            } else if (bottomEdge >= App.HEIGHT - App.TOPBAR) {
                // Collision with bottom screen edge
                collisionY = true;
                y = App.HEIGHT - App.TOPBAR - ballSize; // Align to screen edge
            }
        } else if (vy < 0) { // Moving up
            if (topRow >= 0 && topRow < levelObjects.length) {
                Object objLeft = levelObjects[topRow][leftCol];
                Object objRight = levelObjects[topRow][rightCol];
                if (objLeft instanceof Wall || objRight instanceof Wall) {
                    collisionY = true;
                    y = (topRow + 1) * cellSize; // Align to wall
                    handleCollision(objLeft != null ? objLeft : objRight);
                }
            } else if (topEdge <= 0) {
                // Collision with top screen edge
                collisionY = true;
                y = 0; // Align to screen edge
            }
        }

        if (collisionY) {
            vy = -vy;
        }
    }

    public void handleCollision(Object obj) {
        if (obj instanceof Wall) {
            if (obj instanceof ColoredWall) {
                ColoredWall coloredWall = (ColoredWall) obj;
                setColor(coloredWall.getColor());
            }
            // Walls are solid; no other action needed
        }
    }

    public void render() {
        app.image(ballImage, x, y + App.TOPBAR, ballSize, ballSize);
    }

    public void setColor(int newColor) {
        this.color = newColor;
        if (newColor >= 0 && newColor < app.ballImage.length) {
            this.ballImage = app.ballImage[newColor];
        }
    }

    public int getColor() {
        return this.color;
    }

    public void checkLineCollisions(List<Line> lines) {
        Iterator<Line> iterator = lines.iterator();
        while (iterator.hasNext()) {
            Line line = iterator.next();
            List<PVector> points = line.getPoints();

            // Check collision with each segment in the line
            for (int i = 0; i < points.size() - 1; i++) {
                PVector p1 = points.get(i);
                PVector p2 = points.get(i + 1);

                // Check for collision with the line segment between p1 and p2
                if (lineCircleCollision(p1.x, p1.y, p2.x, p2.y, x + ballSize / 2, y + ballSize / 2, ballSize / 2)) {
                    // Reflect the ball
                    reflectOffLine(p1, p2);

                    // Remove the line after collision
                    iterator.remove();
                    break;  // Exit the loop after collision
                }
            }
        }
    }

    public boolean lineCircleCollision(float x1, float y1, float x2, float y2, float cx, float cy, float radius) {
        // Find the closest point on the line segment to the circle center
        float dx = x2 - x1;
        float dy = y2 - y1;

        float lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) return false;  // Avoid division by zero for zero-length lines

        float t = ((cx - x1) * dx + (cy - y1) * dy) / lengthSquared;
        t = clamp(t, 0, 1);

        float closestX = x1 + t * dx;
        float closestY = y1 + t * dy;

        // Calculate the distance between the closest point and the circle center
        float distanceX = closestX - cx;
        float distanceY = closestY - cy;
        float distanceSquared = distanceX * distanceX + distanceY * distanceY;

        return distanceSquared <= radius * radius;
    }

    public void reflectOffLine(PVector p1, PVector p2) {
        // Calculate the normal vector of the line
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;

        // Normal vector components
        float nx = -dy;
        float ny = dx;

        // Normalize the normal vector
        float length = (float) Math.sqrt(nx * nx + ny * ny);
        if (length == 0) return;  // Avoid division by zero for zero-length lines

        nx /= length;
        ny /= length;

        // Choose the normal vector that points towards the ball
        float midX = (p1.x + p2.x) / 2;
        float midY = (p1.y + p2.y) / 2;
        float toBallX = x + ballSize / 2 - midX;
        float toBallY = y + ballSize / 2 - midY;
        float dot = toBallX * nx + toBallY * ny;
        if (dot < 0) {
            nx = -nx;
            ny = -ny;
        }

        // Reflection formula
        float dotProduct = vx * nx + vy * ny;
        vx = vx - 2 * dotProduct * nx;
        vy = vy - 2 * dotProduct * ny;
    }

    public boolean isCaptured() {
        return isCaptured && ballSize <= 5;
    }

    public boolean enteredCorrectHole() {
        return enteredCorrectHole;
    }

    // Utility methods to replace app methods

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
