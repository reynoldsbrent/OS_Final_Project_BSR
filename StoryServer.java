import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class StoryServer {
    private static final int PORT = 12345;
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static String currentStory = "";
    private static int currentPlayerIndex = 0;
    private static Semaphore turnSemaphore = new Semaphore(1);

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

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Scanner input;
        private PrintWriter output;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                input = new Scanner(clientSocket.getInputStream());
                output = new PrintWriter(clientSocket.getOutputStream(), true);

                // Add the client's PrintWriter to the list
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

                        // Send the updated story to all clients
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
                // Remove the client's PrintWriter when they disconnect
                synchronized (clientWriters) {
                    clientWriters.remove(output);
                }
            }
        }
    }
}

