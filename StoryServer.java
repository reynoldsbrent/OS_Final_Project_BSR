import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/**
 * This class represents the server for the Story Builder Game.
 * It listens for client connections and handles them using separate threads.
 * @author Brent Reynolds
 * @date Fall 2023
 */
public class StoryServer {
    private static final int PORT = 8080;
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static String currentStory = "";
    private static int currentPlayerIndex = 0;
    private static Semaphore turnSemaphore = new Semaphore(1);

    /**
     * The main method that starts the server and continuously listens for client connections.
     */
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This class handles communication with a single client.
     * It is responsible for managing the turn-based story-building game.
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Scanner input;
        private PrintWriter output;

        /**
         * Constructor for the ClientHandler class.
         * @param socket The client's socket.
         */
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                input = new Scanner(clientSocket.getInputStream());
                output = new PrintWriter(clientSocket.getOutputStream(), true);

                // Add the client's PrintWriter to the list in a synchronized block
                synchronized (clientWriters) {
                    clientWriters.add(output);
                }

                // Send the current story to the new client
                output.println(currentStory);

                while (true) {
                    // Acquire the semaphore to control the turn
                    turnSemaphore.acquire();

                    // Notify the current player and wait for their turn
                    int playerIndex = clientWriters.indexOf(output);
                    if (playerIndex == currentPlayerIndex) {
                        output.println("Your turn: ");
                        String clientInput = input.nextLine();

                        // Update the current story
                        currentStory += clientInput + "\n";

                        // Switch to the next player
                        currentPlayerIndex = (currentPlayerIndex + 1) % clientWriters.size();

                        // Send the updated story to all clients in a synchronized block
                        synchronized (clientWriters) {
                            for (PrintWriter writer : clientWriters) {
                                writer.println(currentStory);
                            }
                        }
                    }

                    // Release the semaphore to allow the next player to take a turn
                    turnSemaphore.release();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Remove the client's PrintWriter when they disconnect in a synchronized block
                synchronized (clientWriters) {
                    clientWriters.remove(output);
                }
            }
        }
    }
}