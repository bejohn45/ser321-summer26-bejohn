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
- [x] Start New Game
- [x] Guess Letter
- [x] Game State
- [x] Win/Lose Detection
- [x] Graceful Quit

### Medium Features (Enhanced Gameplay)
- [x] Hint feature
- [x] Word Guessing
- [x] Guessed Letters Command
- [x] Give Up

### Advanced Features (Competition)
- [x] Scoring System
- [x] Leaderboard

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
    "message" : "New game started!",
    "wordLength" : "<integer>",
    "wordDisplay" : "<string>",
    "hangman" : "<string>",
    "misses" : 0,
    "maxMisses" : 6,
    "score" : 0,
    "hintsUsed" : 0, 
    "lettersGuessed" : "[]",
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

### 3. Guess Letter

**Request:**
```json
{
    "type": "guessLetter",
    "letter" : "<letter>"
}
```

**Correct Guess Response:**
```json
{
    "type" : "guessLetter",
    "ok" : true,
    "letter" : "<letter>",
    "message" : "Correct! <letter> is in the word <times> times.",
    "correct" : true, 
    "times" : "<integer>",
    "wordDisplay" : "<string>",
    "hangman" : "<string>",
    "misses" : "<integer",
    "maxMisses" : 6,
    "score" : "<integer>",
    "hintsUsed" : "<integer>",
    "lettersGuessed" : "[<letter>]",
    "gameOver" : false,
    "won" : false
}
```

**Incorrect Guess Response:**
```json
{
    "type" : "guessLetter",
    "ok" : true,
    "letter" : "<letter>",
    "message" : "Wrong! <letter> is not in the word.",
    "correct" : false, 
    "times" : 0, 
    "wordDisplay" : "<string>",
    "hangman" : "<string>",
    "misses" : "<integer",
    "maxMisses" : 6,
    "score" : "<integer>",
    "hintsUsed" : "<integer>",
    "lettersGuessed" : ["<letter>"],
    "gameOver" : false,
    "won" : false 
}
```

**Winning Game Response:**
```json
{
    "type" : "guessLetter",
    "ok" : true,
    "letter" : "<letter>",
    "message" : "Correct! <letter> is in the word <times> times. You won the game!",
    "correct" : true, 
    "times" : "<integer>",
    "wordDisplay" : "<string>",
    "hangman" : "<string>",
    "misses" : "<integer",
    "maxMisses" : 6,
    "score" : "<integer>",
    "hintsUsed" : "<integer>",
    "lettersGuessed" : "[<letter>]",
    "gameOver" : true, 
    "won" : true,
    "word" : "<String>"
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
    "wordDisplay" : "<string>", // no change
    "hangman" : "<string>", // update hangman
    "misses" : 6,  // misus = maxMisses
    "score" : "<integer>", // updated score -- -1 point
    "gameOver" : true, 
    "won" : false ,
    "word" : "<String>" // word
}
```

**Error Response: Invalid Letter:**
```json
{
    "ok" : false,
    "message" : "<letter> is not a valid guess. Guess must be a letter from a-z!"
}
```

**Error Response: Letter Already Guessed:**
```json
{
    "ok" : false,
    "message" : "<letter> has already been guessed!"
}
```

---

### 4. Show Game State

**Request:**
```json
{
    "type": "gameState",
}
```

**Success Response:**
```json
{
    "type" : "gameState",
    "ok" : true,
    "message" : "Current game state: ",
    "wordDisplay" : "<string>", 
    "hangman" : "<string>",  
    "misses" : "<integer>", 
    "maxMisses" : 6, 
    "score" : "<integer>",
    "hintsUsed" : 0, 
    "lettersGuessed" : ["<letter>"], 
    "gameOver" : false, 
    "won" : false 
}
```

---


### 5. Graceful Quit

**Request:**
```json
{
    "type": "quit",
}
```

**Success Response:**
```json
{
    "type" : "quit",
    "ok" : true,
    "message" : "Goodbye <name>!"
}
```

---


### 6. Get Hint

**Request:**
```json
{
    "type": "hint",
}
```

**Success Response:**
```json
{
    "type" : "hint",
    "ok" : true,
    "message" : "Hint shows the letter <letter>!",
    "wordDisplay" : "<string>", 
    "hangman" : "<string>",  
    "misses" : "<integer>", 
    "maxMisses" : 6, 
    "score" : "<integer>",
    "hintsUsed" : 0, 
    "lettersGuessed" : ["<letter>"], 
    "gameOver" : false, 
    "won" : false 
}
```

**Winning Hint Response:**
```json
{
    "type" : "hint",
    "ok" : true,
    "hintLetter" : "<letter>",
    "times" : "<integer>",
    "message" : "Hint showed the final letter <letter>. You won the game!",
    "wordDisplay" : "<string>", 
    "hangman" : "<string>",  
    "misses" : "<integer>", 
    "maxMisses" : 6, 
    "score" : "<integer>",
    "hintsUsed" : "<integer>", 
    "lettersGuessed" : ["<letter>"], 
    "gameOver" : true, 
    "won" : true,
    "word" : "<String>"
}
```

---


### 7. Show Guessed Letters

**Request:**
```json
{
    "type": "guessedLetters"
}
```

**Success Response With Guessed Letters:**
```json
{
    "type" : "guessedLetters",
    "ok" : true,
    "letters" : ["<letter"],
    "total" : "<integer>",
    "message" : "These are the letters you guessed <name>!"
}
```

**Success Response With No Guessed Letters:**
```json
{
    "type" : "guessedLetters",
    "ok" : true,
    "letters" : [],
    "total" : 0,
    "message" : "No letters guessed yet!"
}
```

---


### 8. Give Up

**Request:**
```json
{
    "type": "giveUp"
}
```

**Success Response:**
```json
{
    "type" : "giveUp",
    "ok" : true,
    "message" : "You gave up! Returning to main menu...",
    "hangman" : "<string>",  
    "misses" : "<integer>", 
    "score" : "<integer>",
    "hintsUsed" : "<integer>", 
    "lettersGuessed" : ["<letter>"], 
    "gameOver" : true, 
    "won" : false,
    "word" : "<String>"
}
```


---


### 9. View Leaderboard

**Request:**
```json
{
    "type": "leaderboard"
}
```

**Success Response:**
```json
{
    "type" : "leaderboard",
    "ok" : true,
    "message" : "Leaderboard ranked by best score.",
    "players" : ["name" : "<string>", "bestScore" : "<integer>", "averageScore" : "<number>", "winPercentage" : "<number>", "gamesPlayed" : "<integer>"]
}
```

---


---



## Error Handling Strategy

[Explain your approach to error handling:]

**Server-side validation:**
- [What validations does your server perform?]
  I check for a letter is given from a-z, if it was already guessed, if a word or letter given is a string.

- [How do you handle missing fields?]
  I used the testField() method which returns message if the field doesn't exist in request.

- [How do you handle invalid data types?]
  I get the value inputted as an object first and then verify if it's a string. Then I check for if guessed letter is a-z or word only has letters. I return error messages to indicate if not valid. So numbers or symbols would indicate to user they need to enter a letter a-z.

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
