# Assignment 3 Task 2: Hangman Game Protocol

**Author:** Bevin John
**Date:** 6/11/26

---

## How to Run
You can use Gradle to run things, running with ./gradlew is of course also an option
**Server:**
Default
```bash
gradle Server
```

With arguments
```bash
gradle Server -Pport=8888
```

**Client:**
Default but running more quietly on Gradle
```bash
gradle Client --console=plain -q
```

With arguments
```bash
gradle Client -Phost=localhost -Pport=8888
```

---

## Video Demonstration

**Link:** [Insert link to your 4-7 minute video demonstration here]

The video demonstrates:
- Starting server and client
- Complete game playthrough
- All implemented features

---

## Implemented Features Checklist

### Core Features (Required)
- [x] Set Player Name (provided as example)
- [ ] Start New Game
- [ ] Guess Letter
- [ ] Game State
- [ ] Win/Lose Detection
- [x] Graceful Quit

### Medium Features (Enhanced Gameplay)
- [ ] Hint feature
- [ ] Word Guessing
- [ ] Guessed Letters Command
- [ ] Give Up

### Advanced Features (Competition)
- [ ] Scoring System
- [ ] Leaderboard

**Note:** Mark [x] for completed features, [ ] for not implemented.

---

## Protocol Specification

### Overview
[Provide a brief overview of your protocol design - what patterns did you use, how does communication work, etc.]



---

### 1. Set Player Name

**Request:**
```json
{
    "type": "name",
    "name": "<string>"
}
```

**Success Response:**
```json
{
    "type": "name",
    "ok": true,
    "message": "Welcome <name>! ..."
}
```

**Error Response:**
```json
{
    "ok": false,
    "message": "Name cannot be empty"
}
```

---

### 2. Start New Game

**Request:**
```json
{
    "type": "start"
}
```

**Success Response:**
```json
{
    "type" : "start",
    "ok" : true,
    "message" : "New game started!", // send message for new game started to client
    "wordLength" : "<integer>", // length of word
    "wordDisplay" : "<string>", // starts with "_ _ _ _ ..." to start and fills in as client guesses
    "hangman" : "<string>", // hangman display
    "misses" : 0, // guesses where client was wrong
    "maxMisses" : 6, // max misses before game ends
    "score" : 0, // score at 0 to start
    "hintsUsed" : 0, // hints used start at 0
    "lettersGuessed" : 0, // letters guessed by client start at 0
    "gameOver" : false, // false if game active
    "won" : false // false if word not correctly guessed yet
}
```

**Error Response(s):**
```json
{
    "ok" : false,
    "message" : "Player name must be set before starting a game!"
}
```

---

### 3. Guess Letter

**Request:**
```json
{
    "type": "guessLetter",
    "letter" : "<char>"
}
```

**Correct Guess Response:**
```json
{
    "type" : "guessLetter",
    "ok" : true,
    "letter" : "<char>",
    "message" : "Correct! <letter> is in the word <times> times.", // guess correct -- show client how many times letter is in word
    "correct" : true, 
    "times" : "<integer>", // number of times letter is in word
    "wordDisplay" : "<string>", // replaces _ in word with wherever letter is
    "hangman" : "<string>", // no change to hangman
    "misses" : "<integer",  // no change to misses
    "maxMisses" : 6,
    "score" : "<integer>", // updated score -- +5 points per times letter is in word
    "hintsUsed" : "<integer>", // no change to hints
    "lettersGuessed" : "<integer>", // incremented by 1
    "gameOver" : false, // false if game active
    "won" : false // false if word not correctly guessed yet
}
```

**Incorrect Guess Response:**
```json
{
    "type" : "guessLetter",
    "ok" : true,
    "letter" : "<char>",
    "message" : "Wrong! <letter> is not in the word.", // guess wrong
    "correct" : false, 
    "times" : 0, 
    "wordDisplay" : "<string>", // no change
    "hangman" : "<string>", // no change to hangman
    "misses" : "<integer",  // incremented by 1
    "maxMisses" : 6,
    "score" : "<integer>", // updated score -- -1 point
    "hintsUsed" : "<integer>", // no change to hints
    "lettersGuessed" : "<integer>", // incremented by 1
    "gameOver" : false, 
    "won" : false 
}
```

**Winning Game Response:**
```json
{
    "type" : "guessLetter",
    "ok" : true,
    "letter" : "<char>",
    "message" : "Correct! <letter> is in the word <times> times. You won the game!", // game winning guess
    "correct" : true, 
    "times" : "<integer>", 
    "wordDisplay" : "<string>", // whole word
    "hangman" : "<string>", // no change to hangman
    "misses" : "<integer",  // no change
    "maxMisses" : 6,
    "score" : "<integer>", // updated score -- +5 points per times and +20 points for winning
    "hintsUsed" : "<integer>", // no change to hints
    "lettersGuessed" : "<integer>", // incremented by 1
    "gameOver" : false, 
    "won" : false,
    "word" : "<String>" // word
}
```

**Losing Game Response:**
```json
{
    "type" : "guessLetter",
    "ok" : true,
    "letter" : "<char>",
    "message" : "Wrong! <letter> is not in the word. Game over, you lost!", // guess wrong
    "correct" : false, 
    "times" : 0, 
    "wordDisplay" : "<string>", // no change
    "hangman" : "<string>", // no change to hangman
    "misses" : "<integer",  // incremented by 1
    "maxMisses" : 6,
    "score" : "<integer>", // updated score -- -1 point
    "hintsUsed" : "<integer>", // no change to hints
    "lettersGuessed" : "<integer>", // incremented by 1
    "gameOver" : false, 
    "won" : false 
}
```


**Error Response(s):**
```json
{
    "ok" : false,
    "message" : "Player name must be set before starting a game!"
}
```

---

### 4. Start New Game

**Request:**
```json
{
    "type": "start"
}
```

**Success Response:**
```json
{
    "type" : "start",
    "ok" : true,
    "message" : "New game started!", // send message for new game started to client
    "wordLength" : "<integer>", // length of word
    "wordDisplay" : "<string>", // starts with "_ _ _ _ ..." to start and fills in as client guesses
    "hangman" : "<string>", // hangman display
    "misses" : 0, // guesses where client was wrong
    "maxMisses" : 6, // max misses before game ends
    "score" : 0, // score at 0 to start
    "hintsUsed" : 0, // hints used start at 0
    "lettersGuessed" : 0, // letters guessed by client start at 0
    "gameOver" : false, // false if game active
    "won" : false // false if word not correctly guessed yet
}
```

**Error Response(s):**
```json
{
    "ok" : false,
    "message" : "Player name must be set before starting a game!"
}
```


## Error Handling Strategy

[Explain your approach to error handling:]

**Server-side validation:**
- [What validations does your server perform?]
  <Your answer>

- [How do you handle missing fields?]
  <Your answer>

- [How do you handle invalid data types?]
  <Your answer>

- [How do you handle game state errors?]
  <Your answer>

---

## Robustness

[Explain how you ensured robustness:]

**Server robustness:**
- [How does server handle invalid input without crashing?]
- <Your answer>


**Client robustness:**
- [How does client handle unexpected responses?]
- <Your answer>

- [What happens if server is unavailable?]
- <Your answer>

---

## Assumptions (if applicable)

[List any assumptions you made about the protocol or game rules]

1. [Assumption 1]
2. [Assumption 2]
3. [etc.]

---

## Known Issues

[List any known bugs or limitations]

1. [Issue 1]
2. [Issue 2]

---
