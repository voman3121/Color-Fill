/*
 * GameController
 * ---------------
 * Acts as the core logic handler of the Flood Fill game.
 *
 * Responsibilities:
 * - Manages turn flow between Human and CPU
 * - Handles flood fill expansion using BFS
 * - Maintains score and win conditions
 * - Controls timer-based turn restriction
 * - Implements Greedy AI (CPU move selection)
 * - Implements Minimax-based Hint System (Review 2 enhancement)
 *
 * Design Choice:
 * UI and game logic are separated — UI handles display,
 * GameController handles decision-making and state updates.
 */


package swingprac;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.lang.module.ModuleDescriptor.Provides;

import javax.swing.Timer;
import java.util.List;
import java.util.Queue;
import java.util.stream.Gatherer.Integrator.Greedy;

// Constructor connects UI with logic and initializes listeners
// We keep all game control centralized here..
public class GameController {
	private ColorFillUI ui;
	private Grid grid;
	private Set<Cell> humanCells = new HashSet<>();
	private Set<Cell> CPUCells = new HashSet<>();
	private boolean humanTurn = true;
	private boolean gameOver = false;
	// Timer fields
	private Timer turnTimer;
	private int timeLeft = 10;
	// updating Score fields 
	private void updateScores() {
		ui.updateScores(humanCells.size(), CPUCells.size());
	}
	public GameController(ColorFillUI ui) {
		this.ui = ui;
		this.grid = ui.getGrid();
		// Attach all UI listeners
		difficultyListener();
		colorListener();
		initializeGame();
		newGameListener();
		resetGameListener();
		helpListener();
	}
	private void difficultyListener() {
		ui.getDifficulties().addActionListener(e -> {
			int difficulty = ui.getDifficulties().getSelectedIndex() + 1;
			int rows = 0, cols = 0;
			switch (difficulty) {
				case 1:
					rows = cols = 6;
					break;
				case 2:
					rows = cols = 8;
					break;
				case 3:
					rows = cols = 10;
					break;
			}
			ui.rebuildGrid(rows, cols, difficulty);
			grid = ui.getGrid();
			// Start the first game
			initializeGame();
			gameOver = false;
			colorListener();
		});
	}
	// Sets up initial ownership and starts timer
	// Called when game starts or resets
	private void initializeGame() {
		// Clear any previous state
		humanCells.clear();
		CPUCells.clear();
		Cell[][] cells = grid.getCells();
		int rows = cells.length;
		int cols = rows;
		humanCells.add(cells[0][0]);
		CPUCells.add(cells[rows - 1][cols - 1]);
		updateScores();

		// Create timer only once
			if (turnTimer == null) {
			turnTimer = new Timer(1000, e -> {
				timeLeft--;
				ui.updateTimer(timeLeft);
				if (timeLeft <= 0) {
					stopTurnTimer();
					// Skip human turn
					humanTurn = false;
					cpuTurn();
				}
			});
		}
		startTurnTimer();
	}
	private void startTurnTimer() {
		timeLeft = 15;
		ui.updateTimer(timeLeft);
		if (turnTimer != null)
			turnTimer.restart();
	}
	private void stopTurnTimer() {
		if (turnTimer != null)
			turnTimer.stop();
	}
	private void colorListener() {
		for (JButton colorBtn : ui.getColorButtons()) {
			colorBtn.addActionListener(e -> {
				if (!humanTurn || gameOver) {
					return;
				}
				Color chosenColor = colorBtn.getBackground();
				Color humanColor = humanCells.iterator().next().color;
				Color CPUColor = CPUCells.iterator().next().color;
				if (chosenColor.equals(humanColor) || chosenColor.equals(CPUColor)) {
					return;
				}
				humanTurn(chosenColor);
			});
		}
	}
	private void humanTurn(Color chosenColor) {
		stopTurnTimer();
		fillCells(humanCells, chosenColor, Owner.HUMAN);
		ui.updateGridUI(grid.getCells());
		updateScores();
		Owner winner = checkWin();
		if (winner != Owner.NONE) {
			ui.displayWinner(winner);
			gameOver = true;
			return;
		}
		humanTurn = false;
		cpuTurn();
	}

// 	 Expands the player's territory by capturing adjacent cells of the selected color.
	private void fillCells(Set<Cell> cells, Color chosenColor, Owner owner) {
		Queue<Cell> queue = new LinkedList<>(cells);
		Set<Cell> visited = new HashSet<>(cells);
		while (!queue.isEmpty()) {
			Cell current = queue.poll();
			current.color = chosenColor;
			    // Expand only into neutral cells with matching color
			for (Cell neighbour : current.neighbours) {
				if (!visited.contains(neighbour) && neighbour.owner == Owner.NONE
						&& neighbour.color.equals(chosenColor)) {
					visited.add(neighbour);
					queue.add(neighbour);
					cells.add(neighbour);
					neighbour.owner = owner;
				}
			}
		}
	}
	private Owner checkWin() {
		int totalCells = (int) Math.pow(grid.getCells().length, 2);
		if (humanCells.size() >= totalCells / 2) {
			return Owner.HUMAN;
		} else if (CPUCells.size() >= totalCells / 2) {
			return Owner.CPU;
		}
		return Owner.NONE;
	}

