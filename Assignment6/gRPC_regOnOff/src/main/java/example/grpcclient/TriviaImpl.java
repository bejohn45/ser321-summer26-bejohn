package example.grpcclient;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import service.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;


/**
 * Implementation of trivia quiz service
 */
class TriviaImpl extends TriviaGrpc.TriviaImplBase
{
    private static final String QUESTIONS_FILE = "questions.json";     // question bank, loaded once on first load
    private static final String PERSISTENT_FILE = "trivia_leaderboard.json";     // file to persist top 5 leaderboard
    private static final int TOTAL_QUESTIONS = 10;                       // total quiz questions
    private static final int LEADERBOARD_SIZE = 5;                       // top 5 leaderboard
    private final ArrayList<QuizSession> sessions;                      // list of quiz sessions
    private final ArrayList<QuizQuestion> questionBank;               // all loaded questions
    private final ArrayList<LeaderboardEntry> leaderboard;          // top 5 scores
    private final Random random;                                        // random generator

    // constructor
    public TriviaImpl()
    {
        super();
        sessions = new ArrayList<>();
        questionBank = new ArrayList<>();
        leaderboard = new ArrayList<>();
        random = new Random();
        loadQuestions();
        loadLeaderboard();
    }


    // inner class for a trivia question
    static class QuizQuestion
    {
        String question;    // question
        String a, b, c, d;  // answer choices
        String correctAnswer;         // A, B, C, or D

        // constructor
        public QuizQuestion(String question, String a, String b, String c, String d, String answer)
        {
            this.question = question;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.correctAnswer = answer;
        }

        // format question
        ArrayList<String> toChoicesList()
        {
            ArrayList<String> choices = new ArrayList<>();
            choices.add("A: " +a);
            choices.add("B: " +b);
            choices.add("C: " +c);
            choices.add("D: " +d);

            return choices;
        }
    }

    
    // inner class for quiz session
    static class QuizSession
    {
        String sessionId;
        String name;
        int currentQuestion;
        int score;
        int percent;
        boolean complete;
        ArrayList<QuizQuestion> questions;
        ArrayList<String> userAnswers;
        ArrayList<Boolean> results;

        // constructor
        public QuizSession(String sessionId, String name, ArrayList<QuizQuestion> questions)
        {
            this.sessionId = sessionId;
            this.name = name;
            this.currentQuestion = 0;
            this.score = 0;
            this.percent = 0;
            this.complete = false;
            this.questions = questions;
            this.userAnswers = new ArrayList<>();
            this.results = new ArrayList<>();
        }
    }

    // inner class for leaderboard
    static class LeaderboardEntry
    {
        String name;
        int percent;
    }


    /**
     * Starts a new quiz session for operation chosen
     */
    @Override
    public void startQuiz(StartQuizRequest request, StreamObserver<StartQuizResponse> responseObserver)
    {
        System.out.println("Trivia: startQuiz for " + request.getName());
        StartQuizResponse.Builder response = StartQuizResponse.newBuilder();
        String name = request.getName().trim();

        // validate name
        if (name.isEmpty())
        {
            response.setIsSuccess(false).setError("name is required"); 
        }
        else
        {
            // create new session with 10 random questions
            String sessionId = generateSessionId();
            ArrayList<QuizQuestion> selected = chooseRandomQuestions();
            QuizSession session = new QuizSession(sessionId, name, selected);
            sessions.add(session);
            QuizQuestion first = session.questions.get(0);
            response.setIsSuccess(true).setSessionId(sessionId).setQuestion(formatQuestion(0, first)).addAllChoices(first.toChoicesList());
        }

        StartQuizResponse r = response.build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }

