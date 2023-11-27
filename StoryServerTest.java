import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/**
 * The StoryServer class represents the server for the Story Builder Game.
 * It listens for client connections and handles them using separate threads.
 * The server manages a turn-based story-building game with multiple clients.
 * The class includes a nested ClientHandler class to handle communication with individual clients.
 * @author Your Name
 * @date Date
 */
public class StoryServerTest {

    // The port number on which the server listens for client connections.
    private static final int PORT = 8080;

    // List of PrintWriter instances for each connected client, used for communication.
    private static List<PrintWriter> clientWriters = new ArrayList<>();

    // The current state of the story being built by clients.
    private static String currentStory = "";

    // Index of the current player
    private static int currentPlayerIndex = 0;

    // Semaphore for controlling the turn in the game and managing access to shared resources.
    private static Semaphore turnSemaphore = new Semaphore(1);

    // Counter for generating unique client IDs.
    private static int clientIdCounter = 0;

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
     * The ClientHandler class handles communication with a single client.
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

        /**
         * The run method for the client thread.
         * Manages the communication and turn-based game with a single client.
         */
        @Override
        public void run() {
            try {
                input = new Scanner(clientSocket.getInputStream());
                output = new PrintWriter(clientSocket.getOutputStream(), true);

                // Add the client's PrintWriter to the list in a synchronized block
                synchronized (clientWriters) {
                    clientWriters.add(output);
                }

                // Generate and send a unique client ID to the client
                int clientId = generateClientId();
                output.println("Client ID: " + clientId);

                // Send the current story to the new client
                output.println(currentStory);

                while (true) {
                    // Acquire the semaphore to control the turn
                    turnSemaphore.acquire();

                    // Notify the current player and wait for their turn
                    int playerIndex = clientWriters.indexOf(output);
                    if (playerIndex == currentPlayerIndex) {
                        output.println("Your turn: ");

                        // Process client input if available
                        if (input.hasNextLine()) {
                            String clientInput = input.nextLine();
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

        /**
         * Generates a unique client ID in a synchronized manner.
         * @return The generated client ID.
         */
        private synchronized int generateClientId() {
            return clientIdCounter++;
        }
    }
}