	// Calculates how many cells CPU would gain if it chooses a particular color.
	private int gain(Color color) {
		Queue<Cell> queue = new LinkedList<>(CPUCells);
		Set<Cell> visited = new HashSet<>(CPUCells);
		int gain = 0;
		while(!queue.isEmpty()) {
			Cell current = queue.poll();
			for(Cell neighbour: current.neighbours) {
				if(!visited.contains(neighbour) && neighbour.owner==Owner.NONE && neighbour.color.equals(color)) {
					queue.add(neighbour);
					visited.add(neighbour);
					gain++;
				}
			}
		}
		return gain;
	}

	// Greedy CPU strategy: Selects the color that results in maximum immediate gain.
	private Color bestColor() {
		Color bestColor = null;
		int maxGain = -1;
		Color humanColor = humanCells.iterator().next().color;
		Color CPUColor = CPUCells.iterator().next().color;
		for(Color color: grid.getColors()) {
			if(color.equals(humanColor) || color.equals(CPUColor)) {
				continue;
			}
			int gain = gain(color);
			if(gain>maxGain) {
				maxGain = gain;
				bestColor = color;
			}
		}
		return bestColor;
	}
	private void cpuTurn() {
		Color chosenColor = bestColor();
		if(chosenColor!=null) {
			fillCells(CPUCells,chosenColor,Owner.CPU);
			ui.updateGridUI(grid.getCells());
			updateScores();
		}
		Owner winner = checkWin();
		if(winner!=Owner.NONE) {
			ui.displayWinner(winner);
			gameOver = true;
			return;
		}
		humanTurn = true;
		startTurnTimer();
	}
	private void newGameListener() {
		ui.getNewGameBtn().addActionListener(e -> {
			int difficulty = ui.getDifficulties().getSelectedIndex() + 1;
			int rows = 0, cols = 0;
			switch (difficulty) {
				case 1:
					rows = cols = 6;
					break;
				case 2:
					rows = cols = 8;
					break;
				case 3:
					rows = cols = 10;
					break;
			}
			ui.rebuildGrid(rows, cols, difficulty);
			grid = ui.getGrid();
			newGame();
		});
	}
	private void newGame() {
		gameOver = false;
		humanTurn = true;
		initializeGame();
		colorListener();
		helpListener();
	}
	private void resetGameListener() {
		ui.getResetBtn().addActionListener(e -> {
			ui.resetGridUI(grid.getCells());
			grid = ui.getGrid();
			newGame();
		});
	}
	// ---- MINIMAX ----
// Provides strategic hint suggestions for the human.
// Depth-limited recursive adversarial search.

