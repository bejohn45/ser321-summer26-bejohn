# GRPC Services and Registry

The following folder contains a Registry.jar which includes a Registering service where Nodes can register to allow clients to find them and use their implemented GRPC services. 

Some more detailed explanations will follow and please also check the build.gradle file

## Run things locally without registry
To run see also video. To run locally and without Registry which you should do for the beginning

First Terminal

    gradle runNode

Second Terminal

    gradle runClient

## Run things locally with registry

First terminal

    gradle runRegistryServer

Second terminal

    gradle runNode -PregOn=true 

Third Terminal

    gradle runClient -PregOn=true

### gradle runRegistryServer
Will run the Registry node on localhost (arguments are possible see gradle). This node will run and allows nodes to register themselves. 

The Server allows Protobuf, JSON and gRPC. We will only be using gRPC

### gradle runNode
Will run a node with services. The starter code includes Echo and Joke services as examples. You will need to implement and add the Converter and Library services.

For the Library service: A books.txt file is provided with initial book data (format: title|author|isbn, one per line). Your server should load this on first run and create library_data.json for persistence.

The node registers itself on the Registry. You can change the host and port the node runs on and this will register accordingly with the Registry

### gradle runClient
Will run a client which will call the services from the node, it talks to the node directly not through the registry. At the end the client does some calls to the Registry to pull the services, this will be needed later.

### gradle runDiscovery
Will create a couple of threads with each running a node with services in JSON and Protobuf. This is just an example and not needed for assignment 6. 

### gradle testProtobufRegistration
Registers the protobuf nodes from runDiscovery and do some calls. 

### gradle testJSONRegistration
Registers the json nodes from runDiscovery and do some calls. 

### gradle test
Runs the test cases. The starter code includes example tests for Joke and Echo in ServerTest.java. You need to add your own tests for Converter and Library services in the same file.

IMPORTANT: Tests expect the server to be running first!
First run in one terminal:
    gradle runNode
Then in second terminal:
    gradle test

The tests connect to localhost:8000 by default.

To run in IDE:
- go about it like in the ProtoBuf assignment to get rid of errors
- all mains expect input, so if you want to run them in your IDE you need to provide the inputs for them, see build.gradle






## Project Description
This project implements a distributed system using gRPC where a service node hosts services that clients can connect to and use. The system includes a Registry that nodes can register with, so clients and find the services.

Services Available:
- **Echo** - echoes back a message sent by client
- **Joke** - get and add jokes on the server
- **Converter** - converts between units of length, weight, and temperature
- **Library** - manage a book libray with borrow and returning with persistent state
- **Trivia** - take a 10 question multiple choice trivia quiz with persistent top 5 leaderboard

## How to Run

### Run locally without registry

First terminal

    gradle runNode

Second terminal

    gradle runClient --console=plain


### Run locally with registry

First terminal

    gradle runRegistryServer

Second terminal

    gradle runNode -PregOn=true

Third terminal

    gradle runClient -PregOn=true --console=plain


### Run unit tests

IMPORTANT: Tests expect the server to be running first!

First run in one terminal:
    gradle runNode
Then in second terminal:
    gradle serverTest

The tests connect to localhost:8000 by default.


### How to work with program

When client starts, it shows menu of available services:

Available services:
1- Echo
2- Joke
3- Converter
4- Library
5- Trivia
0- Exit
Choose a service (enter number): 


### Echo
Enter any message any the server will echo back

### Joke
- Option 1: Get jokes - enter how many jokes you want
- Option 2: Add a joke - enter a joke to add to server

### Converter
Supports units:
- LENGTH: KILOMETER, MILE, YARD, FOOT
- WEIGHT: KILOGRAM, POUND
- TEMPERATURE: CELSIUS, FAHRENHEIT

Enter a value, then from unit, then to unit. Not case sensitive for units.

### Library
- Option 1: List all books - shows a books and availability
- Option 2: Seach books - search by title or author
- Option 3: Borrow a book - enter isbn and your name
- Option 4: Return a book - enter isbn

On first run library loads from books.json and creates library_data.json for persistence. Anytime after it loads from library_data.json so data loads when restarting.

### Trivia
- Option 1: View leaderboard - shows the current top 5 scores
- Option 2: Take a quiz - enter your name and then answer 10 multiple choice trivia questions

Answer 10 questions to get score. Results shown with CORRECT/WRONG breakdown. If score is in top 5, it is saved to the leaderboard.


## List of requirements fullfilled

### Task 1:
- Node runs with gradle runNode and client runs with gradle runClient
- Converted service implemented in server and client
- Library service implemented in server and client with persistent state
- Easy user interaction
- Client shows available services and lets user choose
- Unit tests for Converter and Library in ServerTest.java
- Library data persists after server restart
- Server and client handles errors

### Task 2:
- Protocol design in trivia.proto
- Client lets user choose Trivia from the menu, then either view the leaderboard or take a quiz 
- Server loads bank of trivia questions from `questions.json`, randomly selected 10 questions for session, and tracks session progress and score
- Service allows at least 2 different requests
- Each request needs at least 1 input
- Response returns different data for different requests
- Response returns a repeated field for `choices`, `next_choices`, `all_results`, and `scores`
- Data is held persistent on the server with top 5 leaderboard saved to `trivia_leaderboard.java` and loaded when started again

## Screencast
https://youtu.be/_aRIM-RDAmc


### Task 3:
Run with
    gradle runClient -PserviceHost="3.15.168.230" -PservicePort=8000 --console=plain