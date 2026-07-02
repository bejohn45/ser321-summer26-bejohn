import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Worker node to connect to leader and participate in consensus rounds
 */
public class Worker 
{
    /**
     * Main method to start worker
     */
    public static void main(String[] args)
    {
        // check if arguments given
        if (args.length < 2)
        {
            System.out.println("Usage: gradle runWorker --args \"<leaderHost> <leaderPort>\"");
            System.out.println("Example usage: gradle runWorker --args \"localhost 9000\"");
            return;
        }
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        // need name from leader
        String name = "";
        System.out.println("Connecting to leader at " +host+ ":" +port);

        // socket connection to leader
        try (Socket socket = new Socket(host, port);
            BufferedReader fromLeader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter toLeader = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in)))
        {
            
        }
        catch (IOException e)
        {
            System.out.println(name+ " could not communicate with leader: " +e.getMessage());
        }
        System.out.println(name+ " stopped.");
    }
}
