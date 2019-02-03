import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class Cell extends JPanel {

	// Indicate the row and column of this cell in the board
	private int row;
	private int column;

	// Token used for this cell
	private char token = ' ';

	private ConnectFourClient parent;

	/**
	 * Creates the cell
	 * @param row
	 * @param column
	 * @param gui
	 */
	public Cell(int row, int column, ConnectFourClient gui) {

		this.row = row;
		this.column = column;
		this.parent = gui;

		setBorder(new LineBorder(Color.black, 1)); // Set cell's border
		addMouseListener(new ClickListener()); // Register listener
		setBackground(Color.ORANGE);
	}

	/** Return token */
	public char getToken() {
		return token;
	}

	/** Set a new token */
	public void setToken(char c) {
		token = c;
		repaint();
	}

	/** Paint the cell */

	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		if (token == 'X') {
			g.setColor(Color.RED);
			g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);

		} else if (token == 'O') {
			g.setColor(Color.BLUE);
			g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
		}
	}

	/** Handle mouse click on a cell */
	private class ClickListener extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {

			// If cell is not occupied and the player has the turn

			if ((token == ' ') && parent.getMyTurn()) {

				setToken(parent.getMyToken()); // Set the player's token in the
												// cell
				parent.setMyTurn(false);
				parent.setRowSelected(row);
				parent.setColumnSelected(column);
				parent.setStatusMessage("Waiting for the other player to move");
				parent.setWaiting(false); // Just completed a successful move

			}
		}
	}
}
