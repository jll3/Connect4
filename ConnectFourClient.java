import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class ConnectFourClient extends JFrame implements Runnable,
		ConnectFourConstants {

	// Indicate whether the player has the turn
	private boolean myTurn = false;

	// The token for this player
	private char myToken = ' ';

	// The token for the other player
	private char otherToken = ' ';

	// Create and initialize cells
	private Cell[][] cell = new Cell[7][7];

	// Create and initialize a title label
	private JLabel jlblTitle = new JLabel();

	// Create and initialize a status label
	private JLabel jlblStatus = new JLabel();

	// Selected row and column by the current move
	private int rowSelected;
	private int columnSelected;

	// Input and output streams from/to server
	private DataInputStream fromServer;
	private DataOutputStream toServer;

	// Continue to play?
	private boolean continueToPlay = true;

	// Wait for the player to mark a cell
	private boolean waiting = true;


	// Host name or ip
	private String host = "localhost";
	

	/** 
	 * Sets up the GUI
	 * @param title
	 */
	public ConnectFourClient(String title) {

		super(title);

		// Panel p to hold cells
		JPanel p = new JPanel();


		p.setLayout(new GridLayout(8, 7, 0, 0));

		for (int i = 0; i < 7; i++)//Creates the grids
			for (int j = 0; j < 7; j++) {
				p.add(cell[i][j] = new Cell(i, j, this));
			}

		// Set properties for labels and borders for labels and panel
		p.setBorder(new LineBorder(Color.black, 1));

		jlblTitle.setHorizontalAlignment(JLabel.CENTER);
		jlblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
		jlblTitle.setBorder(new LineBorder(Color.black, 1));
		jlblStatus.setBorder(new LineBorder(Color.black, 1));

		// Place the panel and the labels to the frame

		setLayout(new BorderLayout()); // implicit anyway

		add(jlblTitle, BorderLayout.NORTH);
		add(p, BorderLayout.CENTER);
		add(jlblStatus, BorderLayout.SOUTH);

		// Connect to the server
		connectToServer();
	}

	/**
	 * Connects to the server to allow multiplayer
	 */
	private void connectToServer() {

		try {
			// Create a socket to connect to the server

			Socket socket;

			socket = new Socket(host, 8000);

			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());

			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (Exception ex) {
			System.err.println(ex);
		}

		// Control the game on a separate thread
		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Method to run in a multi threaded environemnt
	 */
	public void run() {

		try {

			// Get notification from the server
			int player = fromServer.readInt();

			// Am I player 1 or 2?
			if (player == PLAYER1) {
				setMyToken('X');
				setOtherToken('O');
				setTitleMessage("Player 1 with Red Token");
				setStatusMessage("Waiting for player 2 to join");

				// Receive startup notification from the server
				fromServer.readInt(); // Whatever read is ignored here, but this
										// is the first CONTNUE message
										// this is to tell the client to
										// effectively start or continue now a
										// 2nd player has joined

				// The other player has joined
				setStatusMessage("Player 2 has joined. I start first");

				// It is my turn
				setMyTurn(true);
			}

			else if (player == PLAYER2) {

				setMyToken('O');
				setOtherToken('X');
				setTitleMessage("Player 2 with Blue Token");
				setStatusMessage("Waiting for player 1 to move");
			}

			// Continue to play

			while (continueToPlay) {

				if (player == PLAYER1) {
					waitForPlayerAction(); // Wait for player 1 to move
					sendMove(); // Send the move to the server
					receiveInfoFromServer(); // Receive info from the server
				} else if (player == PLAYER2) {
					receiveInfoFromServer(); // Receive info from the server
					waitForPlayerAction(); // Wait for player 2 to move
					sendMove(); // Send player 2's move to the server
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Wait for the player to mark a cell */
	private void waitForPlayerAction() throws InterruptedException {

		while (isWaiting()) { // we are effectively "polling" on a wait flag

			Thread.sleep(100);
		}

		// when we reach this point, the local player must have selected a board
		// position, so proceed
		// and send on the move to the server side

		setWaiting(true);
	}

	/** Send this player's move to the server */
	private void sendMove() throws IOException {

		toServer.writeInt(getRowSelected()); // Send the selected row
		toServer.writeInt(getColumnSelected()); // Send the selected column
	}

	/** Receive info from the server */
	private void receiveInfoFromServer() throws IOException {

		// Receive game status - this will either be a win message, a draw
		// message or a continue message

		int status = fromServer.readInt();

		if (status == PLAYER1_WON) {
			// Player 1 won, stop playing
			continueToPlay = false;
			if (getMyToken() == 'X') {
				setStatusMessage("I won! (X)");
			} else if (getMyToken() == 'O') {
				setStatusMessage("Player 1 has won!");
				receiveMove();
			}
		} else if (status == PLAYER2_WON) {
			// Player 2 won, stop playing
			continueToPlay = false;
			if (getMyToken() == 'O') {
				setStatusMessage("I won! (O)");
			} else if (getMyToken() == 'X') {
				setStatusMessage("Player 2 has won!");
				receiveMove();
			}
		} else if (status == DRAW) {
			// No winner, game is over
			continueToPlay = false;
			setStatusMessage("Game is over, no winner!");

			if (getMyToken() == 'O') {
				receiveMove();
			}
		} else {
			receiveMove();
			setStatusMessage("My turn");
			setMyTurn(true); // It is my turn
		}
	}

	/**
	 * Method to receive players move
	 * @throws IOException
	 */
	private void receiveMove() throws IOException {
		// Get the other player's move
		int row = fromServer.readInt();
		int column = fromServer.readInt();
		cell[row][column].setToken(otherToken);
	}

	// accessors/mutators

	public void setMyTurn(boolean b) {
		myTurn = b;
	}

	public boolean getMyTurn() {
		return myTurn;
	}

	public char getMyToken() {
		return myToken;
	}

	public void setMyToken(char c) {
		myToken = c;
	}

	public char getOtherToken() {
		return otherToken;
	}

	public void setOtherToken(char c) {
		otherToken = c;
	}

	public void setRowSelected(int r) {
		rowSelected = r;
	}

	public int getRowSelected() {
		return rowSelected;
	}

	public void setColumnSelected(int c) {
		columnSelected = c;
	}

	public int getColumnSelected() {
		return columnSelected;
	}

	public void setWaiting(boolean b) {
		waiting = b;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setStatusMessage(String msg) {
		jlblStatus.setText(msg);
	}

	public void setTitleMessage(String msg) {
		jlblTitle.setText(msg);
	}

	public static void main(String[] args) {

		// Create a frame
		ConnectFourClient frame = new ConnectFourClient("Connect Four Client");

		// Display the frame
		frame.setSize(320, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}