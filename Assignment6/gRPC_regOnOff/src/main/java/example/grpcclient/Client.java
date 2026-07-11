package example.grpcclient;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import service.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.protobuf.Empty; // needed to use Empty


/**
 * Client that requests `parrot` method from the `EchoServer`.
 */
public class Client {
  private final EchoGrpc.EchoBlockingStub blockingStub;
  private final JokeGrpc.JokeBlockingStub blockingStub2;
  private final RegistryGrpc.RegistryBlockingStub blockingStub3;
  private final RegistryGrpc.RegistryBlockingStub blockingStub4;
  private final ConverterGrpc.ConverterBlockingStub blockingStub5;

  /** Construct client for accessing server using the existing channel. */
  public Client(Channel channel, Channel regChannel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's
    // responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to
    // reuse Channels.
    blockingStub = EchoGrpc.newBlockingStub(channel);
    blockingStub2 = JokeGrpc.newBlockingStub(channel);
    blockingStub3 = RegistryGrpc.newBlockingStub(regChannel);
    blockingStub4 = RegistryGrpc.newBlockingStub(channel);
    blockingStub5 = ConverterGrpc.newBlockingStub(channel);
  }

  /** Construct client for accessing server using the existing channel. */
  public Client(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's
    // responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to
    // reuse Channels.
    blockingStub = EchoGrpc.newBlockingStub(channel);
    blockingStub2 = JokeGrpc.newBlockingStub(channel);
    blockingStub3 = null;
    blockingStub4 = null;
    blockingStub5 = null;
  }

  public void askServerToParrot(String message) {

    ClientRequest request = ClientRequest.newBuilder().setMessage(message).build();
    ServerResponse response;
    try {
      response = blockingStub.parrot(request);
    } catch (Exception e) {
      System.err.println("RPC failed: " + e.getMessage());
      return;
    }
    System.out.println("Received from server: " + response.getMessage());
  }

