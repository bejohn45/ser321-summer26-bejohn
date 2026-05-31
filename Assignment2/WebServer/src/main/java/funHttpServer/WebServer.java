/*
Simple Web Server in Java which allows you to call 
localhost:9000/ and show you the root.html webpage from the www/root.html folder
You can also do some other simple GET requests:
1) /random shows you a random picture (well random from the set defined)
2) json shows you the response as JSON for /random instead the html page
3) /file/filename shows you the raw file (not as HTML)
4) /multiply?num1=3&num2=4 multiplies the two inputs and responses with the result
5) /github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving
   JSON which will for now only be printed in the console. See the todo below

The reading of the request is done "manually", meaning no library that helps making things a 
little easier is used. This is done so you see exactly how to pars the request and 
write a response back
*/

package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;;

class WebServer {
  public static void main(String args[]) {
    WebServer server = new WebServer(9000);
  }

  /**
   * Main thread
   * @param port to listen on
   */
  public WebServer(int port) {
    ServerSocket server = null;
    Socket sock = null;
    InputStream in = null;
    OutputStream out = null;

    try {
      server = new ServerSocket(port);
      while (true) {
        sock = server.accept();
        out = sock.getOutputStream();
        in = sock.getInputStream();
        byte[] response = createResponse(in);
        out.write(response);
        out.flush();
        in.close();
        out.close();
        sock.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (sock != null) {
        try {
          server.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Used in the "/random" endpoint
   */
  private final static HashMap<String, String> _images = new HashMap<>() {
    {
      put("streets", "https://iili.io/JV1pSV.jpg");
      put("bread", "https://iili.io/Jj9MWG.jpg");
    }
  };

  private Random random = new Random();

  /**
   * Reads in socket stream and generates a response
   * @param inStream HTTP input stream from socket
   * @return the byte encoded HTTP response
   */
  public byte[] createResponse(InputStream inStream) {

    byte[] response = null;
    BufferedReader in = null;

    try {

      // Read from socket's input stream. Must use an
      // InputStreamReader to bridge from streams to a reader
      in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

      // Get header and save the request from the GET line:
      // example GET format: GET /index.html HTTP/1.1

      String request = null;

      boolean done = false;
      while (!done) {
        String line = in.readLine();

        System.out.println("Received: " + line);

        // find end of header("\n\n")
        if (line == null || line.equals(""))
          done = true;
        // parse GET format ("GET <path> HTTP/1.1")
        else if (line.startsWith("GET")) {
          int firstSpace = line.indexOf(" ");
          int secondSpace = line.indexOf(" ", firstSpace + 1);

          // extract the request, basically everything after the GET up to HTTP/1.1
          request = line.substring(firstSpace + 2, secondSpace);
        }

      }
      System.out.println("FINISHED PARSING HEADER\n");

      // Generate an appropriate response to the user
      if (request == null) {
        response = "<html>Illegal request: no GET</html>".getBytes();
      } else {
        // create output buffer
        StringBuilder builder = new StringBuilder();
        // NOTE: output from buffer is at the end

        if (request.length() == 0) {
          // shows the default directory page

          // opens the root.html file
          String page = new String(readFileInBytes(new File("www/root.html")));
          // performs a template replacement in the page
          page = page.replace("${links}", buildFileList());

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(page);

        } else if (request.equalsIgnoreCase("json")) {
          // shows the JSON of a random image and sets the header name for that image

          // pick a index from the map
          int index = random.nextInt(_images.size());

          // pull out the information
          String header = (String) _images.keySet().toArray()[index];
          String url = _images.get(header);

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: application/json; charset=utf-8\n");
          builder.append("\n");
          builder.append("{");
          builder.append("\"header\":\"").append(header).append("\",");
          builder.append("\"image\":\"").append(url).append("\"");
          builder.append("}");

        } else if (request.equalsIgnoreCase("random")) {
          // opens the random image page

          // open the index.html
          File file = new File("www/index.html");

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(new String(readFileInBytes(file)));

        } else if (request.contains("file/")) {
          // tries to find the specified file and shows it or shows an error

          // take the path and clean it. try to open the file
          File file = new File(request.replace("file/", ""));

          // Generate response
          if (file.exists()) { // success
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Would theoretically be a file but removed this part, you do not have to do anything with it for the assignment");
          } else { // failure
            builder.append("HTTP/1.1 404 Not Found\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("File not found: " + file);
          }
        } else if (request.contains("multiply?")) {
          // This multiplies two numbers, with error handling
          // if num1 or num2 not provided or not valid, using default value 1

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          // split parameters with &
          String query = request.replace("multiply?","");
          String[] pairs = query.split("&");
          // flags if num1 and num2 default value
          boolean num1Default = false;
          boolean num2Default = false;
          String message = "";

          // put query values into map
          for (String pair : pairs) 
          {
              if (pair.contains("="))
              {
                  // split key and value with =
                  String[] keyValue = pair.split("=", 2);
                  String key = keyValue[0];
                  // set value empty if no value provided
                  String value = keyValue.length > 1 ? keyValue[1] : "";
                  // 
                  if (key.equals("num1") || key.equals("num2"))
                  {
                      query_pairs.put(key, value);
                  }
              }
              else
              {
                  // no = in num1 or num2 given
                  if (pair.equals("num1"))
                  {
                      query_pairs.put("num1", "1");
                      num1Default = true;
                      message += "num1 was provided without an equals sign, so it was defaulted to 1.<br>";
                  }
                  else if (pair.equals("num2"))
                  {
                      query_pairs.put("num2", "1");
                      num2Default = true;
                      message += "num2 was provided without an equals sign, so it was defaulted to 1.<br>";
                  }
              }
          }

          // num1 parameter missing
          if (!query_pairs.containsKey("num1"))
          {
              query_pairs.put("num1", "1");
              num1Default = true;
              message += "num1 parameter was missing, so it was defaulted to 1.<br>";
          }
          // num2 parameter missing
          if (!query_pairs.containsKey("num2"))
          {
              query_pairs.put("num2", "1");
              num2Default = true;
              message += "num2 parameter was missing, so it was defaulted to 1.<br>";
          }

          // num1 value empty
          if (query_pairs.get("num1").equals(""))
          {
              query_pairs.put("num1", "1");
              num1Default = true;
              message += "num1 value was not provided, so it was defaulted to 1.<br>";
          }
          // num2 value empty
          if (query_pairs.get("num2").equals(""))
          {
              query_pairs.put("num2", "1");
              num2Default = true;
              message += "num2 value was not provided, so it was defaulted to 1.<br>";
          }

          // default num1, num2
          Integer num1 = 1; 
          Integer num2 = 1;

          // check if valid num1
          try
          {
              num1 = Integer.parseInt(query_pairs.get("num1"));
          }
          catch (NumberFormatException e)
          {
              num1 = 1;
              num1Default = true;
              message += "num1 value was not a valid integer, so it was defaulted to 1.<br>";
          }
          // check if valid num2
          try
          {
              num2 = Integer.parseInt(query_pairs.get("num2"));
          }
          catch (NumberFormatException e)
          {
              num2 = 1;
              num2Default = true;
              message += "num2 value was not a valid integer, so it was defaulted to 1.<br>";
          }

          
          // build response body
          Integer result = num1 * num2;
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("<html><body>");
          builder.append("<h1>Multiplication result</h1>");

          // bold message for parameter issues
          if (num1Default || num2Default)
          {
              builder.append("<p><strong>Parameter issues:</strong></p>");
              builder.append("<p>").append(message).append("</p>");
          }

          builder.append("<p>Result is " +num1+ " * " +num2+ " = " +result+ "</p>");
          builder.append("</body></html>");

        } else if (request.contains("github?")) {
          // pulls the query from the request and runs it with GitHub's REST API
          // check out https://docs.github.com/rest/reference/
          //
          // HINT: REST is organized by nesting topics. Figure out the biggest one first,
          //     then drill down to what you care about
          // "Owner's repo is named RepoName. Example: find RepoName's contributors" translates to
          //     "/repos/OWNERNAME/REPONAME/contributors"

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          query_pairs = splitQuery(request.replace("github?", ""));
           //check if query missing
          if (!query_pairs.containsKey("query") || query_pairs.get("query").equals(""))
          {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n\n");
              builder.append("<html><body>");
              builder.append("<h1>Github Request Error</h1>");
              builder.append("<p>Query parameter missing or empty.</p>");
              builder.append("<p>Use format: /github?query=users/bejohn45/repos</p>");
              builder.append("</body></html>");
          }
          else
          {
              // call api 
              String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));
              System.out.println(json);
              String trimmed = (json==null) ? "" : json.trim();

              // empty response handling
              if (trimmed.isEmpty()) 
              {
                  builder.setLength(0); 
                  builder.append("HTTP/1.1 502 Bad Gateway\n");
                  builder.append("Content-Type: text/html; charset=utf-8\n\n");
                  builder.append("<html><body>");
                  builder.append("<h1>Github API Error</h1>");
                  builder.append("<p>No data returned from GitHub API.</p>");
                  builder.append("<p>The username or API path may be invalid.</p>");
                  builder.append("<p>Use format: /github?query=users/bejohn45/repos</p>");
                  builder.append("</body></html>");
              } 

              // check if array of repos returned
              else if (trimmed.startsWith("["))
              {
                  // stores repos as JSON array
                  JSONArray repos;
                  try 
                  {
                      repos = new JSONArray(trimmed);
                  } 
                  catch (JSONException e) {
                      // invalid data returned handling
                      repos = null;
                      builder.setLength(0); 
                      builder.append("HTTP/1.1 502 Bad Gateway\n");
                      builder.append("Content-Type: text/html; charset=utf-8\n\n");
                      builder.append("<html><body>");
                      builder.append("<h1>Github API Error</h1>");
                      builder.append("<p>Invalid JSON returned from GitHub API.</p>");
                      builder.append("</body></html>");
                  }
                  
                  // check if repos is not null 
                  if (repos != null)
                  {
                      builder.append("HTTP/1.1 200 OK\n");
                      builder.append("Content-Type: text/html; charset=utf-8\n\n");
                      builder.append("<html><body>");
                      builder.append("<h3>Public Repositories</h3>");

                      // check if no repos
                      if (repos.length() == 0)
                      {
                          builder.append("<p>No public repositories found for this user.</p>");
                      }
                      else
                      {
                          // iterate through repos
                          for (int i=0; i<repos.length(); i++) 
                          {
                              JSONObject repo = repos.getJSONObject(i);
                              String fullName = repo.optString("full_name", "(missing full_name)");
                              long id = repo.optLong("id", -1);
                              JSONObject owner = repo.optJSONObject("owner");
                              String login = (owner != null) ? owner.optString("login", "(missing login)") : "(missing owner)";

                              builder.append("full_name: ").append(fullName).append("<br>");
                              builder.append("id: ").append(id).append("<br>");
                              builder.append("owner_login: ").append(login).append("<br><br>");                      
                            
                          }
                          builder.append("</body></html>");
                      }

                  }
              }
              // github error object
              else if (trimmed.startsWith("{"))
              {
                  JSONObject obj;
                  try 
                  {
                      obj = new JSONObject(trimmed);
                  } 
                  catch (JSONException e) 
                  {
                      // invalid data returned handling
                      obj = null;
                      builder.setLength(0); 
                      builder.append("HTTP/1.1 502 Bad Gateway\n");
                      builder.append("Content-Type: text/html; charset=utf-8\n\n");
                      builder.append("<html><body>");
                      builder.append("<h1>Github API Error</h1>");
                      builder.append("<p>Invalid JSON returned from GitHub API.</p>");
                      builder.append("</body></html>");
                  }
                  // obj not null
                  if (obj != null)
                  {
                      String message = obj.optString("message", "");
                      // check if no message returned
                      if (message.equals(""))
                      {
                          message = "Github returned an error response without a message.";
                      }
                      String lower = message.toLowerCase();
                      // check if it's an unknown, rate limit error, authentication error
                      int status = 400;
                      String statusText = "Bad Request";
                      if (lower.contains("not found"))
                      {
                          status = 404;
                          statusText = "Not Found";
                      }
                      else if (lower.contains("rate limit"))
                      {
                          status = 503;
                          statusText = "Service Unavailable";
                      }
                      else if (lower.contains("bad credentials") || lower.contains("requires authentication"))
                      {
                          status = 401;
                          statusText = "Unauthorized";
                      }
      
                      builder.append("HTTP/1.1 " + status + " " + statusText + "\n");
                      builder.append("Content-Type: text/html; charset=utf-8\n\n");
                      builder.append("<html><body>");
                      builder.append("<h1>Github API Error</h1>");
                      builder.append("<p>").append(message).append("</p>");
                      builder.append("<p>Use format: /github?query=users/bejohn45/repos</p>");
                      builder.append("</body></html>");
                  }
              }
              // unexpected response
              else
              {
                  builder.append("HTTP/1.1 502 Bad Gateway\n");
                  builder.append("Content-Type: text/html; charset=utf-8\n\n");
                  builder.append("<html><body>");
                  builder.append("<h1>GitHub API Error</h1>");
                  builder.append("<p>Unexpected response from GitHub API.</p>");
                  builder.append("<p>Use format: /github?query=users/bejohn45/repos</p>");
                  builder.append("</body></html>");
              }      
          }
        }
        
        // request to compare 2 numbers, giving max and difference
        else if (request.contains("compare?"))
        {
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();
            // extract path parameters
            query_pairs = splitQuery(request.replace("compare?", ""));


            // check if parameters exist
            if (!query_pairs.containsKey("num1") || !query_pairs.containsKey("num2")) 
            {
                builder.append("HTTP/1.1 400 Bad Request\n");
                builder.append("Content-Type: text/html; charset=utf-8\n\n");
                builder.append("<html><body>");
                builder.append("<h1>Compare Request Error</h1>");
                builder.append("<p>Missing parameters! Include num1 and num2 in the query.</p>");
                builder.append("<p>Use format: /compare?num1=5&num2=10</p>");
                builder.append("</body></html>");
            }
            // check if values empty
            else if (query_pairs.get("num1").equals("") || query_pairs.get("num2").equals(""))
            {
                builder.append("HTTP/1.1 400 Bad Request\n");
                builder.append("Content-Type: text/html; charset=utf-8\n\n");
                builder.append("<html><body>");
                builder.append("<h1>Compare Request Error</h1>");
                builder.append("<p>Empty parameter values! Make sure num1 and num2 have values.</p>");
                builder.append("<p>Use format: /compare?num1=5&num2=10</p>");
                builder.append("</body></html>");
            }
            else
            {
                try 
                {
                    // extract required fields from parameters
                    Integer num1 = Integer.parseInt(query_pairs.get("num1"));
                    Integer num2 = Integer.parseInt(query_pairs.get("num2"));
                    builder.append("HTTP/1.1 200 OK\n");
                    builder.append("Content-Type: text/html; charset=utf-8\n\n");
                    builder.append("<html><body>");
                    builder.append("<h1>Compare Result</h1>");

                    // check if num1 or num2 bigger
                    if (num1 > num2)
                    {
                        builder.append("<p>").append(num1).append(" is greater than ").append(num2).append(".</p>");
                        builder.append("<p>Difference is ").append(num1 - num2).append(".</p>");
                    }
                    else if (num2 > num1)
                    {
                        builder.append("<p>").append(num2).append(" is greater than ").append(num1).append(".</p>");
                        builder.append("<p>Difference is ").append(num2 - num1).append(".</p>");
                    }
                    else
                    {
                        builder.append("<p>").append(num1).append(" and ").append(num2).append(" are equal. </p>");
                        builder.append("<p>Difference is 0.</p>");
                    }
                } 
                catch (NumberFormatException e) 
                {
                    builder.append("HTTP/1.1 400 Bad Request\n");
                    builder.append("Content-Type: text/html; charset=utf-8\n\n");
                    builder.append("<html><body>");
                    builder.append("<h1>Compare Request Error</h1>");
                    builder.append("<p>Invalid parameters! Please make sure num1 and num2 are integers.</p>");
                    builder.append("</body></html>");
                }
            }
        }
        
        else 
        {
          // if the request is not recognized at all

          builder.append("HTTP/1.1 400 Bad Request\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("I am not sure what you want me to do...");
        }

        // Output
        response = builder.toString().getBytes();
      }
    } catch (IOException e) {
      e.printStackTrace();
      response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
    }

    return response;
  }

  /**
   * Method to read in a query and split it up correctly
   * @param query parameters on path
   * @return Map of all parameters and their specific values
   * @throws UnsupportedEncodingException If the URLs aren't encoded with UTF-8
   */
  public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    // "q=hello+world%2Fme&bob=5"
    String[] pairs = query.split("&");
    // ["q=hello+world%2Fme", "bob=5"]
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
          URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    // {{"q", "hello world/me"}, {"bob","5"}}
    return query_pairs;
  }

  /**
   * Builds an HTML file list from the www directory
   * @return HTML string output of file list
   */
  public static String buildFileList() {
    ArrayList<String> filenames = new ArrayList<>();

    // Creating a File object for directory
    File directoryPath = new File("www/");
    filenames.addAll(Arrays.asList(directoryPath.list()));

    if (filenames.size() > 0) {
      StringBuilder builder = new StringBuilder();
      builder.append("<ul>\n");
      for (var filename : filenames) {
        builder.append("<li>" + filename + "</li>");
      }
      builder.append("</ul>\n");
      return builder.toString();
    } else {
      return "No files in directory";
    }
  }

  /**
   * Read bytes from a file and return them in the byte array. We read in blocks
   * of 512 bytes for efficiency.
   */
  public static byte[] readFileInBytes(File f) throws IOException {

    FileInputStream file = new FileInputStream(f);
    ByteArrayOutputStream data = new ByteArrayOutputStream(file.available());

    byte buffer[] = new byte[512];
    int numRead = file.read(buffer);
    while (numRead > 0) {
      data.write(buffer, 0, numRead);
      numRead = file.read(buffer);
    }
    file.close();

    byte[] result = data.toByteArray();
    data.close();

    return result;
  }

  /**
   *
   * a method to make a web request. Note that this method will block execution
   * for up to 20 seconds while the request is being satisfied. Better to use a
   * non-blocking request.
   * 
   * @param aUrl the String indicating the query url for the OMDb api search
   * @return the String result of the http request.
   *
   **/
  public String fetchURL(String aUrl) {
    StringBuilder sb = new StringBuilder();
    URLConnection conn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(aUrl);
      conn = url.openConnection();
      if (conn != null)
        conn.setReadTimeout(20 * 1000); // timeout in 20 seconds
      if (conn != null && conn.getInputStream() != null) {
        in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
        BufferedReader br = new BufferedReader(in);
        if (br != null) {
          int ch;
          // read the next character until end of reader
          while ((ch = br.read()) != -1) {
            sb.append((char) ch);
          }
          br.close();
        }
      }
      in.close();
    } catch (Exception ex) {
      System.out.println("Exception in url request:" + ex.getMessage());
    }
    return sb.toString();
  }
}
