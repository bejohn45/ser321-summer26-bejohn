package taskone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Threaded server to handle multiple client connections concurrently
 */
public class ThreadedServer 
{
    // private static final int DEFAULT_PORT = 8888;
    // private static TaskList taskList = new TaskList();

    // public static void main(String[] args) {
    //     int port = DEFAULT_PORT;

    //     // Parse port from command line if provided
    //     if (args.length > 0) {
    //         try {
    //             port = Integer.parseInt(args[0]);
    //         } catch (NumberFormatException e) {
    //             System.err.println("Invalid port number. Using default port: " + DEFAULT_PORT);
    //             port = DEFAULT_PORT;
    //         }
    //     }

    //     System.out.println("Task Management Server starting on port " + port);
    //     System.out.println("Waiting for clients...");

    //     try (ServerSocket serverSocket = new ServerSocket(port)) {
    //         while (true) {
    //             // Accept connection (blocks until client connects)
    //             Socket clientSocket = serverSocket.accept();
    //             System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

    //             // Handle client request (BLOCKS - single-threaded)
    //             Performer performer = new Performer(clientSocket, taskList);
    //             performer.doPerform();

    //             // Close connection
    //             clientSocket.close();
    //             System.out.println("Client disconnected");
    //         }
    //     } catch (IOException e) {
    //         System.err.println("Server error: " + e.getMessage());
    //         e.printStackTrace();
    //     }
    // }
    private static final int DEFAULT_PORT = 8888;
    private static TaskList taskList = new TaskList();

    /**
     * Main method to start the threaded server
     */
    public static void main(String[] args) 
    {
        int port = DEFAULT_PORT;

        // parse port from command line if provided
        if (args.length > 0) 
        {
            try 
            {
                port = Integer.parseInt(args[0]);
            } 
            catch (NumberFormatException e) 
            {
                System.err.println("Invalid port number. Using default port: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        System.out.println("ThreadedTask Management Server starting on port " + port);
        System.out.println("Waiting for clients...");

        try (ServerSocket serverSocket = new ServerSocket(port)) 
        {
            while (true) 
            {
                // accept connection (blocks until client connects)
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // give client own thread, start, and go back to waiting for next client
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } 
        catch (IOException e) 
        {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Handle client connection in a separate thread
     */
    private static void handleClient(Socket clientSocket)
    {
        // Performer for client
        try
        {
            Performer performer = new Performer(clientSocket, taskList);
            performer.doPerform();
        }
        // error for a client with doPerform()
        catch (Exception e)
        {
            System.err.println("Error handling client: " + e.getMessage());
        }
        finally
        {
            try
            {
                if ((clientSocket != null) && (!clientSocket.isClosed())) 
                {
                    clientSocket.close();
                }
                System.out.println("Client disconnected");
            }
            catch (IOException e)
            {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
