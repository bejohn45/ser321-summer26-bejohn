import org.json.JSONArray;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 * Hangman Game Client - Student Starter Code
 *
 * Your task: Implement the protocol communication for all game features.
 *
 * What's provided:
 * - Complete menu structure with different game states
 * - Name handling as a complete example
 * - Some method stubs as examples
 *
 * What you need to implement:
 * - Protocol requests/responses for all game operations
 * - Proper response handling and display
 */
public class HangmanClient 
{
    static Socket sock;
    static ObjectOutputStream oos;
    static ObjectInputStream in;

    static Scanner scanner = new Scanner(System.in);
    static boolean inGame = false;
    static boolean hasName = false;
    static String playerName = "";

    public static void main(String[] args) 
    {
        if (args.length != 2) 
        {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try 
        {
            sock = new Socket(host, port);
            oos = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());

            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║     WELCOME TO HANGMAN GAME!           ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println();

            boolean running = true;
            while (running) 
            {
                if (!hasName) 
                {
                    running = showInitialMenu();
                } 
                else if (!inGame) 
                {
                    running = showMainMenu();
                } 
                else 
                {
                    running = showGameMenu();
                }
                System.out.println();
            }

            closeConnection();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * Initial menu - before name is set
     */
    static boolean showInitialMenu() 
    {
        System.out.println("────────────────────────────────────────");
        System.out.println("  1. Set Your Name");
        System.out.println("  2. Quit");
        System.out.println("────────────────────────────────────────");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) 
        {
            case "1":
                setName();
                return true;
            case "2":
                quit();
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }

    /**
     * Main menu - after name set, no active game
     */
    static boolean showMainMenu() 
    {
        System.out.println("────────────────────────────────────────");
        System.out.println("MAIN MENU:");
        System.out.println("  1. Start New Game");
        System.out.println("  2. View Leaderboard");
        System.out.println("  3. Quit");
        System.out.println("────────────────────────────────────────");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) 
        {
            case "1":
                startGame();
                return true;
            case "2":
                // TODO: implement me
                return true;
            case "3":
                quit();
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }

    /**
     * Game menu - during active game
     * Natural input: just type letter/word to guess
     * Commands: 1, 2, 3, 4, 0 for special actions
     */
    static boolean showGameMenu() 
    {
        System.out.println("\n────────────────────────────────────────");
        System.out.println("Type a letter or word to guess");
        System.out.println("Or choose:");
        System.out.println("  1 - Show game state");
        System.out.println("  2 - See guessed letters");
        System.out.println("  3 - Get a hint (-8 points)");
        System.out.println("  4 - Give up (return to main menu)");
        System.out.println("  0 - Quit game");
        System.out.println("────────────────────────────────────────");
        System.out.print("Your input: ");
        String input = scanner.nextLine().trim();

        // Handle special commands
        if (input.equals("1")) 
        {
            // show game state
            showGameState();
            return true;
        } 
        else if (input.equals("2")) 
        {
            // show guessed letters
            showGuessedLetters();
            return true;
        } 
        else if (input.equals("3"))
        {
            getHint();
            return true;
        } 
        else if (input.equals("4")) 
        {
            giveUp();
            return false;
        } 
        else if (input.equals("0")) 
        {
            quit();
            return false;
        }

        if (input.isEmpty()) {
            System.out.println("Please enter a letter, word, or command.");
            return true;
        }

        // Single character = letter guess, multiple = word guess
        if (input.length() == 1) 
        {
            // guess letter
            guessLetter(input);
        } 
        else 
        {
            // guess word
            guessWord(input);
        }

        return true;
    }

    /**
     * TODO: Implement the rest
     * IMPORTANT: This should send a request to the server to end the game!
     * Just setting inGame = false locally creates a state mismatch
     * where the client thinks the game is over but the server still has it active.
     *
     * Proper implementation should:
     * - Confirm with user
     * - Send "give up" or "end game" (or similar) request to server
     * - Server ends game, does not add to leaderboard
     * - Server responds confirming game ended
     * - Client sets inGame = false
     */
    static void giveUp() 
    {
        System.out.print("\nAre you sure you want to give up? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) 
        {
            // TODO: Send request to server to properly end the game
            inGame = false;
            System.out.println("\nYou gave up! Returning to main menu...\n");
            System.out.println("[TODO: This should notify the server to end the game properly]");
        } 
        else 
        {
            System.out.println("\nContinuing game...");
        }
    }

    /**
     * EXAMPLE IMPLEMENTATION: Set player name
     * This is provided as a complete example.
     * Use this as a reference for implementing other methods.
     */
    static void setName() 
    {
        System.out.print("\nEnter your name: ");
        String name = scanner.nextLine().trim();

        // Create request according to YOUR protocol design
        JSONObject request = new JSONObject();
        request.put("type", "name");
        request.put("name", name);

        // Send request and get response
        JSONObject response = sendRequest(request);
        if (response != null) {
            if (response.getBoolean("ok")) 
            {
                hasName = true;
                playerName = name;
                System.out.println("\n" + response.getString("message"));
                System.out.println();
            } 
            else 
            {
                System.out.println("✗ Error: " + response.getString("message"));
            }
        }
    }
    

    /**
     * Send a start request to the server and handle the response
     */
    static void startGame() 
    {
        JSONObject request = new JSONObject();
        request.put("type", "start");
        JSONObject response = sendRequest(request);
        // check if null response
        if (response == null)
        {
            return;
        }
        // check if error response
        if (!response.getBoolean("ok"))
        {
            System.out.println("Error: " + response.getString("message"));
            return;
        }

        inGame = true;
        System.out.println("\n" + response.getString("message"));
        System.out.println("Word length: " + response.getInt("wordLength"));
        System.out.println("Max misses allowed: " + response.getInt("maxMisses"));
        displayGame(response);
    }


    /**
     * Display game state using server response
     */
    static void displayGame(JSONObject response)
    {
        System.out.println(response.getString("hangman"));
        System.out.println("Word: " + response.getString("wordDisplay"));
        System.out.println("Misses: " +response.getInt("misses") + "/" + response.getInt("maxMisses"));
        System.out.println("Score: " + response.getInt("score"));
    }

    /**
     * 
     */
    static void guessLetter(String input)
    {
        JSONObject request = new JSONObject();
        request.put("type", "guessLetter");
        request.put("letter", input);

        JSONObject response = sendRequest(request);

        // check if null response
        if (response == null)
        {
            return;
        }
        // check if error response
        if (!response.getBoolean("ok"))
        {
            System.out.println("Error: " + response.getString("message"));
            return;
        }

        System.out.println("\n" + response.getString("message"));
        displayGame(response);

        // check if game over
        if (response.getBoolean("gameOver"))
        {
            System.out.println("Word: " + response.getString("word"));
            System.out.println("Final score: " + response.getInt("score"));
            System.out.println("Hints used: " + response.getInt("hintsUsed"));
            if (response.getBoolean("won"))
            {
                System.out.println("You won!");
            }
            else
            {
                System.out.println("You lost!");
            }
            inGame = false;
        }
    }

    /**
     * Show game state to client
     */
    static void showGameState()
    {
        JSONObject request = new JSONObject();
        request.put("type", "gameState");
        JSONObject response = sendRequest(request);

        // check if null response
        if (response == null)
        {
            return;
        }
        // check if error response
        if (!response.getBoolean("ok"))
        {
            System.out.println("\nError: " + response.getString("message"));
            return;
        }
        // print state
        System.out.println("\n===== GAME STATE =====");
        displayGame(response);
        System.out.println("Hints used: " + response.getInt("hintsUsed"));
        System.out.println("Letters guessed : " +response.getJSONArray("lettersGuessed").length());
    }

    /**
     * Get hint from server
     */
    static void getHint()
    {
        JSONObject request = new JSONObject();
        request.put("type", "hint");
        JSONObject response = sendRequest(request);
        // check if null response
        if (response == null)
        {
            return;
        }
        // check if error response
        if (!response.getBoolean("ok"))
        {
            System.out.println("\nError: " + response.getString("message"));
            return;
        }
        // show hint
        System.out.println("\n" +response.getString("message"));
        displayGame(response);

        // check if game over
        if (response.getBoolean("gameOver"))
        {
            System.out.println("Word: " + response.getString("word"));
            System.out.println("Final score: " + response.getInt("score"));
            System.out.println("Hints used: " + response.getInt("hintsUsed"));
            inGame = false;
        }
    }

    /**
     * Guess the entire word
     */
    static void guessWord(String input)
    {
        JSONObject request = new JSONObject();
        request.put("type", "guessWord");
        request.put("wordGuess", input);

        JSONObject response = sendRequest(request);

        // check if null response
        if (response == null)
        {
            return;
        }
        // check if error response
        if (!response.getBoolean("ok"))
        {
            System.out.println("Error: " + response.getString("message"));
            return;
        }

        System.out.println("\n" + response.getString("message"));
        displayGame(response);

        // check if game over
        if (response.getBoolean("gameOver"))
        {
            System.out.println("Word: " + response.getString("word"));
            System.out.println("Final score: " + response.getInt("score"));
            System.out.println("Hints used: " + response.getInt("hintsUsed"));
            inGame = false;
        }
    }

    /**
     * Show guessed letters
     */
    static void showGuessedLetters()
    {
        JSONObject request = new JSONObject();
        request.put("type", "guessedLetters");
        JSONObject response = sendRequest(request);
        // check if null response
        if (response == null)
        {
            return;
        }
        // check if error response
        if (!response.getBoolean("ok"))
        {
            System.out.println("\nError: " + response.getString("message"));
            return;
        }
        // show guessed letters
        JSONArray letters = response.getJSONArray("letters");
        System.out.println("\n===== Guessed Letters =====");
        // check if no letters
        if (letters.length() == 0)
        {
            System.out.println(response.getString("message"));
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < letters.length(); i++)
            {
                if (i > 0)
                {
                    sb.append(", ");
                }
                sb.append(letters.getString(i));
            }
            System.out.println("Letters: " + sb.toString());
        }
        System.out.println("Total: " +response.getInt("total"));
    }

    /**
     * Quit game
     */
    static boolean quit() 
    {
        JSONObject request = new JSONObject();
        request.put("type", "quit");

        JSONObject response = sendRequest(request);
        if (response != null && response.getBoolean("ok")) 
        {
            System.out.println("\n" + response.getString("message"));
            System.out.println("Thanks for playing!");
        }
        return false; // Stop the main loop
    }

    /**
     * Helper: Send request and receive response
     * This handles the basic communication pattern
     */
    static JSONObject sendRequest(JSONObject request) 
    {
        try 
        {
            String req = request.toString();
            oos.writeObject(req);
            oos.flush();

            String res = (String) in.readObject();
            return new JSONObject(res);
        } 
        catch (Exception e) 
        {
            System.out.println("Error communicating with server: " + e.getMessage());
            return null;
        }
    }

    /**
     * Close connection
     */
    static void closeConnection() 
    {
        try 
        {
            if (oos != null) oos.close();
            if (in != null) in.close();
            if (sock != null) sock.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}
