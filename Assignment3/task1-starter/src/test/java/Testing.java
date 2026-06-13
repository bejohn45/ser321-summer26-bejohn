import org.junit.Test;
import static org.junit.Assert.*;
import org.json.JSONObject;
import mysteryservice.MysteryService;

public class Testing {

    // some tests for locally testing methods in the server, for this one the server does not need to run. 
    // It does not use networking it just calls Server methods on the SockServer!
    // Just an example you are not required to use this test
    @Test
    public void typeWrong() {
        JSONObject req = new JSONObject();
        req.put("type1", "echo");

        JSONObject res = SockServer.testField(req, "type");

        assertEquals(res.getBoolean("ok"), false);
        assertEquals(res.getString("message"), "Field type does not exist in request");
    }

    @Test
    public void echoCorrect() {
        JSONObject req = new JSONObject();
        req.put("type", "echo");
        req.put("data", "whooooo");
        JSONObject res = SockServer.echo(req);

        assertEquals("echo", res.getString("type"));
        assertEquals(res.getBoolean("ok"), true);
        assertEquals(res.getString("echo"), "Here is your echo: whooooo");
    }

    @Test
    public void echoErrors() {
        JSONObject req = new JSONObject();
        req.put("type", "echo");
        req.put("data1", "whooooo");
        JSONObject res = SockServer.echo(req);

        assertEquals(res.getBoolean("ok"), false);
        assertEquals(res.getString("message"), "Field data does not exist in request");

        JSONObject req2 = new JSONObject();
        req2.put("type", "echo");
        req2.put("data", 33);
        JSONObject res2 = SockServer.echo(req2);

        assertEquals(false, res2.getBoolean("ok"));
        assertEquals(res2.getString("message"), "Field data needs to be of type: String");

        JSONObject req3 = new JSONObject();
        req3.put("type", "echo");
        req3.put("data", true);
        JSONObject res3 = SockServer.echo(req3);

        assertEquals(res3.getBoolean("ok"), false);
        assertEquals(res3.getString("message"), "Field data needs to be of type: String");
    }

    @Test
    public void discoverAnalyzer()
    {
        // attempt 1- type as analyzer
        JSONObject req1 = new JSONObject();
        req1.put("type", "analyzer");
        JSONObject res1 = MysteryService.processRequest(req1);
        System.out.println(res1.toString());

        // attempt 2- type as analyzer, action as repeat
        JSONObject req2 = new JSONObject();
        req2.put("type", "analyzer");
        req2.put("action", "repeat");
        JSONObject res2 = MysteryService.processRequest(req2);
        System.out.println(res2.toString());

        // attempt 3- type as analyzer, action repeat, text as "hello"
        JSONObject req3 = new JSONObject();
        req3.put("type", "analyzer");
        req3.put("action", "repeat");
        req3.put("text", "hello");
        JSONObject res3 = MysteryService.processRequest(req3);
        System.out.println(res3.toString());

        // attempt 4- type as analyzer, action wordcount, text as "hello"
        JSONObject req4 = new JSONObject();
        req4.put("type", "analyzer");
        req4.put("action", "wordcount");
        req4.put("text", "hello");
        JSONObject res4 = MysteryService.processRequest(req4);
        System.out.println(res4.toString());

        // attempt 5- type as analyzer, action wordcount, text as "hello my name is bevin"
        JSONObject req5 = new JSONObject();
        req5.put("type", "analyzer");
        req5.put("action", "wordcount");
        req5.put("text", "hello my name is bevin");
        JSONObject res5 = MysteryService.processRequest(req5);
        System.out.println(res5.toString());

        // attempt 6- type as analyzer, action charcount, text as "hello my name is bevin"
        JSONObject req6 = new JSONObject();
        req6.put("type", "analyzer");
        req6.put("action", "charcount");
        req6.put("text", "hello my name is bevin");
        JSONObject res6 = MysteryService.processRequest(req6);
        System.out.println(res6.toString());

        // attempt 7- type as analyzer, action search, text as "hello my name is bevin"
        JSONObject req7 = new JSONObject();
        req7.put("type", "analyzer");
        req7.put("action", "search");
        req7.put("text", "hello my name is bevin");
        JSONObject res7 = MysteryService.processRequest(req7);
        System.out.println(res7.toString());

        // attempt 8- type as analyzer, action search, text as "hello my name is bevin", 'find' as 'bev'
        JSONObject req8 = new JSONObject();
        req8.put("type", "analyzer");
        req8.put("action", "search");
        req8.put("text", "hello my name is bevin");
        req8.put("find", "bev");
        JSONObject res8 = MysteryService.processRequest(req8);
        System.out.println(res8.toString());
    }
}