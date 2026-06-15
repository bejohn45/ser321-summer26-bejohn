import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Hangman Game Server - Student Starter Code
 *
 * Your task: Design the protocol and implement the game logic.
 *
 * What's provided:
 * - Resource loading (game stages, word list)
 * - Name handling as a complete example
 * - Basic server structure and routing
 *
 * What you need to implement:
 * - Complete protocol design (document in README.md)
 * - All game logic handlers (stubs provided below)
 */
public class HangmanServer {
    static Socket sock;
    static ObjectOutputStream os;
    static ObjectInputStream in;
    static int port = 8888;

    // Game state for current player - YOU WILL NEED THESE
    static String playerName = null;
    static String secretWord = null;
    static Set<Character> usedLetters = new HashSet<>();
    static int misses = 0;
    static int points = 0;
    static int hintsUsed = 0;
    static boolean inGame = false;

    // Leaderboard - list of game results (you can change this any way you want)
    static List<Map<String, Object>> leaderboard = new ArrayList<>();

    // Game ASCII art - 7 stages (0-6 misses allowed)
    // Loaded from resources/game_stages.txt
    static String[] GAME_STAGES = new String[7];

    // Word list - loaded from resource file
    static String[] WORDS;

    public static void main(String args[]) 
    {
        if (args.length != 1) 
        {
            System.out.println("Expected arguments: <port(int)>");
            System.exit(1);
        }

        try 
        {
            port = Integer.parseInt(args[0]);
        } 
        catch (NumberFormatException nfe) 
        {
            System.out.println("Port must be an integer");
            System.exit(2);
        }

        // Load game resources
        loadGameStages();
        loadWords();

        try 
        {
            ServerSocket serv = new ServerSocket(port);
            System.out.println("Hangman Server ready for connections on port " + port);

            while (true) 
            {
                System.out.println("Server waiting for a connection");
                sock = serv.accept();
                System.out.println("Client connected");

                // Setup streams
                in = new ObjectInputStream(sock.getInputStream());
                OutputStream out = sock.getOutputStream();
                os = new ObjectOutputStream(out);

                // Initialize game state for new connection
                initGame();

                boolean connected = true;
                while (connected) 
                {
                    String s = "";
                    try 
                    {
                        s = (String) in.readObject();
                    } 
                    catch (Exception e) 
                    {
                        System.out.println("Client disconnect");
                        connected = false;
                        continue;
                    }

                    JSONObject res = isValid(s);
                    if (res.has("ok")) {
                        sendResponse(res);
                        continue;
                    }

                    JSONObject req = new JSONObject(s);
                    res = testField(req, "type");
                    if (!res.getBoolean("ok")) 
                    {
                        res = noType(req);
                        sendResponse(res);
                        continue;
                    }

                    // Route to appropriate handler
                    String type = req.getString("type");
                    if (type.equals("name")) 
                    {
                        res = handleName(req);
                      /// include the other types
                    } 
                    else if (type.equals("start")) 
                    {
                        res = handleStart(req);
                    }
                    else if (type.equals("guessLetter"))
                    {
                        res = handleGuessLetter(req);
                    }
                    else if (type.equals("gameState"))
                    {
                        res = handleGameState(req);
                    }
                    else if (type.equals("hint"))
                    {
                        res = handleHint(req);
                    }
                    else if (type.equals("guessWord"))
                    {
                        res = handleGuessWord(req);
                    }
                    else if (type.equals("quit")) 
                    {
                        res = handleQuit(req);
                        sendResponse(res);
                        connected = false;
                        continue;
                    } 
                    else 
                    {
                        res = wrongType(req);
                    }
                    sendResponse(res);
                }
                closeConnection();
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            closeConnection();
        }
    }

    /**
     * EXAMPLE IMPLEMENTATION: Set player name
     * This is provided as a complete example of request handling.
     * Use this as a reference for implementing other handlers.
     */
    static JSONObject handleName(JSONObject req) 
    {
        System.out.println("Name request: " + req.toString());
        JSONObject res = testField(req, "name");
        if (!res.getBoolean("ok")) 
        {
            return res;
        }

        String name = req.getString("name");
        if (name == null || name.trim().isEmpty()) 
        {
            res = new JSONObject();
            res.put("ok", false);
            res.put("message", "Name cannot be empty");
            return res;
        }

        playerName = name.trim();
        res = new JSONObject();
        res.put("ok", true);
        res.put("type", "name");
        res.put("message", "Welcome " + playerName + "! Ready to play Hangman?");
        return res;
    }


    /**
     * Show word display for client
     */    
    static String showWordDisplay() 
    {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < secretWord.length(); i++)
        {
            char c = secretWord.charAt(i);
            if (usedLetters.contains(c)) 
            {
                display.append(c);
            } 
            else 
            {
                display.append("_");
            }
            // add space between letters
            if (i < (secretWord.length() - 1)) 
            {
                display.append(" ");
            }
        }
        return display.toString();
    }

