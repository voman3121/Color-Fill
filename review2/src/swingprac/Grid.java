/*
 * Grid
 * ----
 * Responsible for creating and managing the game board.
 * It initializes cells with random colors based on difficulty
 * and connects each cell to its neighbouring cells.
 */
package swingprac;
import java.util.*;
import java.awt.Color;
public class Grid {
	private int rows, cols;
	private Cell[][] cells;
	private Color[] colors;
	private Random rand = new Random();
	public Grid(int rows, int cols, int difficulty) {
		this.rows = rows;
		this.cols = cols;
		this.cells = new Cell[rows][cols];
		// Set available colors based on difficulty
		switch (difficulty) {
		case 1:
			colors = new Color[] {Color.red, Color.yellow, Color.green, Color.pink};
			break;
		case 2:
			colors = new Color[] {Color.RED, Color.YELLOW, Color.GREEN, Color.PINK, Color.CYAN};
			break;
		case 3:
			colors = new Color[] {Color.RED, Color.YELLOW, Color.GREEN, Color.PINK, Color.CYAN, Color.magenta, Color.orange};
		}
		initializeCells(); 
		connectCells();
	}

		// Assigns each cell a random color
	private void initializeCells() {
		for(int i=0;i<rows;i++) {
			for(int j=0;j<cols;j++) {
				cells[i][j] = new Cell(i,j,colors[rand.nextInt(colors.length)]);
			}
		}
	}

		// Connects each cell to its up/down/left/right neighbours
	private void connectCells() {
		for(int i=0;i<rows;i++) {
			for(int j=0;j<cols;j++) {
				if(i>0) {
					cells[i][j].addNeighbour(cells[i-1][j]);
				}
				if(i<rows-1) {
					cells[i][j].addNeighbour(cells[i+1][j]);
				}
				if(j>0) {
					cells[i][j].addNeighbour(cells[i][j-1]);
				}
				if(j<cols-1) {
					cells[i][j].addNeighbour(cells[i][j+1]);
				}
			}
		}
	}
	public Cell[][] getCells(){
		return cells;
	}
	public Color[] getColors() {
		return colors;
	}
}