    /**
     * Submit answer for question
     */
    @Override
    public void submitAnswer(SubmitAnswerRequest request, StreamObserver<SubmitAnswerResponse> responseObserver)
    {
        System.out.println("Trivia: submitAnswer for session " + request.getSessionId());
        SubmitAnswerResponse.Builder response = SubmitAnswerResponse.newBuilder();
        String sessionId = request.getSessionId();
        String answer = request.getAnswer().trim().toUpperCase();

        // find session
        if (sessionId.isEmpty())
        {
            response.setIsSuccess(false).setError("session_id is required");
        }
        else
        {
            QuizSession session = findBySessionId(sessionId);
            if (session == null)
            {
                response.setIsSuccess(false).setError("session not found");
            }
            else if (session.complete)
            {
                response.setIsSuccess(false).setError("quiz already completed");
            }
            else if (!isValidAnswer(answer))
            {
                response.setIsSuccess(false).setError("invalid answer");
            }
            else
            {
                // check answer
                int currentIndex = session.currentQuestion;
                QuizQuestion currentQuestion = session.questions.get(currentIndex);
                boolean correct = answer.equals(currentQuestion.correctAnswer);
                // update result
                session.userAnswers.add(answer);
                session.results.add(correct);
                if (correct)
                {
                    session.score++;
                }
                session.currentQuestion++;
                // result message
                String resultMessage;
                if (correct)
                {
                    resultMessage = "Correct!";
                }
                else
                {
                    resultMessage = "Wrong! The correct answer is " +currentQuestion.correctAnswer;
                }

                response.setIsSuccess(true).setCorrect(correct).setResultMessage(resultMessage);

                // check if quiz complete
                if (session.currentQuestion >= TOTAL_QUESTIONS)
                {
                    session.complete = true;
                    session.percent = (session.score*100) / TOTAL_QUESTIONS;
                    String finalScore = "You got " +session.percent+ "% (" +session.score+ "/" +TOTAL_QUESTIONS+ ")!";
                    response.setQuizComplete(true).setFinalScore(finalScore);

                    // results summary as repeated string
                    for (int i=0; i < TOTAL_QUESTIONS; i++)
                    {
                        String grade = session.results.get(i) ? "CORRECT" : "WRONG: answer is " +session.questions.get(i).correctAnswer;
                        response.addAllResults("Q" +(i+1)+ ": " +session.questions.get(i)+ session.userAnswers.get(i)+ "    " +grade);
                    }

                    // update leaderboard if top 5
                    updateLeaderboard(session.name, session.percent);
                }
                else
                {
                    // send next question
                    QuizQuestion next = session.questions.get(session.currentQuestion);
                    response.setQuizComplete(false).setNextQuestion(formatQuestion(session.currentQuestion, next)).addAllNextChoices(next.toChoicesList());
                }
            }
        }

        SubmitAnswerResponse r = response.build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }


    /**
     * Get top 5 scores
     */
    @Override
    public void getScores(Empty request, StreamObserver<GetScoresResponse> responseObserver)
    {
        System.out.println("Trivia: getScores");
        GetScoresResponse.Builder response = GetScoresResponse.newBuilder();

        // check if leaderboard empty
        if (leaderboard.isEmpty())
        {
            response.setIsSuccess(false).setError("no scores yet");
        }
        else
        {
            ArrayList<TriviaScore> scores = new ArrayList<>();
            for (LeaderboardEntry entry : leaderboard)
            {
                scores.add(TriviaScore.newBuilder().setName(entry.name).setPercent(entry.percent).build());
            }
            response.setIsSuccess(true).addAllScores(scores);
        }
        GetScoresResponse r = response.build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }

    /**
     * Find session with session id
     */
    private QuizSession findBySessionId(String sessionId)
    {
        for (QuizSession session : sessions)
        {
            if (session.sessionId.equals(sessionId))
            {
                return session;
            }
        }
        return null;
    }