    /**
     * Get used letters as JSONArray
     */
    static JSONArray getUsedLetters() 
    {
        // sort letters
        List<Character> sortedLetters = new ArrayList<>(usedLetters);
        Collections.sort(sortedLetters);
        // add sorted letters to array
        JSONArray letters = new JSONArray();
        for (char c : sortedLetters) 
        {
            letters.put(String.valueOf(c));
        }
        return letters;
    }

     /**
      * Count times a letter is in secret word
      */
    static int countTimes(char guessedLetter)
    {
        int count = 0;
        for (int i = 0; i < secretWord.length(); i++)
        {
            if (secretWord.charAt(i) == guessedLetter) 
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if word guessed
     */
    static boolean isWordGuessed() 
    {
        for (int i = 0; i < secretWord.length(); i++)
        {
            if (!usedLetters.contains(secretWord.charAt(i))) 
            {
                return false;
            }
        }
        return true;
    }


    /**
     * Start handler
     */
    static JSONObject handleStart(JSONObject req) 
    {
        System.out.println("Start request: " + req.toString());
        JSONObject res = new JSONObject();

        // check if player name set
        if ((playerName == null || playerName.trim().isEmpty())) 
        {
            res.put("ok", false);
            res.put("message", "You must set your name before starting the game");
            return res;
        }
        // set new secret word and reset game state
        Random random = new Random();
        secretWord = WORDS[random.nextInt(WORDS.length)];
        usedLetters = new HashSet<>();
        misses = 0;
        points = 0;
        hintsUsed = 0;
        inGame = true;

        // print secret word for server
        System.out.println("Secret word is: " + secretWord);

        // build response
        res.put("type", "start");
        res.put("ok", true);
        res.put("message", "New game started!");
        res.put("wordLength", secretWord.length());
        res.put("wordDisplay", showWordDisplay());
        res.put("hangman", GAME_STAGES[misses]);
        res.put("misses", misses);
        res.put("maxMisses", 6);
        res.put("score", points);
        res.put("hintsUsed", hintsUsed);
        res.put("lettersGuessed", getUsedLetters());
        res.put("gameOver", !inGame);
        res.put("won", false);

        return res;
    }

    /**
     * Guess letter handler
     */
    static JSONObject handleGuessLetter(JSONObject req) 
    {
        System.out.println("Guess letter request: " + req.toString());
        // check if letter field exists
        JSONObject res = testField(req, "letter");
        if (!res.getBoolean("ok")) 
        {
            return res;
        }

        // check if letter is string and not int
        Object letterVal = req.get("letter");
        if (!(letterVal instanceof String))
        {
            res = new JSONObject();
            res.put("ok", false);
            res.put("message", letterVal + " is not a valid guess. Guess must be a letter from a-z!");
            return res;
        }

        // check guess is a letter a-z
        String letterInput = ((String) letterVal).trim().toLowerCase();
        if (!letterInput.matches("[a-z]"))
        {
            res = new JSONObject();
            res.put("ok", false);
            res.put("message", letterInput + " is not a valid guess. Guess must be a single letter from a-z!");
            return res;
        }

        // check if letter already guessed
        char guessedLetter = letterInput.charAt(0); 
        if (usedLetters.contains(guessedLetter))
        {
            res = new JSONObject();
            res.put("ok", false);
            res.put("message", guessedLetter + " has already been guessed!");
            return res;
        }

        // add letter
        usedLetters.add(guessedLetter);
        int times = countTimes(guessedLetter);
        boolean correct = times > 0;

        // update score and misses
        if (correct)
        {
            points += times*5;
        }
        else
        {
            misses++;
            points--;
        }

        boolean won = (correct) && (isWordGuessed());
        boolean lost = (misses >= 6);

        res = new JSONObject();
        res.put("type", "guessLetter");
        res.put("ok", true);
        res.put("letter", String.valueOf(guessedLetter));
        res.put("correct", correct);
        res.put("times", times);

        // winning response
        if (won)
        {
            points +=20;

            // check if perfect game
            if ((hintsUsed == 0) && (misses <= 1))
            {
                points += 10;
            }

            inGame = false;

            // build response
            res.put("message", "Correct! " +guessedLetter+ " is in the word " +times+ " times. You won the game!");
            res.put("wordDisplay", showWordDisplay());
            res.put("hangman", GAME_STAGES[misses]);
            res.put("misses", misses);
            res.put("maxMisses", 6);
            res.put("score", points);
            res.put("hintsUsed", hintsUsed);
            res.put("lettersGuessed", getUsedLetters());
            res.put("gameOver", true);
            res.put("won", true);
            res.put("word", secretWord);
            return res;
        }

        // losing response
        if (lost)
        {
            misses = 6;
            inGame = false;

            // build response
            res.put("message", "Wrong! " +guessedLetter+ " is not in the word. Game over, you lost!");
            res.put("wordDisplay", showWordDisplay());
            res.put("hangman", GAME_STAGES[misses]);
            res.put("misses", misses);
            res.put("maxMisses", 6);
            res.put("score", points);
            res.put("hintsUsed", hintsUsed);
            res.put("lettersGuessed", getUsedLetters());
            res.put("gameOver", true);
            res.put("won", false);
            res.put("word", secretWord);
            return res;
        }

        // correct guess still in game
        if (correct)
        {
            res.put("message", "Correct! " +guessedLetter+ " is in the word " +times+ " times.");
        }
        // wrong guess still in game
        else
        {
            res.put("message", "Wrong! " +guessedLetter+ " is not in the word.");
        }

        res.put("wordDisplay", showWordDisplay());
        res.put("hangman", GAME_STAGES[misses]);
        res.put("misses", misses);
        res.put("maxMisses", 6);
        res.put("score", points);
        res.put("hintsUsed", hintsUsed);
        res.put("lettersGuessed", getUsedLetters());
        res.put("gameOver", false);
        res.put("won", false);
        return res;
    }

    /**
     * Guess word handler
     */
    static JSONObject handleGuessWord(JSONObject req)
    {
        System.out.println("Guess word request: " + req.toString());
        JSONObject res = testField(req, "wordGuess");
        if (!res.getBoolean("ok")) 
        {
            return res;
        }

        // check if word is string 
        Object wordVal = req.get("wordGuess");
        if (!(wordVal instanceof String))
        {
            res = new JSONObject();
            res.put("ok", false);
            res.put("message", wordVal + " is not a valid word guess. Word guess must be only letters a-z!");
            return res;
        }

        // check if word is only letters a-z
        String wordInput = ((String) wordVal).trim().toLowerCase();
        if (!wordInput.matches("[a-z]+"))
        {
            res = new JSONObject();
            res.put("ok", false);
            res.put("message", wordInput + " is not a valid word guess. Word guess must be only letters a-z!");
            return res;
        }
        // check if correct
        boolean correct = wordInput.equals(secretWord);

        res = new JSONObject();
        res.put("type", "guessWord");
        res.put("ok", true);
        res.put("wordGuess", wordInput);
        res.put("correct", correct);

        // if correct guess
        if (correct)
        {
            points += 20;
            // check if perfect game
            if ((hintsUsed == 0) && (misses <= 1))
            {
                points += 10;
            }
            inGame = false;
            for (int i = 0; i < secretWord.length(); i++)
            {
                usedLetters.add(secretWord.charAt(i));
            }
            // build response
            res.put("message", "Correct! You guessed the word " + secretWord + " and won the game!");
            res.put("wordDisplay", showWordDisplay());
            res.put("hangman", GAME_STAGES[misses]);
            res.put("misses", misses);
            res.put("maxMisses", 6);
            res.put("score", points);
            res.put("hintsUsed", hintsUsed);
            res.put("lettersGuessed", getUsedLetters());
            res.put("gameOver", true);
            res.put("won", true);
            res.put("word", secretWord);
            return res;
        }

        // wrong word guess
        misses += 2;
        points -=2;
        // check if more than 6 misses
        if (misses >= 6)
        {
            misses = 6;
            inGame = false;
            // build response
            res.put("message", "Wrong! " +wordInput+ " is not the word. Game over, you lost!");
            res.put("wordDisplay", showWordDisplay());
            res.put("hangman", GAME_STAGES[misses]);
            res.put("misses", misses);
            res.put("maxMisses", 6);
            res.put("score", points);
            res.put("hintsUsed", hintsUsed);
            res.put("lettersGuessed", getUsedLetters());
            res.put("gameOver", true);
            res.put("won", false);
            res.put("word", secretWord);
            return res;

        }

        // wrong word guess still in game
        res.put("message", "Wrong! " +wordInput+ " is not the word. 2 misses added.");
        res.put("wordDisplay", showWordDisplay());
        res.put("hangman", GAME_STAGES[misses]);
        res.put("misses", misses);
        res.put("maxMisses", 6);
        res.put("score", points);
        res.put("hintsUsed", hintsUsed);
        res.put("lettersGuessed", getUsedLetters());
        res.put("gameOver", false);
        res.put("won", false);
        
        return res;
    }


    /**
     * Handle game state request
     */
    static JSONObject handleGameState(JSONObject req) 
    {
        System.out.println("Game state request: " + req.toString());
        JSONObject res = new JSONObject();

        // build response
        res.put("type", "gameState");
        res.put("ok", true);
        res.put("message", "Current game state: ");
        res.put("wordDisplay", showWordDisplay());
        res.put("hangman", GAME_STAGES[misses]);
        res.put("misses", misses);
        res.put("maxMisses", 6);
        res.put("score", points);
        res.put("hintsUsed", hintsUsed);
        res.put("lettersGuessed", getUsedLetters());
        res.put("gameOver", false);
        res.put("won", false);
        return res;
    }


    static JSONObject handleHint(JSONObject req)
    {
        System.out.println("Hint request: " + req.toString());
        JSONObject res = new JSONObject();

        // find letters not guessed
        List<Character> notGuessed = new ArrayList<>();
        for (int i = 0; i < secretWord.length(); i++)
        {
            char letter = secretWord.charAt(i);
            if ((!usedLetters.contains(letter)) && (!notGuessed.contains(letter)))
            {
                notGuessed.add(letter);
            }
        }

        // choose random not guessed letter
        Random random = new Random();
        char hintLetter = notGuessed.get(random.nextInt(notGuessed.size()));
        // update server state
        usedLetters.add(hintLetter);
        hintsUsed++;
        points -= 8;
        int times = countTimes(hintLetter);
        boolean won = isWordGuessed();

        // build response
        res.put("type", "hint");
        res.put("ok", true);
        res.put("hintLetter", String.valueOf(hintLetter));
        res.put("times", times);
        
        // check if won
        if (won)
        {
            points += 20;
            inGame = false;
            res.put("message", "Hint showed the final letter " + hintLetter + ". You won the game!");
            res.put("wordDisplay", showWordDisplay());
            res.put("hangman", GAME_STAGES[misses]);
            res.put("misses", misses);
            res.put("maxMisses", 6);
            res.put("score", points);
            res.put("hintsUsed", hintsUsed);
            res.put("lettersGuessed", getUsedLetters());
            res.put("gameOver", true);
            res.put("won", true);
            res.put("word", secretWord);
            return res;
        }

        // in game still
        res.put("message", "Hint showed the letter " + hintLetter + " !");
        res.put("wordDisplay", showWordDisplay());
        res.put("hangman", GAME_STAGES[misses]);
        res.put("misses", misses);
        res.put("maxMisses", 6);
        res.put("score", points);
        res.put("hintsUsed", hintsUsed);
        res.put("lettersGuessed", getUsedLetters());
        res.put("gameOver", false);
        res.put("won", false);
        return res;
    }


    /**
     * Quit handler
     */
    static JSONObject handleQuit(JSONObject req) 
    {
        System.out.println("Quit request: " + req.toString());
        JSONObject res = new JSONObject();

        res.put("ok", true);
        res.put("type", "quit");
        res.put("message", "Goodbye " + (playerName != null ? playerName : "player") + "!");

        return res;
    }

    /**
     * Helper: Initialize game state for new connection
     */
    static void initGame() 
    {
        playerName = null;
        secretWord = null;
        usedLetters = new HashSet<>();
        misses = 0;
        points = 0;
        hintsUsed = 0;
        inGame = false;
    }

    /**
     * Helper: Check if field exists in request
     */
    static JSONObject testField(JSONObject req, String key) 
    {
        JSONObject res = new JSONObject();
        if (!req.has(key)) 
        {
            res.put("ok", false);
            res.put("message", "Field '" + key + "' does not exist in request");
            return res;
        }
        return res.put("ok", true);
    }

    /**
     * Helper: Validate JSON
     */
    static JSONObject isValid(String json) 
    {
        try 
        {
            new JSONObject(json);
        } 
        catch (JSONException e) 
        {
            try 
            {
                new JSONArray(json);
            } 
            catch (JSONException ne) 
            {
                JSONObject res = new JSONObject();
                res.put("ok", false);
                res.put("message", "Request is not valid JSON");
                return res;
            }
        }
        return new JSONObject();
    }

    /**
     * Error: no type field
     */
    static JSONObject noType(JSONObject req) 
    {
        System.out.println("No type request: " + req.toString());
        JSONObject res = new JSONObject();
        res.put("ok", false);
        res.put("message", "No request type was given");
        return res;
    }

    /**
     * Error: wrong type
     */
    static JSONObject wrongType(JSONObject req) 
    {
        System.out.println("Wrong type request: " + req.toString());
        JSONObject res = new JSONObject();
        res.put("ok", false);
        res.put("message", "Type '" + req.getString("type") + "' is not supported");
        return res;
    }

    /**
     * Load game ASCII art stages from resource file
     */
    static void loadGameStages() 
    {
        try 
        {
            InputStream is = HangmanServer.class.getResourceAsStream("/game_stages.txt");
            if (is == null) 
            {
                System.err.println("Error: game_stages.txt not found in resources");
                System.exit(1);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder currentStage = new StringBuilder();
            int stageIndex = 0;

            while ((line = reader.readLine()) != null) 
            {
                if (line.equals("---")) 
                {
                    GAME_STAGES[stageIndex++] = "\n" + currentStage.toString();
                    currentStage = new StringBuilder();
                } 
                else if (!line.startsWith("STAGE")) 
                {
                    currentStage.append(line).append("\n");
                }
            }
            // Add final stage
            if (currentStage.length() > 0 && stageIndex < 7) 
            {
                GAME_STAGES[stageIndex] = "\n" + currentStage.toString();
            }
            reader.close();
            System.out.println("Loaded " + (stageIndex + 1) + " game stages");
        } 
        catch (Exception e) 
        {
            System.err.println("Error loading game stages: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Load word list from resource file
     */
    static void loadWords() 
    {
        try 
        {
            WORDS = loadWordList("/words.txt");
            System.out.println("Loaded " + WORDS.length + " words");
        } 
        catch (Exception e) 
        {
            System.err.println("Error loading word list: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Helper: Load a single word list from file
     */
    static String[] loadWordList(String filename) throws IOException 
    {
        InputStream is = HangmanServer.class.getResourceAsStream(filename);
        if (is == null) 
        {
            throw new IOException("Word list file not found: " + filename);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> words = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) 
        {
            line = line.trim();
            if (!line.isEmpty()) 
            {
                words.add(line.toLowerCase());
            }
        }
        reader.close();

        return words.toArray(new String[0]);
    }

    /**
     * Write response to client
     */
    static void sendResponse(JSONObject res) 
    {
        try 
        {
            os.writeObject(res.toString());
            os.flush();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * Close connection
     */
    static void closeConnection() 
    {
        try 
        {
            if (os != null) os.close();
            if (in != null) in.close();
            if (sock != null) sock.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}
