import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
        System.out.println("Leader starting on port " + port);
        System.out.println("Waiting for workers to connect... (need at least " +MIN_WORKERS+ ")");

        // accept connections
        while (workers.size() < MIN_WORKERS)
        {
            Socket socket = serverSocket.accept();
        }

    }



    private class WorkerHandler extends Thread
    {
        private final Socket socket;
        private final String name;
        private final BufferedReader in;
        private final PrintWriter out;

        WorkerHandler(Socket socket) throws IOException
        {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
            
        }

        @Override
        public void run()
        {
            try
            {
                String line;
                while ((line = in.readLine()) != null)
                {
                    if (line.startsWith("RESULT:"))
                    {
                        String result = line.substring("RESULT:".length()).trim();
                }
            }
        }
    }





    
}
