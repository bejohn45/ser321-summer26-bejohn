import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.net.ServerSocket;

/**
 * Leader node to manage workers with assigning tasks and determining consensus
 */
public class Leader 
{
    private static final int MIN_WORKERS = 3;               // min number of workers to start
    private static final int ROUND_TIMEOUT_SECONDS = 30;    // round timeout in seconds
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());   // synchronized list of workers
    private volatile Round currentRound;    // current round

    /**
     * Inner class for state of current round
     * Round
     */
    private static class Round
    {
        final String task;  // task for this round
        final Map<String, String> results = new ConcurrentHashMap<>();  // map of worker results
        final CountDownLatch latch; // latch to wait for all workers to finish

        // constructor
        Round(String task, int numWorkers)
        {
            this.task = task;
            this.latch = new CountDownLatch(numWorkers);
        }
    }

    /**
     * Main method to start the leader node
     */
    public static void main(String[] args) throws Exception
    {
        // if port not given -> using default 9000
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 9000;
        Leader leader = new Leader();
        leader.start(port);
    }

    private void start(int port) throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(port);

    }





    
}
