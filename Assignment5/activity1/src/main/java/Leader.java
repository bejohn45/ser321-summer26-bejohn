import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

/**
 * Leader node to manage workers with assigning tasks and determining consensus
 */
public class Leader 
{
    private static final int MIN_WORKERS = 3;               // min number of workers to start
    private static final int ROUND_TIMEOUT_SECONDS = 30;    // round timeout in seconds
    private final List<WorkerHandler> workers = Collections.synchronizedList(new ArrayList<>());   // synchronized list of workers
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
     * Inner class to handle each worker connection
     */
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
            setDaemon(true);

            // first line from worker
            String hello = in.readLine();
            String workerName = "Worker@" +socket.getPort();
            if ((hello != null) && (hello.startsWith("HELLO:")))
            {
                workerName = hello.substring("HELLO ".length()).trim();
            }
            this.name = workerName;
            // add worker to list
            workers.add(this);
            System.out.println(name+ " connected from " +socket.getInetAddress().getHostName()+ ":" +socket.getPort());
            
        }

        /**
         * Run method to read messages from worker and record results
         */
        @Override
        public void run()
        {
            // read result messages from worker
            try
            {
                String line;
                while ((line = in.readLine()) != null)
                {
                    // record result
                    if (line.startsWith("RESULT:"))
                    {
                        String result = line.substring("RESULT:".length()).trim();
                        recordResult(result);
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            // end of connection
            finally
            {
                workers.remove(this);
                System.out.println("(Leader) : " +name+ " disconnected.");
                close();
            }
        }

        /**
         * Record the result from a worker for current round
         */
        private void recordResult(String value)
        {
            Round round = currentRound;
            // check if not active round
            if (round == null)
            {
                return;
            }

            // 1 vote per worker per round and 1 latch countdown
            if (round.results.putIfAbsent(name, value) == null)
            {
                System.out.println("Received from " +name+ ": " + value);
                round.latch.countDown();
            }
        }

        /**
         * Send a message to the worker
         */
        void send(String message)
        {
            out.println(message);
        }

        /**
         * Close socket connection
         */
        void close()
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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


    /**
     * Start the leader node on port and accept worker connections
     */
    private void start(int port) throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Leader starting on port " + port);
        System.out.println("Waiting for workers to connect... (need at least " +MIN_WORKERS+ ")");

        // accept workers 1 at a time
        while (workers.size() < MIN_WORKERS)
        {
            Socket socket = serverSocket.accept();
            WorkerHandler worker = new WorkerHandler(socket);
            worker.start();
        }
        // start rounds
        System.out.println("\nAll " +workers.size()+ " workers connected. Starting consensus rounds...\n");
        runRounds();
        shutdown(serverSocket);
    }


    /**
     * Run rounds until user quits
     */
    private void runRounds() throws Exception
    {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        int roundNumber = 0;
        // 
        while (true)
        {
            // check if no rounds started yet
            if (roundNumber == 0)
            {
                System.out.println("Please enter an arithmetic task:");
            }
            // other rounds ask for task or quit
            else
            {
                System.out.println("Please enter an arithmetic task (or 'quit'):");
            }
            // read task from console
            String task = console.readLine();
            if ((task == null) || task.trim().equalsIgnoreCase("quit"))
            {
                break;
            }
            task = task.trim();
            if (task.isEmpty())
            {
                continue;
            }
            // run round
            roundNumber++;
            runRound(roundNumber, task);
        }
    }

    /**
     * Run a round with given task
     */
    private void runRound(int roundNumber, String task) throws InterruptedException
    {
        // current workers list incase worker left
        List<WorkerHandler> currentWorkers = new ArrayList<>(workers);
        Round round = new Round(task, currentWorkers.size());
        currentRound = round;
        System.out.println("\nRound " +roundNumber+ ": Assigning task \"" +task+ "\"");
        // send task to workers
        for (WorkerHandler worker : currentWorkers)
        {
            worker.send("TASK " + task);
        }

        // wait for all answers or timeout
        boolean allAnswered = round.latch.await(ROUND_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        if (!allAnswered)
        {
            // timeout message
            System.out.println("(Leader) Timeout (30 sec) reached. Proceeding with " +round.results.size()+ "/" +currentWorkers.size()+ " responses.");
        }
        // close round and determine consensus
        currentRound = null;
        determineConsensus(round, currentWorkers);
    }


    /**
     * Determine consensus from round results and announce to workers
     */
    private void determineConsensus(Round round, List<WorkerHandler> workers)
    {
        Map<String, String> results = round.results;
        int totalAnswers = results.size();
        // track map of value to count of votes
        Map<String, Integer> voteCount = new LinkedHashMap<>();
        for (String value : results.values())
        {
            // add vote for value
            voteCount.put(value, voteCount.getOrDefault(value, 0) + 1);
        }
        System.out.println("Vote distribution: " +voteCount);
    
        // find most votes
        int mostVotes = 0;
        for (int votes : voteCount.values())
        {
            mostVotes = Math.max(mostVotes, votes);
        }
        String winner = null;
        int numTied = 0;
        for (Map.Entry<String, Integer> entry : voteCount.entrySet())
        {
            // check if value has most votes
            if (entry.getValue() == mostVotes)
            {
                numTied++;
                winner = entry.getKey();
            }
        }

        // determine consensus
        String consensusMessage;
        String consensusMessageWorker;
        if (totalAnswers == 0)
        {
            consensusMessageWorker = "No consensus reached: 0 responses";
            consensusMessage = consensusMessageWorker;
        }
        // check if ties
        else if (numTied > 1)
        {
            consensusMessageWorker = "No consensus reached: tie at " +mostVotes+ " votes. Distribution: " +voteCount;
            consensusMessage = consensusMessageWorker;
        }
        // check if no majority
        else if ((mostVotes * 2) <= totalAnswers)
        {
            consensusMessageWorker = "No consensus reached: no answer is a majority. Distribution: " +voteCount;
            consensusMessage = consensusMessageWorker;
        }
        else
        {
            consensusMessageWorker = winner+ " (" +mostVotes+ "/" +totalAnswers+ " workers agreed)";
            consensusMessage = "Consensus: " +consensusMessageWorker;
        }

        // print and announce consensus to workers
        System.out.println(consensusMessage);
        for (WorkerHandler worker : workers)
        {
            worker.send("CONSENSUS " + consensusMessageWorker);
        }
    }


    /**
     * Shutdown leader and notify workers to quit
     */
    private void shutdown(ServerSocket serverSocket)
    {
        System.out.println("\nShutting down. Notifying workers...");
        // close worker connections
        synchronized (workers)
        {
            for (WorkerHandler worker : workers)
            {
                worker.send("QUIT");
                worker.close();
            }
        }
        // close server socket
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("Leader stopped.");
    }

}
