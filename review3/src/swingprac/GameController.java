package swingprac;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

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

	// --- UNDO SYSTEM FIELDS ---
	private Stack<GameState> undoStack = new Stack<>();
	private int undosRemaining = 3;

	// Snapshot class to capture game state
	private class GameState {
		Map<Cell, Color> cellColors = new HashMap<>();
		Map<Cell, Owner> cellOwners = new HashMap<>();
		Set<Cell> hCells;
		Set<Cell> cCells;
		boolean hTurn;

		GameState(Cell[][] grid, Set<Cell> h, Set<Cell> c, boolean turn) {
			for (Cell[] row : grid) {
				for (Cell cell : row) {
					cellColors.put(cell, cell.color);
					cellOwners.put(cell, cell.owner);
				}
			}
			this.hCells = new HashSet<>(h);
			this.cCells = new HashSet<>(c);
			this.hTurn = turn;
		}
	}
	// ---------------------------

	// Score fields updating
	private void updateScores() {
		ui.updateScores(humanCells.size(), CPUCells.size());
	}
	public GameController(ColorFillUI ui) {
		this.ui = ui;
		this.grid = ui.getGrid();
		difficultyListener();
		colorListener();
		initializeGame();
		newGameListener();
		resetGameListener();
		helpListener();
		undoListener(); // New Listener
	}

	private void undoListener() {
		if (ui.getUndoBtn() != null) {
			ui.getUndoBtn().addActionListener(e -> undoMove());
		}
	}

	private void undoMove() {
		// Only allow undo if stack has a state, match isn't over, and limit hasn't been reached
		if (undoStack.isEmpty() || gameOver || undosRemaining <= 0) {
			return;
		}

		GameState state = undoStack.pop(); // Clears the stack so they can't undo again this turn
		undosRemaining--;
		
		// Restore grid
		for (Map.Entry<Cell, Color> entry : state.cellColors.entrySet()) {
			Cell c = entry.getKey();
			c.color = entry.getValue();
			c.owner = state.cellOwners.get(c);
		}

		this.humanCells = state.hCells;
		this.CPUCells = state.cCells;
		this.humanTurn = state.hTurn;

		ui.updateGridUI(grid.getCells());
		updateScores();
		ui.clearHighlight();
		startTurnTimer();
		
		// Optional: Update UI with remaining count if your UI supports it
		// ui.setUndoText("Undos: " + undosRemaining); 
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
			initializeGame();
			gameOver = false;
			colorListener();
		});
	}
	private void initializeGame() {
		humanCells.clear();
		CPUCells.clear();
		undoStack.clear();    // Reset undo history
		undosRemaining = 3;   // Reset match limit
		Cell[][] cells = grid.getCells();
		int rows = cells.length;
		int cols = rows;
		humanCells.add(cells[0][0]);
		CPUCells.add(cells[rows - 1][cols - 1]);
		updateScores();
		// Initialize timer if not already
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

				// SAVE STATE BEFORE THE MOVE
				// Clear stack first to ensure only ONE undo per turn is possible
				undoStack.clear(); 
				undoStack.push(new GameState(grid.getCells(), humanCells, CPUCells, humanTurn));

				humanTurn(chosenColor);
			});
		}
	}
	// Human turn
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
	// Fill cells method for updating the selected color for cells
	private void fillCells(Set<Cell> cells, Color chosenColor, Owner owner) {
		Queue<Cell> queue = new LinkedList<>(cells);
		Set<Cell> visited = new HashSet<>(cells);
		while (!queue.isEmpty()) {
			Cell current = queue.poll();
			current.color = chosenColor;
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
	// method to check cells conquered for each color
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
	// picks best color for the cpu
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
		ui.clearHighlight();
	}
	private void resetGameListener() {
		ui.getResetBtn().addActionListener(e -> {
			ui.resetGridUI(grid.getCells());
			grid = ui.getGrid();
			newGame();
		});
	}
	// ---- MINIMAX HELP SYSTEM ----

	private void helpListener() {
		ui.getHelpBtn().addActionListener(e -> {
			if (!humanTurn || gameOver) return;
			Color suggested = minimaxBestColor(humanCells, CPUCells, 3);
			ui.highlightSuggestedColor(suggested);
		});
	}
	
	private Color minimaxBestColor(Set<Cell> hCells, Set<Cell> cCells, int depth) {
		Color humanColor = hCells.iterator().next().color;
		Color cpuColor = cCells.iterator().next().color;
		Color bestColor = null;
		int bestScore = Integer.MIN_VALUE;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		
		for (Color color : grid.getColors()) {
			if (color.equals(humanColor) || color.equals(cpuColor)) continue;
			
			Set<Cell> newHuman = new HashSet<>(hCells);
			simulateFill(newHuman, color, Owner.HUMAN);
			int score = minimax(newHuman, cCells, depth - 1, alpha, beta, false);
			
			if (score > bestScore) {
				bestScore = score;
				bestColor = color;
			}
			alpha = Math.max(alpha, bestScore);
		}
		return bestColor;
	}
	
	private int minimax(Set<Cell> hCells, Set<Cell> cCells, int depth, int alpha, int beta, boolean isMaximizing) {
		int totalCells = grid.getCells().length * grid.getCells().length;
		if (hCells.size() >= totalCells / 2) return 1000;
		if (cCells.size() >= totalCells / 2) return -1000;
		if (depth == 0) return hCells.size() - cCells.size();
		
		Color humanColor = hCells.isEmpty() ? null : hCells.iterator().next().color;
		Color cpuColor = cCells.isEmpty() ? null : cCells.iterator().next().color;
		
		if (isMaximizing) {
			int maxEval = Integer.MIN_VALUE;
			for (Color color : grid.getColors()) {
				if (color.equals(humanColor) || color.equals(cpuColor)) continue;
				
				Set<Cell> newHuman = new HashSet<>(hCells);
				int before = newHuman.size();
				simulateFill(newHuman, color, Owner.HUMAN);
				if (newHuman.size() == before) continue;
				
				int eval = minimax(newHuman, cCells, depth - 1, alpha, beta, false);
				maxEval = Math.max(maxEval, eval);
				alpha = Math.max(alpha, eval);
				if (beta <= alpha) break; // Prune
			}
			return maxEval == Integer.MIN_VALUE ? hCells.size() - cCells.size() : maxEval;
		} else {
			int minEval = Integer.MAX_VALUE;
			for (Color color : grid.getColors()) {
				if (color.equals(humanColor) || color.equals(cpuColor)) continue;
				
				Set<Cell> newCPU = new HashSet<>(cCells);
				int before = newCPU.size();
				simulateFill(newCPU, color, Owner.CPU);
				if (newCPU.size() == before) continue;
				
				int eval = minimax(hCells, newCPU, depth - 1, alpha, beta, true);
				minEval = Math.min(minEval, eval);
				beta = Math.min(beta, eval);
				if (beta <= alpha) break; // Prune
			}
			return minEval == Integer.MAX_VALUE ? hCells.size() - cCells.size() : minEval;
		}
	}
	
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