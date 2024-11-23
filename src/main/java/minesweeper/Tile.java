package minesweeper;

import processing.core.PApplet;
import processing.core.PImage;

public class Tile {

    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMineCount;
    private int mineFrame;
    private boolean isHovering;

    public Tile() {
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMineCount = 0;
        this.mineFrame = 0;
        this.isHovering = false;
    }

    public void checkHovering(boolean isHovering) {
        this.isHovering = isHovering;
    }

    public boolean getIsMine() {
        return this.isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    public boolean getIsRevealed() {
        return this.isRevealed;
    }

    public void setRevealed(boolean isRevealed) {
        this.isRevealed = isRevealed;
    }

    public boolean getIsFlagged() {
        return this.isFlagged;
    }

    public void setFlagged(boolean isFlagged) {
        this.isFlagged = isFlagged;
    }

    public int getAdjCount() {
        return this.adjacentMineCount;
    }

    public void setAdjCount(int count) {
        this.adjacentMineCount = count;
    }

    public void draw(PApplet app, int x, int y, PImage[] mineImages, PImage[] tileImages) {
        int drawX = x * App.CELLSIZE;
        int drawY = y * App.CELLSIZE + App.TOPBAR;

        if (isRevealed) {
            if (isMine) {
                if (mineFrame < 9) {
                    app.image(mineImages[mineFrame], drawX, drawY);
                    mineFrame++;
                } else {
                    app.image(mineImages[9], drawX, drawY);
                }
            } else {
                app.image(tileImages[3], drawX, drawY);
                if (adjacentMineCount > 0) {
                    app.fill(App.mineCountColour[adjacentMineCount][0], App.mineCountColour[adjacentMineCount][1], App.mineCountColour[adjacentMineCount][2]);
                    app.textSize(24);
                    app.textAlign(PApplet.CENTER, PApplet.CENTER);
                    app.text(adjacentMineCount, drawX + App.CELLSIZE / 2, drawY + App.CELLSIZE / 2);
                }
            }
        } else {
            if (this.isHovering && this.isFlagged) {
                app.image(tileImages[2], drawX, drawY);
                app.image(tileImages[4], drawX, drawY);
            } else if(isHovering){
                app.image(tileImages[2], drawX, drawY);
            } else if (isFlagged) {
                app.image(tileImages[1], drawX, drawY);
                app.image(tileImages[4], drawX, drawY);
            } else {
                app.image(tileImages[1], drawX, drawY);
            }
        }
    }

    public void reset() {
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMineCount = 0;
        this.mineFrame = 0;
    }
}