	private void helpListener() {
	    ui.getHelpBtn().addActionListener(e -> {
	        if (!humanTurn || gameOver) return;
	        Color suggested = minimaxBestColor(humanCells, CPUCells, 3);
	        ui.highlightSuggestedColor(suggested);
	    });
	}
// Tries all possible colors and selects the one that maximizes the human's advantage.
	private Color minimaxBestColor(Set<Cell> hCells, Set<Cell> cCells, int depth) {
	    Color humanColor = hCells.iterator().next().color;
	    Color cpuColor = cCells.iterator().next().color;
	    Color bestColor = null;
	    int bestScore = Integer.MIN_VALUE;

	    for (Color color : grid.getColors()) {
	        if (color.equals(humanColor) || color.equals(cpuColor)) continue;

	        Set<Cell> newHuman = new HashSet<>(hCells);
	        simulateFill(newHuman, color, Owner.HUMAN);
	        int score = minimax(newHuman, cCells, depth - 1, false);

	        if (score > bestScore) {
	            bestScore = score;
	            bestColor = color;
	        }
	    }
	    return bestColor;
	}
//  Minimax recursive evaluation.
//  
//   isMaximizing = true  -> Human turn
//   isMaximizing = false -> CPU turn
	private int minimax(Set<Cell> hCells, Set<Cell> cCells, int depth, boolean isMaximizing) {

    // Terminal condition: someone already won
    int totalCells = grid.getCells().length * grid.getCells().length;
    if (hCells.size() >= totalCells / 2) return 1000;   // strong win
    if (cCells.size() >= totalCells / 2) return -1000;  // strong loss

    // Depth limit
    if (depth == 0) return hCells.size() - cCells.size();

    Color humanColor = hCells.isEmpty() ? null : hCells.iterator().next().color;
    Color cpuColor   = cCells.isEmpty() ? null : cCells.iterator().next().color;

    boolean movePossible = false;

    if (isMaximizing) {
        int best = Integer.MIN_VALUE;

        for (Color color : grid.getColors()) {
            if (color.equals(humanColor) || color.equals(cpuColor)) continue;

            Set<Cell> newHuman = new HashSet<>(hCells);
            int before = newHuman.size();
            simulateFill(newHuman, color, Owner.HUMAN);

            // Skip if no expansion
            if (newHuman.size() == before) continue;

            movePossible = true;
            best = Math.max(best, minimax(newHuman, cCells, depth - 1, false));
        }

        // If no move possible, return evaluation
        if (!movePossible) return hCells.size() - cCells.size();

        return best;

    } else {
        int best = Integer.MAX_VALUE;

        for (Color color : grid.getColors()) {
            if (color.equals(humanColor) || color.equals(cpuColor)) continue;

            Set<Cell> newCPU = new HashSet<>(cCells);
            int before = newCPU.size();
            simulateFill(newCPU, color, Owner.CPU);

            if (newCPU.size() == before) continue;

            movePossible = true;
            best = Math.min(best, minimax(hCells, newCPU, depth - 1, true));
        }

        if (!movePossible) return hCells.size() - cCells.size();

        return best;
    }
}


// Simulates flood fill on a COPY of the player's cell set.
	private void simulateFill(Set<Cell> cells, Color chosenColor, Owner owner) {
	    Queue<Cell> queue = new LinkedList<>(cells);
	    Set<Cell> visited = new HashSet<>(cells);
	   
	    while (!queue.isEmpty()) {
	        Cell current = queue.poll();
	        for (Cell neighbour : current.neighbours) {
	            if (!visited.contains(neighbour)
	                    && neighbour.owner == Owner.NONE
	                    && neighbour.color.equals(chosenColor)) {
	                visited.add(neighbour);
	                queue.add(neighbour);
	                cells.add(neighbour);
	            }
	        }
	    }
	}
}