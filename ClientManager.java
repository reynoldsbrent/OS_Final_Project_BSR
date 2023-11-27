import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The ClientManager class is responsible for creating and managing multiple client threads,
 * each connecting to a server, receiving a unique client ID, and storing it in a queue.
 * The client threads simulate behavior such as waiting for turns and contributing to a story.
 * This is to test the server's ability to handle multiple clients.
 * @author Brent Reynolds
 * @date Fall 2023
 */
public class ClientManager {

    
    private static final String SERVER_ADDRESS = "localhost";

    
    private static final int PORT = 8080;

    
    private static final int NUM_CLIENTS = 200;

    /**
     * The main method that orchestrates the creation and execution of client threads.
     */
    public static void main(String[] args) {
        // BlockingQueue to store unique client IDs in order
        BlockingQueue<String> clientIdQueue = new ArrayBlockingQueue<>(NUM_CLIENTS);

        // Create and start client threads
        for (int i = 0; i < NUM_CLIENTS; i++) {
            try {
                // Delay between the creation of each client thread
                Thread.sleep(100);

                // Create a new client thread
                Thread clientThread = new Thread(() -> {
                    try {
                        // Establish a connection to the server
                        Socket socket = new Socket(SERVER_ADDRESS, PORT);
                        Scanner serverInput = new Scanner(socket.getInputStream());
                        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                        // Receive the client ID from the server
                        String clientIdMessage = serverInput.nextLine();
                        System.out.println(clientIdMessage);

                        // Extract the client ID and add it to the queue
                        String clientId = clientIdMessage.split(": ")[1];
                        clientIdQueue.offer(clientId);

                        // Close the client socket
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Start the client thread
                clientThread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Client IDs in order: " + clientIdQueue);
    }
}
