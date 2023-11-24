import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class StoryClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             Scanner serverInput = new Scanner(socket.getInputStream());
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner userInput = new Scanner(System.in)) {

            // Read and print the initial story
            //String initialStory = serverInput.nextLine();
            System.out.println("Welcome to the Story Builder Game!");

            // Allow the player to contribute to the story
            while (true) {
                String message = serverInput.nextLine();
                String turnIndicator = "";
                // Print the message from the server (Your turn or the updated story)
                if(message.equals("Your turn: ") || message.equals("Waiting for other players...")) {
                    turnIndicator = message;
                    if(turnIndicator.equals("Your turn: ")){
                        System.out.print(turnIndicator);
                    }
                    //System.out.print("turn indicator: " + turnIndicator);
                    if (turnIndicator.equals("Your turn: ")) {
                    System.out.print("Enter your sentence: ");
                    String userSentence = userInput.nextLine();
                    output.println(userSentence);
                    }
                    else {
                    String updatedStory = serverInput.nextLine();
                    System.out.println(updatedStory);
                }
                }
                else {
                    if(message != "Waiting for other players...") {
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

