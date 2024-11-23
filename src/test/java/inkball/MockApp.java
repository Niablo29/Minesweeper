package inkball;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.*;
import java.util.*;

public class MockApp extends App {
    // Use the static constants from App
    public PImage[] ballImage = new PImage[5];
    public PImage[] wallImage = new PImage[5];
    public PImage[] holeImage = new PImage[5];
    public int score;
    public float scoreIncreaseModifier;
    public float scoreDecreaseModifier;
    public List<Ball> balls = new ArrayList<>();
    public Map<String, Integer> scoreIncreaseMap = new HashMap<>();
    public Map<String, Integer> scoreDecreaseMap = new HashMap<>();
    public Queue<Integer> upcomingBallsQueue = new LinkedList<>();
    public Object[][] levelObjects = new Object[20][18];

    public MockApp() {
        // Initialize any required fields
        for (int i = 0; i < ballImage.length; i++) {
            ballImage[i] = new PImage(32, 32); // Dummy images
        }
        for (int i = 0; i < wallImage.length; i++) {
            wallImage[i] = new PImage(32, 32); // Dummy images
        }
        for (int i = 0; i < holeImage.length; i++) {
            holeImage[i] = new PImage(32, 32); // Dummy images
        }
    }

    @Override
    public void image(PImage img, float x, float y, float width, float height) {
        // Do nothing or provide minimal implementation
    }

    @Override
    public void updateBalls() {
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            if (!ball.isCaptured()) {
                ball.update(levelObjects, new ArrayList<>()); // Pass empty lines
            }

            // Check if the ball has been captured by a hole
            if (ball.isCaptured()) {
                // Update score based on whether the ball entered the correct hole
                String colorName = getColorName(ball.getColor());
                if (ball.enteredCorrectHole()) {
                    int increaseScore = (int) (scoreIncreaseMap.get(colorName) * scoreIncreaseModifier);
                    score += increaseScore;  // Increase score
                } else {
                    int decreaseScore = (int) (scoreDecreaseMap.get(colorName) * scoreDecreaseModifier);
                    score -= decreaseScore;  // Decrease score
                }
                // Remove the ball from play
                ballIterator.remove();
            }
        }
    }


    public String getColorName(int colorIndex) {
        switch (colorIndex) {
            case 0:
                return "grey";
            case 1:
                return "orange";
            case 2:
                return "blue";
            case 3:
                return "green";
            case 4:
                return "yellow";
            default:
                return "grey";
        }
    }
}

