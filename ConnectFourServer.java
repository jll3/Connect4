import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class ConnectFourServer extends JFrame implements ConnectFourConstants {

    private ServerSocket serverSocket;
    private Socket player1;
    private Socket player2;
    private GameSession game;


    /**
     * Constructor to start up the server
     */
    public ConnectFourServer() {

        JTextArea serverLog = new JTextArea();

        // Create a scroll pane to hold text area
        JScrollPane scrollPane = new JScrollPane(serverLog);

        // Add the scroll pane to the frame
        add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setTitle("ConnectFourServer");
        setVisible(true);

        try {

            // Create a server socket
            serverSocket = new ServerSocket(8000);

            serverLog.append(new Date() + ": Server started at socket 8000\n");

            // Number a session
            int sessionNo = 1;

            // Ready to create a session for every two players

            while (true) {

                serverLog.append(new Date() + ": Wait for players to join session " + sessionNo + '\n');

                // Connect to player 1
                player1 = serverSocket.accept();

                serverLog.append(new Date() + ": Player 1 joined session " + sessionNo + '\n');

                serverLog.append("Player 1's IP address" + player1.getInetAddress().getHostAddress() + '\n');

                // Notify that the player is Player 1

                new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

                // Connect to player 2
                player2 = serverSocket.accept();

                serverLog.append(new Date() + ": Player 2 joined session " + sessionNo + '\n');

                serverLog.append("Player 2's IP address" + player2.getInetAddress().getHostAddress() + '\n');

                // Notify that the player is Player 2

                new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

                // Display this session and increment session number

                serverLog.append(new Date() + ": Start a thread for session " + sessionNo++ + '\n');


                // Create a new thread for this session of two players

                game = new GameSession(player1, player2);

                // Start the new thread
                new Thread(game).start();

          }
        }
        catch(IOException ex) {
            System.err.println(ex);
        }
    }

    public static void main(String[] args) {

    	ConnectFourServer frame = new ConnectFourServer();
    }

}