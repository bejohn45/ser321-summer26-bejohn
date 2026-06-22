# Activity 1: Task Management System with Threading

## Overview

In this activity, you will work with a **Task Management System** that allows multiple clients to manage shared tasks. The system supports:
- Adding tasks with categories
- Listing tasks (all, pending, or finished)
- Marking tasks as finished


You will learn about:
- **Protocol Buffers (Protobuf)** - An efficient binary serialization format
- **Multi-threading** - Handling multiple clients simultaneously
- **Thread safety** - Protecting shared data from race conditions

## Starter Code

### What's Provided

1. **Server.java** - Single-threaded server
2. **Client.java** - Complete client with menu system JSON basis
3. **Performer.java** - Handles all client requests using JSON
4. **Task.java** - Task data structure
5. **TaskList.java** - Shared task collection (not thread-safe yet)
6. **JsonUtils.java** - JSON utility methods
7. **task.proto** - Protobuf protocol definition (STARTER - incomplete)
8. **build.gradle** - Build configuration

### Running the Starter Code

The starter code uses JSON protocol and is fully functional for the implemented operations.
In `Client` and `Performer`, there is a welcome-message section where you can switch
between JSON and Proto by commenting/uncommenting. That should be your first step.
Use it as the example pattern for step-by-step conversion.

**Generate the java files for Proto:**
```bash
gradle generateProto
```

**Start the server:**
Single thread:
```bash
gradle runServer
# Or with custom port:
gradle runServer -Pport=9000
```

Multi-threaded:
```bash
gradle runThreadedServer
# Or with custom port:
gradle runThreadedServer -Pport=9000
```


**Start one or more clients (each in a new terminal):**
```bash
gradle runClient
# Or with custom host/port:
gradle runClient -Phost=localhost -Pport=9000
```

Try the operations:
1. Add a task
2. List tasks
3. Finish a task
4. Quit
5. Try running multiple clients - only one works at a time (single-threaded!)

## Current Protocol (Protocol Buffers)

The server currently uses a Protocol Buffer protocol:

### Message Format

**Request:**
```proto
{
  enum RequestType {
    UNKNOWN = 0;
    ADD = 1;
    LIST = 2;
    FINISH = 3;
    QUIT = 4;

  }
  RequestType type = 1;

  // ADD
  string description = 2;
  string category = 3;

  // LIST
  string filter = 4;

  // FINISH
  int32 id = 5;
}
```

**Response:**
```proto
{
  enum ResponseType {
    UNKNOWN = 0;
    SUCCESS = 1;
    ERROR = 2;
  }

  ResponseType type = 1;
  string message = 2;

  // ADD
  TaskProto task = 3;

  // LIST
  repeated TaskProto tasks = 4;
  int32 count = 5;
}
```

**TaskProto Message:**
```proto
{
  int32 id = 1;
  string description = 2;
  string category = 3;
  bool finished = 4;
}
```

### Add Task
**Request:**
```proto
{
  type : ADD
  description : "HW"
  category : "school"
}
```

**Success Response:**
```proto
{
  type : SUCCESS
  message = "Task added successfully"
  task {
    id : 1
    description : "HW"
    category : "school"
    finished : false
  }
}
```

**Error Response:**
```proto
{
  type : ERROR
  message : "Invalid category value. Must be 'work', 'personal', 'school', or 'other'"
}
```

### List Tasks
**Request:**
```proto
{
  type : LIST
  filter : "all"
}
```
- `filter` can be: "all", "pending", or "finished" (optional, defaults to "all")

**Success Response:**
```proto
{
  type : SUCCESS
  message : "ALL tasks"
  tasks {
    id : 1
    description : "HW"
    category : "school"
    finished : true
  }
  count : 1
}
```

### Finish Task
**Request:**
```proto
{
  type : FINISH
  id : 1
}
```

**Success Response:**
```proto
{
  type : SUCCESS
  message : "Task #1 marked as finished"
}
```

**Error Response:**
```proto
{
  type : ERROR
  message : "Task not found with ID: 1"
}
```

### Quit
**Request:**
```proto
{
  type : QUIT
}
```

**Success Response:**
```proto
{
  type : SUCCESS
  messgage : "Quitting now, goodbye!"
}
```
