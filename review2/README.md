# Color Fill Game - Review 2

This version of the project introduces several significant enhancements and rigorous algorithmic implementations beyond the initial version.

## Key Changes & Improvements from Review 1

- **Sorting Implementation (Divide & Conquer)**: 
  - Integrated **Merge Sort** to rank color choices for the CPU. 
  - This replaces the simple loop-based max search with a formal Divide and Conquer strategy for better logic organization.
- **Live Scoring**:
  - Real-time score tracking for both Human and CPU displayed at the top of the window.
  - Scores update immediately after every move.
- **Turn Timer**:
  - Added a **10-second countdown** for the human player.
  - If the timer reaches zero, the move is automatically skipped, adding a speed challenge to the game.

## Project Structure

```
review2/
├── src/swingprac/
│   ├── Cell.java           - Represents a single grid cell
│   ├── Grid.java           - Manages the grid and cell initialization
│   ├── ColorFillUI.java    - Creates the graphical user interface
│   ├── GameController.java - Handles game logic and turn management
│   ├── Owner.java          - Enum for player ownership
│   └── HelloSwing.java     - Entry point of the application
└── bin/swingprac/          - Compiled bytecode
```

## Technologies Used

- **Language**: Java
- **GUI Framework**: Java Swing
- **Graphics**: AWT (Abstract Window Toolkit)

## Installation & Running

1. Ensure you have Java installed (Java 8 or higher recommended)
2. Compile the project:
   ```bash
   javac -d bin src/swingprac/*.java
   ```
3. Run the application:
   ```bash
   java -cp bin swingprac.HelloSwing
   ```

## Game Controls

- **Difficulty Dropdown**: Select Easy, Medium, or Hard
- **Color Buttons**: Click to select a color for your turn
- **New Game**: Start a fresh game with the current difficulty
- **Reset**: Reset the current game

## Algorithm Insights

The game uses graph algorithms to:
- Track cell adjacency through a neighbor-based system
- Efficiently compute connected components of the same color
- Implement game logic for both human and CPU players
