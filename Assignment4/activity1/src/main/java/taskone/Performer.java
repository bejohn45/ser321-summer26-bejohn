package taskone;

import java.io.*;
import java.net.Socket;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;

import taskone.proto.Request;
import taskone.proto.Response;
import taskone.proto.TaskProto;

/**
 * Performer class handles client requests using JSON protocol.
 * This version uses JSON for serialization.
 *
 * TODO Use this as an example when you create PerformerProto.Java
 */
public class Performer {
    private final Socket clientSocket;
    private final TaskList taskList;

    private InputStream inStream; // For proto
    private OutputStream outStream; // For proto

    private BufferedReader in; // For JSON
    private PrintWriter out; // For JSON

    public Performer(Socket clientSocket, TaskList taskList) {
        this.clientSocket = clientSocket;
        this.taskList = taskList;
    }

    /**
     * Main method to process client requests.
     * Reads requests, processes them, and sends responses.
     */
    public void doPerform() {
        try {
            inStream = clientSocket.getInputStream();
            outStream = clientSocket.getOutputStream();
            // in = new BufferedReader(new InputStreamReader(inStream));
            // out = new PrintWriter(outStream, true);

            // Here you can see how the welcome message is created and sent in both JSON and Proto.
            // Swap comment brackets as desired just ensure you make matching changes in Client.java.

            /////////////////////////////////////////////////////////////////////////////
            // JSON welcome message
            /////////////////////////////////////////////////////////////////////////////
            // Send welcome message.
            // JSONObject welcomeMessage = JsonUtils.createSuccessResponse("connect", "Connected to Task Management Server");
            // out.println(welcomeMessage);
            /////////////////////////////////////////////////////////////////////////////
            // End JSON welcome message
            /////////////////////////////////////////////////////////////////////////////


            /////////////////////////////////////////////////////////////////////////////
            // Proto welcome message
            /////////////////////////////////////////////////////////////////////////////
            Response.Builder protoResp = Response.newBuilder().setType(Response.ResponseType.SUCCESS).setMessage("Connected to Proto Task Management Server");
            protoResp.build().writeDelimitedTo(outStream);
            outStream.flush();
            /////////////////////////////////////////////////////////////////////////////
            // End proto welcome message
            /////////////////////////////////////////////////////////////////////////////


            // Process requests
            // String request;
            while (true) 
            {
                // request = in.readLine(); // Read as String JSON, not Proto yet.
                // You may skip error handling here to focus on Proto conversion.
                // This may fail if the request is malformed or missing expected fields, which is ok this time.

                // Once you start changing things more and more to proto, the JSON parts might not work anymore.
                // That is fine, just do not call these requests until converted.
                // I recommend starting with the "add" request.

                // System.out.println(request);
                // JSONObject requestJSON = new JSONObject(request);
                // String type = requestJSON.getString("type");

                // JSONObject responseJSON;
                // System.out.println(type);


                // request null if client closed connection
                Request protoReq = Request.parseDelimitedFrom(inStream);
                if (protoReq == null)
                {
                    System.out.println("Client closed connection.");
                    break;
                }

                Request.RequestType type = protoReq.getType();
                System.out.println("Request type: " + type);

                switch (type) 
                {
                    case ADD:
                        protoResp = handleAdd(protoReq); // Hint: This will need to be changed to return ProtoRes and get ProtReq
                        break;
                    case LIST:
                        protoResp = handleList(protoReq);
                        break;
                    case FINISH:
                        protoResp = handleFinish(protoReq);
                        break;
                    case QUIT:
                        protoResp = handleQuit();
                        break;
                    default:
                        protoResp = Response.newBuilder().setType(Response.ResponseType.ERROR).setMessage("Unknown request type: " + type);
                }

                protoResp.build().writeDelimitedTo(outStream);
                outStream.flush();

                // If quit, break the loop
                if (type == Request.RequestType.QUIT) 
                {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }


    /**
     * Convert a Task to TaskProto
     */
    private TaskProto taskToProto(Task task)
    {
        return TaskProto.newBuilder().setId(task.getId()).setDescription(task.getDescription()).setCategory(task.getCategory()).setFinished(task.isFinished()).build();
    }


    private Response.Builder handleAdd(Request protoReq) 
    {
        // Validation is not required so that you can focus on Proto conversion.
        // These comments show what production-style validation would look like. You can also delete all these
        // if they annoy you since it makes it harder to read.

//        // Validate required fields
//        if (!request.has("description")) {
//            return JsonUtils.createErrorResponse("add", "Missing 'description' field");
//        }
//        if (!request.has("category")) {
//            return JsonUtils.createErrorResponse("add", "Missing 'category' field");
//        }

        String description = protoReq.getDescription();
        String category = protoReq.getCategory();
        Task task = taskList.addTask(description, category);
        return Response.newBuilder().setType(Response.ResponseType.SUCCESS).setMessage("Task added successfully").setTask(taskToProto(task));

//        // Validate description not empty
//        if (description.trim().isEmpty()) {
//            return JsonUtils.createErrorResponse("add", "Description cannot be empty");
//        }
//
//        // Validate category value
//        if (!category.equals("work") && !category.equals("personal") && !category.equals("school") && !category.equals("other")) {
//            return JsonUtils.createErrorResponse("add", "Invalid category value. Must be 'work', 'personal', 'school', or 'other'");
//        }

        // Add task
        Task task = taskList.addTask(description, category); // You may assume valid input for this starter version.

        // Return success response with created task
        return JsonUtils.createSuccessResponse("add", JsonUtils.taskToJson(task));
    }

    private JSONObject handleList(JSONObject request) {
        // Get filter (defaults to "all")
        String filter = request.optString("filter", "all");

        List<Task> tasks;
        switch (filter) {
            case "all":
                tasks = taskList.listAllTasks();
                break;
            case "pending":
                tasks = taskList.listPendingTasks();
                break;
            case "finished":
                tasks = taskList.listFinishedTasks();
                break;
            default:
                return JsonUtils.createErrorResponse("list", "Invalid filter value. Must be 'all', 'pending', or 'finished'"); // Keep similar error semantics in Proto.
        }

        // Converts tasks to JSON array
        JSONArray taskArray = new JSONArray();
        for (Task task : tasks) {
            taskArray.put(JsonUtils.taskToJson(task));
        }

        // Creates response data
        JSONObject data = new JSONObject();
        data.put("tasks", taskArray);
        data.put("count", tasks.size());

        return JsonUtils.createSuccessResponse("list", data);
    }

    private JSONObject handleFinish(JSONObject request) {
        // Validation intentionally skipped.

        int id = request.getInt("id");
        // Mark task as finished
        boolean success = taskList.finishTask(id);

        if (success) {
            JSONObject data = new JSONObject();
            data.put("message", "Task #" + id + " marked as finished");
            return JsonUtils.createSuccessResponse("finish", data);
        } else {
            return JsonUtils.createErrorResponse("finish", "Task not found with ID: " + id);
        }
    }

    private JSONObject handleQuit() {
        JSONObject data = new JSONObject();
        data.put("message", "Goodbye!");
        return JsonUtils.createSuccessResponse("quit", data);
    }
}