  public void askForJokes(int num) {
    JokeReq request = JokeReq.newBuilder().setNumber(num).build();
    JokeRes response;

    // just to show how to use the empty in the protobuf protocol
    Empty empt = Empty.newBuilder().build();

    try {
      response = blockingStub2.getJoke(request);
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
    System.out.println("Your jokes: ");
    for (String joke : response.getJokeList()) {
      System.out.println("--- " + joke);
    }
  }

  public void setJoke(String joke) {
    JokeSetReq request = JokeSetReq.newBuilder().setJoke(joke).build();
    JokeSetRes response;

    try {
      response = blockingStub2.setJoke(request);
      System.out.println(response.getOk());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void getNodeServices() {
    GetServicesReq request = GetServicesReq.newBuilder().build();
    ServicesListRes response;
    try {
      response = blockingStub4.getServices(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void getServices() {
    GetServicesReq request = GetServicesReq.newBuilder().build();
    ServicesListRes response;
    try {
      response = blockingStub3.getServices(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void findServer(String name) {
    FindServerReq request = FindServerReq.newBuilder().setServiceName(name).build();
    SingleServerRes response;
    try {
      response = blockingStub3.findServer(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void findServers(String name) {
    FindServersReq request = FindServersReq.newBuilder().setServiceName(name).build();
    ServerListRes response;
    try {
      response = blockingStub3.findServers(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }


  /**
   * Print menu of services
   */
  private static void printMenu()
  {
      System.out.println("\nAvailable services:");
      System.out.println("1- Echo");
      System.out.println("2- Joke");
      System.out.println("3- Converter");
      System.out.println("0- Exit");
      System.out.println("Choose a service (enter number): ");
  }
  

  /**
   * Handle user input for echo service
   */
  private static void handleEcho(Client client, BufferedReader reader) throws Exception
  {
      System.out.println("Enter a message to echo: ");
      String message = reader.readLine();
      // check if message is empty
      if (message.isEmpty())
      {
        System.out.println("Message cannot be empty.");
      }
      else
      {
        client.askServerToParrot(message);
      }
  }


  /**
   * Handle user input for joke service
   */
  private static void handleJoke(Client client, BufferedReader reader) throws Exception
  {
      // show joke menu
      System.out.println("Joke options:");
      System.out.println("1- Tell me a joke");
      System.out.println("2- Add a joke");
      System.out.print("Choose an option (enter number): ");
      String choice = reader.readLine();

      // check joke choice
      if (choice.equals("1"))
      {
          client.askForJokes(1);
      }
      else if (choice.equals("2"))
      {
          // ask for joke and add
          System.out.print("Enter a joke to add: ");
          String joke = reader.readLine();
          if (joke.isEmpty())
          {
              System.out.println("Joke cannot be empty");
          }
          else
          {
              client.setJoke(joke);
          }
      }
      // invalid choice
      else
      {
          System.out.println("Invalid choice");
      }
  }


  /**
   * Handle user input for converter service
   */
  private static void handleConverter(Client client, BufferedReader reader) throws Exception
  {
      // show available units
      System.out.println("Available units: ");
      System.out.println("  LENGTH      : KILOMETER, MILE, YARD, FOOT");
      System.out.println("  WEIGHT      : KILOGRAM, POUND");
      System.out.println("  TEMPERATURE : CELSIUS, FAHRENHEIT");

      // ask for value
      System.out.print("Enter value to convert: ");
      String valueString = reader.readLine();
      // get units and convert
      try
      {
          double value = Double.parseDouble(valueString);
          System.out.print("\nFrom unit:  ");
          String from = reader.readLine().trim().toUpperCase();
          System.out.print("\nTo unit:  ");
          String to = reader.readLine().trim().toUpperCase();
          client.convertUnits(value, from, to);
      } 
      catch (NumberFormatException e)
      {
          System.out.println("Invalid value");
      }
  }


  /**
   * Convert units from one unit to another
   */
  public void convertUnits(double value, String from, String to) 
  {
      ConversionRequest request = ConversionRequest.newBuilder().setValue(value).setFromUnit(from).setToUnit(to).build();
      ConversionResponse response;

      // send request to server
      try 
      {
          response = blockingStub5.convert(request);
      } 
      catch (Exception e) 
      {
          System.out.println("RPC failed: " + e.getMessage());
          return;
      }
      // print conversion result
      if (response.getIsSuccess())
      {
          System.out.println(value + " " + from + " = " + response.getResult() + " " + to);
      }
      else
      {
          System.out.println("Conversion failed: " + response.getError());
      }
  }



  public static void main(String[] args) throws Exception {
    if (args.length != 6) {
      System.out
          .println("Expected arguments: <host(String)> <port(int)> <regHost(string)> <regPort(int)> <message(String)> <regOn(bool)>");
      System.exit(1);
    }
    int port = 9099;
    int regPort = 9003;
    String host = args[0];
    String regHost = args[2];
    String message = args[4];
    try {
      port = Integer.parseInt(args[1]);
      regPort = Integer.parseInt(args[3]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port] must be an integer");
      System.exit(2);
    }

    // Create a communication channel to the server (Node), known as a Channel. Channels
    // are thread-safe
    // and reusable. It is common to create channels at the beginning of your
    // application and reuse
    // them until the application shuts down.
    String target = host + ":" + port;
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS
        // to avoid
        // needing certificates.
        .usePlaintext().build();

    String regTarget = regHost + ":" + regPort;
    ManagedChannel regChannel = ManagedChannelBuilder.forTarget(regTarget).usePlaintext().build();
    try {

      // ##############################################################################
      // ## Assume we know the port here from the service node it is basically set through Gradle
      // here.
      // In your version you should first contact the registry to check which services
      // are available and what the port
      // etc is.

      /**
       * Your client should start off with 
       * 1. contacting the Registry to check for the available services
       * 2. List the services in the terminal and the client can
       *    choose one (preferably through numbering) 
       * 3. Based on what the client chooses
       *    the terminal should ask for input, eg. a new sentence, a sorting array or
       *    whatever the request needs 
       * 4. The request should be sent to one of the
       *    available services (client should call the registry again and ask for a
       *    Server providing the chosen service) should send the request to this service and
       *    return the response in a good way to the client
       * 
       * You should make sure your client does not crash in case the service node
       * crashes or went offline.
       */

      // Just doing some hard coded calls to the service node without using the
      // registry
      // create client
      Client client = new Client(channel, regChannel);

      // // call the parrot service on the server
      // client.askServerToParrot(message);

      // ask the user for input how many jokes the user wants
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

      // main loop for menu
      boolean running = true;
      while (running)
      {
          printMenu();
          String choice = reader.readLine();
          // check what user wants
          if (choice.equals("0"))
          {
              running = false;
          }
          else if (choice.equals("1"))
          {
              handleEcho(client, reader);
          }
          else if (choice.equals("2"))
          {
              handleJoke(client, reader);
          }
          else if (choice.equals("3"))
          {
              handleConverter(client, reader);
          }
          else
          {
              System.out.println("Invalid choice");
          }
      }


      // // Reading data using readLine
      // System.out.println("How many jokes would you like?"); // NO ERROR handling of wrong input here.
      // String num = reader.readLine();

      // // calling the joked service from the server with num from user input
      // client.askForJokes(Integer.valueOf(num));

      // // adding a joke to the server
      // client.setJoke("I made a pencil with two erasers. It was pointless.");

      // // showing 6 joked
      // client.askForJokes(Integer.valueOf(6));

      // // list all the services that are implemented on the node that this client is connected to

      // System.out.println("Services on the connected node. (without registry)");
      // client.getNodeServices(); // get all registered services 

      // ############### Contacting the registry just so you see how it can be done

      // if (args[5].equals("true")) { 
      //   // Comment these last Service calls while in Activity 1 Task 1, they are not needed and wil throw issues without the Registry running
      //   // get thread's services
      //   client.getServices(); // get all registered services 

      //   // get parrot
      //   client.findServer("services.Echo/parrot"); // get ONE server that provides the parrot service
        
      //   // get all setJoke
      //   client.findServers("services.Joke/setJoke"); // get ALL servers that provide the setJoke service

      //   // get getJoke
      //   client.findServer("services.Joke/getJoke"); // get ALL servers that provide the getJoke service

      //   // does not exist
      //   client.findServer("random"); // shows the output if the server does not find a given service
      // }

    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent
      // leaking these
      // resources the channel should be shut down when it will no longer be used. If
      // it may be used
      // again leave it running.
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
      if (args[5].equals("true")) { 
        regChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
      }
    }
  }
}
