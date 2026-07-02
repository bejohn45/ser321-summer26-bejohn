# Assignment 5: Distributed Systems and Consensus

**Author:** [Bevin John]
**Date:** [7/1/26]

---

## How to Run

**Leader:**
With arguments
```bash
gradle runLeader --args "9000"
```

**Worker:**
With arguments (run once per worker, in different terminals)
```bash
gradle runWorker --args "localhost 9000"
```

The Leader argument is the port. The Worker arguments are the leader's host and port. The Leader assigns each worker a name `Worker1`, `Worker2`, `Worker3`, etc. by the order the Worker connects. Once an arithmetic task is provided to Leader terminal, like `10+5`, enter an answer in each worker terminal for the task. Enter 'quit' in Leader terminal to shutdown Leader and disconnect Workers.

---

## Protocol Specification

Messages are sent over TCP sockets. Each message is one line with a keyword, space, and information.

### Leader to Worker Messages
- `HELLO <name>` -- Assigns worker its name when it connects.

- `TASK <expression>` -- Sends the arithmetic task for the current round.

- `CONSENSUS <information>` -- Announces round result. Either majority agreed value and count or "No concesus reached..." with a reason.

- `TIMEOUT <reason>` -- Tells worker it did not answer within time limit and vote doesn't count.

- `QUIT` -- Tells all workers to quit

### Worker to Leader Messages
- `RESULT <value>` -- Sends manually entered result from worker back to leader

---

## Consensus Algorithm Design

- The leader uses majority voting to determine the final answer for each round. After getting responses from the workers, it counts how many times each answer was given and selects the answer with the most votes. If that answer has at least 50% of the responding workers agreeing, the leader announces it as the consensus answer. If not, it announces no consensus. Each worker connection runs in its own thread, and a countdown latch lets the leader know when all responses are in or when the timeout is reached.

---

## Handling Worker Failures

- Worker failures are handled using a timeout (30 seconds) so the leader is not waiting forever for responses. If one or more workers do not respond in time, the leader proceeds with the responses it already has and tries to make a decision. All workers that did not respond are sent a `TIMEOUT` message so its terminal stops waiting and shows the reason. If a worker disconnects, its handler thread removes it and the round still completes.

---

## Handling Ties

- Ties are handled by having it be a no consensus case. If 2 answers get the same number of votes, the leader doe not choose either. It would broadcast a CONSENSUS message to all workers with "No consensus reached: tie" with showing the vote distribution, to all workers and goes to the next round.

---

## Edge Cases and Limitations

- One edge case is when a worker receives a task but the user does not provide a response before timeout. The leader may finish the round without that worker's input, which can impact the outcome. A limitation is that worker names are assigned by the order they connect. The workers are set once rounds start, so they cannot join once the session starts.

---

## Issue Encountered

- One issue I encountered was that a worker timed out but did not show the timeout or consensus result. The terminal for worker was stuck on the input prompt with still accepting an answer. I diagnosed this by testing the timeout case and realized the worker was single-threaded. So it could not read the leader's messages until the worker pressed the enter button. I fixed this by splitting the worker into 2 threads, a listener thread that reads the leader's messages and a main thread that reads the console. They both use an answerNeeded flag so if a worker entered a value after timeout, it would be ignored. I added a `TIMEOUT` message from the leader so the worker is told why their vote did not count.

---