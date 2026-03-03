/*
 * ColorFillUI
 * -----------
 * Handles everything related to the visual interface.
 * This class builds the frame, grid, buttons, and top control panel.
 * It does not contain game logic — it only reflects the current state.
 *
 * Review 2 additions:
 * - Score display (Human vs CPU)
 * - Turn timer display
 * - Help button for Minimax suggestion
 * - Highlight system for suggested move
 */

package swingprac;
import javax.swing.*;
import java.awt.*;

public class ColorFillUI {
	private JFrame frame;
	private JPanel gridPanel;
	private JPanel colorPanel;
	private JPanel optionsPanel;
	private JComboBox<String> difficultyBox;
	private JButton newGameBtn;
	private JButton resetBtn;
	private JButton[][] buttons;
	private JButton[] colorButtons;
	private JButton helpBtn;
	private JButton lastHighlighted = null;
	private Grid grid;

	public ColorFillUI() {
		// frame creation
		frame = new JFrame("Color Fill");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(650, 600);
		frame.setLayout(new BorderLayout());
		// default grid creation
		int rows = 8;
		int cols = 8;
		// grid panel creation
		gridPanelCreate(rows, cols, 2);
		colorPanelCreate();
		optionsPanelCreate();
		frame.add(gridPanel, BorderLayout.CENTER);
		frame.add(colorPanel, BorderLayout.SOUTH);
		frame.add(optionsPanel, BorderLayout.NORTH);
		frame.setVisible(true);
	}
	// Builds the grid visually

	private void gridPanelCreate(int rows, int cols, int difficulty) {
		gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(rows, cols));
		buttons = new JButton[rows][cols];
		grid = new Grid(rows, cols, difficulty);
		Cell[][] cells = grid.getCells();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				JButton button = new JButton();
				button.setBackground(cells[i][j].color);
				button.setOpaque(true);
				button.setBorder(BorderFactory.createLineBorder(Color.black));
				button.setEnabled(false);
				button.setFont(new Font("Arial", Font.BOLD, 20));
				buttons[i][j] = button;
				gridPanel.add(button);
			}
		}
			// Set starting positions
		cells[0][0].owner = Owner.HUMAN;
		buttons[0][0].setText("H");
		cells[rows - 1][cols - 1].owner = Owner.CPU;
		buttons[rows - 1][cols - 1].setText("C");
	}

	// Creates selectable color buttons
	private void colorPanelCreate() {
		colorPanel = new JPanel();
		Color[] colors = grid.getColors();
		colorButtons = new JButton[colors.length];
		int i = 0;
		for (Color color : colors) {
			JButton colorBtn = new JButton();
			colorBtn.setBackground(color);
			colorBtn.setOpaque(true);
			colorBtn.setBorder(BorderFactory.createLineBorder(Color.black));
			colorBtn.setPreferredSize(new Dimension(50, 50));
			colorPanel.add(colorBtn);
			colorButtons[i++] = (colorBtn);
		}
	}

	// Creates difficulty dropdown + control buttons + Review 2 elements
	private void optionsPanelCreate() {
		optionsPanel = new JPanel();
		String[] difficulties = { "Easy", "Medium", "Difficult" };
		difficultyBox = new JComboBox<>(difficulties);
		difficultyBox.setSelectedIndex(1);
		optionsPanel.add(new JLabel("Difficulty: "));
		optionsPanel.add(difficultyBox);
		newGameBtn = new JButton("New Game");
		resetBtn = new JButton("Reset");
		optionsPanel.add(newGameBtn);
		optionsPanel.add(resetBtn);

		// Initialize labels
		humanScoreLabel = new JLabel("Human: 1");
		cpuScoreLabel = new JLabel("CPU: 1");
		timerLabel = new JLabel("Time: 10s");
		helpBtn = new JButton("Help");

		// Add some spacing
		optionsPanel.add(Box.createHorizontalStrut(10));
		optionsPanel.add(humanScoreLabel);
		optionsPanel.add(Box.createHorizontalStrut(10));
		optionsPanel.add(cpuScoreLabel);
		optionsPanel.add(Box.createHorizontalStrut(10));
		optionsPanel.add(timerLabel);
		optionsPanel.add(Box.createHorizontalStrut(10));
		optionsPanel.add(helpBtn);
	}

	protected void rebuildGrid(int rows, int cols, int difficulty) {
		frame.remove(gridPanel);
		frame.remove(colorPanel);
		gridPanelCreate(rows, cols, difficulty);
		colorPanelCreate();
		frame.add(gridPanel, BorderLayout.CENTER);
		frame.add(colorPanel, BorderLayout.SOUTH);
		frame.revalidate();
		frame.repaint();
	}

	// Updates cell colors and ownership text
	protected void updateGridUI(Cell[][] cells) {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells.length; j++) {
				Cell cell = cells[i][j];
				buttons[i][j].setBackground(cell.color);
				if (cell.owner == Owner.HUMAN) {
					buttons[i][j].setText("H");
				} else if (cell.owner == Owner.CPU) {
					buttons[i][j].setText("C");
				}
			}
		}
	}

	protected void displayWinner(Owner winner) {
		if (winner == Owner.HUMAN) {
			JOptionPane.showMessageDialog(frame, "Human Wins!");
		} else if (winner == Owner.CPU) {
			JOptionPane.showMessageDialog(frame, "CPU Wins!");
		}
	}

	// Resets grid to initial state
	protected void resetGridUI(Cell[][] cells) {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells.length; j++) {
				cells[i][j].color = cells[i][j].initialColor;
				cells[i][j].owner = Owner.NONE;
				buttons[i][j].setText("");
			}
		}
		cells[0][0].owner = Owner.HUMAN;
		buttons[0][0].setText("H");
		cells[cells.length - 1][cells.length - 1].owner = Owner.CPU;
		buttons[cells.length - 1][cells.length - 1].setText("C");
		updateGridUI(cells);
	}

	public Grid getGrid() {
		return grid;
	}

	public JButton getNewGameBtn() {
		return newGameBtn;
	}

	public JButton getResetBtn() {
		return resetBtn;
	}

	public JComboBox<String> getDifficulties() {
		return difficultyBox;
	}

	public JButton[] getColorButtons() {
		return colorButtons;
	}

	private JLabel humanScoreLabel;
	private JLabel cpuScoreLabel;
	private JLabel timerLabel;

	public void updateScores(int humanScore, int cpuScore) {
		humanScoreLabel.setText("Human: " + humanScore);
		cpuScoreLabel.setText("CPU: " + cpuScore);
	}

	public void updateTimer(int seconds) {
		timerLabel.setText("Time: " + seconds + "s");
		if (seconds <= 3) {
			timerLabel.setForeground(Color.RED);
		} else {
			timerLabel.setForeground(Color.BLACK);
		}
	}
	
	public JButton getHelpBtn() { return helpBtn; }

	// Highlights suggested color from Minimax
	public void highlightSuggestedColor(Color suggested) {
	    clearHighlight(); // clear any previous
	    for (JButton btn : colorButtons) {
	        if (btn.getBackground().equals(suggested)) {
	            btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
	            lastHighlighted = btn;
	            break;
	        }
	    }
	}

	public void clearHighlight() {
	    if (lastHighlighted != null) {
	        lastHighlighted.setBorder(BorderFactory.createLineBorder(Color.black));
	        lastHighlighted = null;
	    }
	}
}