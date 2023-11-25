import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * This class represents a client for the Story Builder Game.
 * It connects to the server and allows the player to contribute to the ongoing story.
 * @author Brent Reynolds
 * @date Fall 2023
 */
public class StoryClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8080;

    /**
     * The main method that starts the client.
     * It connects to the server and handles the communication between the client and server.
     */
    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             Scanner serverInput = new Scanner(socket.getInputStream());
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner userInput = new Scanner(System.in)) {

            // Print the welcome message and initial story
            System.out.println("Welcome to the Story Builder Game!");

            // Allow the player to contribute to the story continuously
            while (true) {
                String message = serverInput.nextLine();
                String turnIndicator = "";

                // Print the message from the server (Your turn or the updated story)
                if (message.equals("Your turn: ") || message.equals("Waiting for other players...")) {
                    turnIndicator = message;
                    if (turnIndicator.equals("Your turn: ")) {
                        System.out.print(turnIndicator);
                    }

                    if (turnIndicator.equals("Your turn: ")) {
                        // Player's turn, prompt for input and send to the server
                        System.out.print("Enter your sentence: ");
                        String userSentence = userInput.nextLine();
                        output.println(userSentence);
                    } else {
                        // Not player's turn, print the updated story
                        String updatedStory = serverInput.nextLine();
                        System.out.println(updatedStory);
                    }
                } else {
                    // Handling other messages (e.g., "Waiting for other players...")
                    if (!message.equals("Waiting for other players...")) {
                        String updatedStory = message;
                        System.out.println(updatedStory);
                        System.out.println("----------------------------------");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}