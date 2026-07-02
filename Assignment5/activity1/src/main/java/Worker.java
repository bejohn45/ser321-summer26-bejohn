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
    private static volatile boolean answerNeeded = false;   // true if worker needs to give answer
    private static volatile String lastAnswer = null;       // last answer given by worker
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
            final String workerName = name;
            
            // listener thead to receive tasks and send results
            Thread listener = new Thread(() -> {
                try
                {
                    // loop to receive tasks and send results
                    String line;
                    while ((line = fromLeader.readLine()) != null)
                    {
                        if (line.startsWith("TASK "))
                        {
                            String task = line.substring("TASK ".length()).trim();
                            lastAnswer = null;
                            answerNeeded = true;
                            System.out.println("\nTask received: " +task);
                            // ask user for answer
                            System.out.print("> Enter your result: ");
                            System.out.flush();
                            // String answer = console.readLine();
                            // disconnect if no answer provided
                            // if (answer == null)
                            // {
                            //     System.out.println("\nNo answer provided. Disconnecting.");
                            //     break;
                            // }
                            // // send answer to leader
                            // answer = answer.trim();
                            // lastAnswer = answer;
                            // toLeader.println("RESULT " +answer);
                            // System.out.println("Result submitted to leader.");
                        }
                        // check if timeout message
                        else if (line.startsWith("TIMEOUT "))
                        {
                            String message = line.substring("TIMEOUT ".length()).trim();
                            answerNeeded = false;
                            System.out.println("\n(Timeout) " +message);
                        }
                        else if (line.startsWith("CONSENSUS "))
                        {
                            String consensus = line.substring("CONSENSUS ".length()).trim();
                            answerNeeded = false;
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
                            System.exit(0);
                        }
                        else
                        {
                            System.out.println("(Worker) Unknown message from leader: " +line);
                        }
                    }
                    // leader closed connection
                    System.out.println("\nLeader connection closed. Disconnecting.");
                    System.exit(0);
                }
                catch (IOException e)
                {
                    System.out.println("\n" +workerName+ " lost connected to leader: " +e.getMessage());
                    System.exit(0);
                }
            });
            listener.setDaemon(true);
            listener.start();

            // read console and send answers
            String input;
            while ((input = console.readLine()) != null)
            {
                if (answerNeeded)
                {
                    String answer = input.trim();
                    lastAnswer = answer;
                    answerNeeded = false;
                    toLeader.println("RESULT " +answer);
                    System.out.println("Result submitted to leader.");
                }
                // timed out wait for next task
                else
                {
                    System.out.println("No answer needed at this time. Please wait for the next task.");
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(name+ " could not communicate with leader: " +e.getMessage());
        }
        System.out.println(name+ " stopped.");
    }
}
