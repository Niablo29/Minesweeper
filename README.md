# **Minesweeper Game**

## **Overview**
This is a Minesweeper game implemented in Java using the **Processing** library for graphics and **Gradle** as the dependency manager. The game features a grid of tiles, where players reveal tiles to avoid mines while marking suspected mine locations. 

The objective is to reveal all non-mine tiles without triggering any mines. If a mine is clicked, all mines explode in a cascading animation, and the game ends.

---

## **Features**
- **Dynamic Gameplay**:
  - Randomly placed mines on an 18x27 grid.
  - Ability to flag suspected mines and reveal safe tiles.
  - Progressive mine explosion animation if a mine is clicked.
  - Adjacent mine counts displayed with unique colors.

- **Player Controls**:
  - **Left-click**: Reveal a tile.
  - **Right-click**: Flag or unflag a tile.
  - **`R` Key**: Restart the game.

- **Timer**: Displays elapsed time in the top-right corner.
- **Win/Loss States**:
  - "You Win!" message when all safe tiles are revealed.
  - "You Lost!" message when a mine is clicked.

---

## **Setup Instructions**

### **Prerequisites**
- **Java 8** or a compatible version.
- **Gradle** installed.
- A terminal or IDE such as IntelliJ IDEA or VS Code with Gradle support.

### **How to Run**
1. Clone the repository:
   ```bash
   git clone https://github.com/YourUsername/Minesweeper.git
   cd Minesweeper
   ```
2. Build the project:
   ```bash
   gradle build
   ```
3. Run the game:
   ```bash
   gradle run --args="NUMBER_OF_MINES"
   ```
   Replace `NUMBER_OF_MINES` with the desired number of mines (default is 100).

---

## **Gameplay Details**
1. **Objective**:
   - Reveal all non-mine tiles without triggering any mines.
2. **Tile States**:
   - **Hidden**: Blue tiles represent hidden states.
   - **Revealed**: Shows either the mine count or remains blank if no adjacent mines exist.
   - **Flagged**: Tiles suspected to contain mines.
3. **Mine Explosion**:
   - Clicking a mine starts a cascading explosion animation.
4. **Winning/Losing**:
   - Reveal all non-mine tiles to win.
   - Clicking a mine results in a loss.

---

## **Resources**
The game uses sprites located in `src/main/resources/minesweeper/`:
- `mine0.png` to `mine9.png`: Used for mine explosion animations.
- `flag.png`: Indicates flagged tiles.
- `tile.png`, `tile1.png`, `tile2.png`: Different states of tiles.

---

## **Project Structure**
- **`src/main/java`**:
  - `App.java`: Main entry point of the game.
  - `Tile.java`: Represents individual tiles on the grid.
- **`src/main/resources`**: Contains game assets such as images.
- **`build.gradle`**: Gradle configuration file.

---

## **How to Contribute**
Feel free to fork this repository and contribute by adding new features, improving performance, or fixing bugs. Submit a pull request for review.
