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
        String name = "Worker(?)";
        System.out.println("Connecting to leader at " +host+ ":" +port);

        // socket connection to leader
        try (Socket socket = new Socket(host, port);
            BufferedReader fromLeader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter toLeader = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in)))
        {
            System.out.println("Connected successfully!");
            // get name from leader
            String hello = fromLeader.readLine();
            if ((hello != null) && (hello.startsWith("HELLO ")))
            {
                name = hello.substring("HELLO ".length()).trim();
            }
            System.out.println("Assigned name: " +name);

            // loop to receive tasks and send results
            String lastAnswer = null;
            String line;
            while ((line = fromLeader.readLine()) != null)
            {
                if (line.startsWith("TASK "))
                {
                    String task = line.substring("TASK ".length()).trim();
                    System.out.println("\nTask received: " +task);
                    // ask user for answer
                    System.out.print("> Enter your result: ");
                    System.out.flush();
                    String answer = console.readLine();
                    // disconnect if no answer provided
                    if (answer == null)
                    {
                        System.out.println("\nNo answer provided. Disconnecting.");
                        break;
                    }
                    // send answer to leader
                    answer = answer.trim();
                    lastAnswer = answer;
                    toLeader.println("RESULT " +answer);
                    System.out.println("Result submitted to leader.");
                }
                else if (line.startsWith("CONSENSUS "))
                {
                    String consensus = line.substring("CONSENSUS ".length()).trim();
                    System.out.println("\nConsensus announced: " +consensus);

                    // check if no consensus
                    if (consensus.startsWith("No consensus"))
                    {
                        System.out.println("No consensus reached this round.");
                    }
                    // check if voted with majority
                    else if (lastAnswer != null)
                    {
                        String winner = consensus.split(" ")[0];
                        if (winner.equals(lastAnswer))
                        {
                            System.out.println("You voted with the majority!");
                        }
                        else
                        {
                            System.out.println("You did not vote with the majority.");
                        }
                    }
                    System.out.println("\nWaiting for next task...");
                }
                // check if leader closed session
                else if (line.equals("QUIT"))
                {
                    System.out.println("\nLeader closed the session. Disconnecting.");
                    break;
                }
                else
                {
                    System.out.println("(Worker) Unknown message from leader: " +line);
                }
            }
            // no line from leader
            if (line == null)
            {
                System.out.println("\nLeader connection closed. Disconnecting.");
            }
        }
        catch (IOException e)
        {
            System.out.println(name+ " could not communicate with leader: " +e.getMessage());
        }
        System.out.println(name+ " stopped.");
    }
}