    /**
     * Generate random session id
     */
    private String generateSessionId()
    {
        return "session_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }

    /**
     * Choose 10 random questions from question bank
     */
    private ArrayList<QuizQuestion> chooseRandomQuesions()
    {
        ArrayList<QuizQuestion> shuffled = new ArrayList<>(questionBank);
        Collections.shuffle(shuffled);
        int count = Math.min(TOTAL_QUESTIONS, shuffled.size());
        return new ArrayList<>(shuffled.subList(0, count));
    }

    /**
     * Format question to show "Question x/10: ___ + ___ = "
     */
    private String formatQuestion(int index, QuizSession session)
    {
        return "Question " +(index + 1)+ "/" +TOTAL_QUESTIONS+ ": " +session.questions.get(index);
    }


    /**
     * Load question bank from questions.json
     */
    private void loadQuestions()
    {
        File file = new File(QUESTIONS_FILE);
        if (!file.exists())
        {
            System.out.println("Trivia: " +QUESTIONS_FILE+ " not found. No questions.");
            return;
        }
        // read file and parse json
        try
        {
            String data = new String(Files.readAllBytes(file.toPath()));
            JSONArray arr = new JSONArray(data);
            // extract questions, choices, and answers from json array
            for (int i=0; i<arr.length(); i++)
            {
                JSONObject obj = arr.getJSONObject(i);
                String question = obj.getString("question");
                String a = obj.getString("A");
                String b = obj.getString("B");
                String c = obj.getString("C");
                String d = obj.getString("D");
                String answer = obj.getString("answer");
                questionBank.add(new QuizQuestion(question, a, b, c, d, answer));
            }
            System.out.println("Trivia: loaded " +questionBank.size()+ " questions from " +QUESTIONS_FILE);
        }
        catch (Exception e)
        {
            System.err.println("Trivia: error loading " +QUESTIONS_FILE+ ": " +e.getMessage());
        }
    }


    /**
     * Load leaderboard from trivia_leaderboard.json
     */
    private void loadLeaderboard()
    {
        File file = new File(PERSISTENT_FILE);
        if (!file.exists())
        {
            System.out.println("Trivia: " +PERSISTENT_FILE+ " not found. No leaderboard.");
            return;
        }
        // read file and parse json
        try
        {
            String data = new String(Files.readAllBytes(file.toPath()));
            JSONArray arr = new JSONArray(data);
            for (int i=0; i<arr.length(); i++)
            {
                JSONObject obj = arr.getJSONObject(i);
                LeaderboardEntry entry = new LeaderboardEntry();
                entry.name = obj.getString("name");
                entry.percent = obj.getInt("percent");
                leaderboard.add(entry);
            }
            System.out.println("Trivia: loaded " +leaderboard.size()+ " leaderboard entries from " +PERSISTENT_FILE);
        }
        catch (Exception e)
        {
            System.err.println("Trivia: error loading " +PERSISTENT_FILE+ ": " +e.getMessage());
        }
    }


    /**
     * Add entry to leaderboard and save
     */
    private void updateLeaderboard(String name, int percent)
    {
        LeaderboardEntry entry = new LeaderboardEntry();
        entry.name = name;
        entry.percent = percent;
        leaderboard.add(entry);
        
        // sort by percent and keep top 5
        leaderboard.sort((a, b) -> b.percent - a.percent);
        while (leaderboard.size() > LEADERBOARD_SIZE)
        {
            leaderboard.remove(leaderboard.size() - 1);
        }
        saveLeaderboard();
    }



    /**
     * Save leaderboard to trivia_leaderboard.json
     */
    private void saveLeaderboard()
    {
        try
        {
            JSONArray arr = new JSONArray();
            for (LeaderboardEntry entry : leaderboard)
            {
                JSONObject obj = new JSONObject();
                obj.put("name", entry.name);
                obj.put("percent", entry.percent);
                arr.put(obj);
            }
            Files.write(Paths.get(PERSISTENT_FILE), arr.toString(2).getBytes());
        }
        catch (Exception e)
        {
            System.err.println("Trivia: error saving " +PERSISTENT_FILE+ ": " +e.getMessage());
        }
    }   

}
