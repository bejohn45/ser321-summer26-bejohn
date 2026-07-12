import com.google.protobuf.Empty;
import example.grpcclient.Client;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.JSONObject;
import service.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;

/**
 * Server unit tests for Assignment 6.
 *
 * IMPORTANT: These tests require the server to be running BEFORE you run them.
 *
 * To run these tests:
 * 1. First, start the server in one terminal: gradle runNode
 * 2. Then, in another terminal, run: gradle test
 *
 * The tests connect to localhost:8000 (the default port for runNode).
 * Make sure your server is running on this port before running tests.
 *
 * TODO for students:
 * This file contains example tests for the Echo and Joke services.
 * You need to add your own tests for:
 * - Converter service (happy path and error cases)
 * - Library service (happy path, error cases, and persistence testing)
 *
 * Your tests should follow the same pattern as the examples below.
 */
public class ServerTest {

    ManagedChannel channel;
    private EchoGrpc.EchoBlockingStub blockingStub;
    private JokeGrpc.JokeBlockingStub blockingStub2;
    private ConverterGrpc.ConverterBlockingStub blockingStub3;
    private LibraryGrpc.LibraryBlockingStub blockingStub4;


    @org.junit.Before
    public void setUp() throws Exception {
        // assuming default port and localhost for our testing, make sure Node runs on this port
        channel = ManagedChannelBuilder.forTarget("localhost:8000").usePlaintext().build();

        blockingStub = EchoGrpc.newBlockingStub(channel);
        blockingStub2 = JokeGrpc.newBlockingStub(channel);
        blockingStub3 = ConverterGrpc.newBlockingStub(channel);
        blockingStub4 = LibraryGrpc.newBlockingStub(channel);
    }

    @org.junit.After
    public void close() throws Exception {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

    }


    @Test
    public void parrot() {
        // success case
        ClientRequest request = ClientRequest.newBuilder().setMessage("test").build();
        ServerResponse response = blockingStub.parrot(request);
        assertTrue(response.getIsSuccess());
        assertEquals("test", response.getMessage());

        // error cases
        request = ClientRequest.newBuilder().build();
        response = blockingStub.parrot(request);
        assertFalse(response.getIsSuccess());
        assertEquals("No message provided", response.getError());

        request = ClientRequest.newBuilder().setMessage("").build();
        response = blockingStub.parrot(request);
        assertFalse(response.getIsSuccess());
        assertEquals("No message provided", response.getError());
    }

    // For this test the server needs to be started fresh AND the list of jokes needs to be the initial list
    @Test
    public void joke() {
        // getting first joke
        JokeReq request = JokeReq.newBuilder().setNumber(1).build();
        JokeRes response = blockingStub2.getJoke(request);
        assertEquals(1, response.getJokeCount());
        assertEquals("Did you hear the rumor about butter? Well, I'm not going to spread it!", response.getJoke(0));

        // getting next 2 jokes
        request = JokeReq.newBuilder().setNumber(2).build();
        response = blockingStub2.getJoke(request);
        assertEquals(2, response.getJokeCount());
        assertEquals("What do you call someone with no body and no nose? Nobody knows.", response.getJoke(0));
        assertEquals("I don't trust stairs. They're always up to something.", response.getJoke(1));

        // getting 2 more but only one more on server
        request = JokeReq.newBuilder().setNumber(2).build();
        response = blockingStub2.getJoke(request);
        assertEquals(2, response.getJokeCount());
        assertEquals("How do you get a squirrel to like you? Act like a nut.", response.getJoke(0));
        assertEquals("I am out of jokes...", response.getJoke(1));

        // trying to get more jokes but out of jokes
        request = JokeReq.newBuilder().setNumber(2).build();
        response = blockingStub2.getJoke(request);
        assertEquals(1, response.getJokeCount());
        assertEquals("I am out of jokes...", response.getJoke(0));

        // trying to add joke without joke field
        JokeSetReq req2 = JokeSetReq.newBuilder().build();
        JokeSetRes res2 = blockingStub2.setJoke(req2);
        assertFalse(res2.getOk());

        // trying to add empty joke
        req2 = JokeSetReq.newBuilder().setJoke("").build();
        res2 = blockingStub2.setJoke(req2);
        assertFalse(res2.getOk());

        // adding a new joke (well word)
        req2 = JokeSetReq.newBuilder().setJoke("whoop").build();
        res2 = blockingStub2.setJoke(req2);
        assertTrue(res2.getOk());

        // should have the new "joke" now and return it
        request = JokeReq.newBuilder().setNumber(1).build();
        response = blockingStub2.getJoke(request);
        assertEquals(1, response.getJokeCount());
        assertEquals("whoop", response.getJoke(0));
    }

    /**
     * Converter tests
     */
    @Test
    public void converter()
    {
        // km to mile
        ConversionRequest request = ConversionRequest.newBuilder().setValue(100).setFromUnit("KILOMETER").setToUnit("MILE").build();
        ConversionResponse response = blockingStub3.convert(request);
        assertTrue(response.getIsSuccess());
        assertEquals(62.14, response.getResult(), 0.01);

        // mile to km
        request = ConversionRequest.newBuilder().setValue(100).setFromUnit("MILE").setToUnit("KILOMETER").build();
        response = blockingStub3.convert(request);
        assertTrue(response.getIsSuccess());
        assertEquals(160.93, response.getResult(), 0.01);

        // yard to foot
        request = ConversionRequest.newBuilder().setValue(22).setFromUnit("YARD").setToUnit("FOOT").build();
        response = blockingStub3.convert(request);
        assertTrue(response.getIsSuccess());
        assertEquals(66, response.getResult(), 0.01);

        // foot to yard
        request = ConversionRequest.newBuilder().setValue(22).setFromUnit("FOOT").setToUnit("YARD").build();
        response = blockingStub3.convert(request);
        assertTrue(response.getIsSuccess());
        assertEquals(7.33, response.getResult(), 0.01);

        // celsius to fahrenheit
        request = ConversionRequest.newBuilder().setValue(15).setFromUnit("CELSIUS").setToUnit("FAHRENHEIT").build();
        response = blockingStub3.convert(request);
        assertTrue(response.getIsSuccess());
        assertEquals(59, response.getResult(), 0.01);

        // fahrenheit to celsius
        request = ConversionRequest.newBuilder().setValue(100).setFromUnit("FAHRENHEIT").setToUnit("CELSIUS").build();
        response = blockingStub3.convert(request);
        assertTrue(response.getIsSuccess());
        assertEquals(37.78, response.getResult(), 0.01);

        // kg to pound
        request = ConversionRequest.newBuilder().setValue(100).setFromUnit("KILOGRAM").setToUnit("POUND").build();
        response = blockingStub3.convert(request);
        assertTrue(response.getIsSuccess());
        assertEquals(220.46, response.getResult(), 0.01);

        // pound to kg
        request = ConversionRequest.newBuilder().setValue(45).setFromUnit("POUND").setToUnit("KILOGRAM").build();
        response = blockingStub3.convert(request);
        assertTrue(response.getIsSuccess());
        assertEquals(20.41, response.getResult(), 0.01);


        // error case: missing from_unit
        request = ConversionRequest.newBuilder().setValue(100).setToUnit("KILOGRAM").build();
        response = blockingStub3.convert(request);
        assertFalse(response.getIsSuccess());
        assertEquals("from_unit is required", response.getError());

        // error case: missing to_unit
        request = ConversionRequest.newBuilder().setValue(100).setFromUnit("POUND").build();
        response = blockingStub3.convert(request);
        assertFalse(response.getIsSuccess());
        assertEquals("to_unit is required", response.getError());

        // error case: cannot convert between units
        request = ConversionRequest.newBuilder().setValue(100).setFromUnit("POUND").setToUnit("CELSIUS").build();
        response = blockingStub3.convert(request);
        assertFalse(response.getIsSuccess());
        assertEquals("units do not match - cannot convert POUND to CELSIUS", response.getError());

        // error case: same unit
        request = ConversionRequest.newBuilder().setValue(100).setFromUnit("POUND").setToUnit("POUND").build();
        response = blockingStub3.convert(request);
        assertFalse(response.getIsSuccess());
        assertEquals("same unit - no conversion needed", response.getError());
        
        // error case: unsupported unit
        request = ConversionRequest.newBuilder().setValue(100).setFromUnit("GRAM").setToUnit("KILOGRAM").build();
        response = blockingStub3.convert(request);
        assertFalse(response.getIsSuccess());
        assertTrue(response.getError().contains("unsupported unit:"));

        // error case: below absolute zero
        request = ConversionRequest.newBuilder().setValue(-500).setFromUnit("FAHRENHEIT").setToUnit("CELSIUS").build();
        response = blockingStub3.convert(request);
        assertFalse(response.getIsSuccess());
        assertTrue(response.getError().contains("absolute zero"));
    }


    /**
     * Library tests
     */
    @Test
    public void library()
    {
        // list books
        BookListResponse listResponse = blockingStub4.listBooks(Empty.newBuilder().build());
        assertTrue(listResponse.getIsSuccess());
        assertTrue(listResponse.getBooksCount() > 0);

        // search with title
        BookListResponse searchResponse = blockingStub4.searchBooks(BookSearchRequest.newBuilder().setQuery("1984").build());
        assertTrue(searchResponse.getIsSuccess());
        assertTrue(searchResponse.getBooksCount() > 0);
        assertEquals("1984", searchResponse.getBooks(0).getTitle());

        // search with author
        searchResponse = blockingStub4.searchBooks(BookSearchRequest.newBuilder().setQuery("Orwell").build());
        assertTrue(searchResponse.getIsSuccess());
        assertTrue(searchResponse.getBooksCount() > 0);
        assertEquals("1984", searchResponse.getBooks(0).getTitle());

        // borrow book
        String isbn = "978-0451524935";
        // return so available
        blockingStub4.returnBook(ReturnRequest.newBuilder().setIsbn(isbn).build());
        BorrowResponse borrowResponse = blockingStub4.borrowBook(BorrowRequest.newBuilder().setIsbn(isbn).setBorrowerName("Bevin").build());
        assertTrue(borrowResponse.getIsSuccess());
        assertTrue(borrowResponse.getMessage().contains("Bevin"));
        assertTrue(borrowResponse.getMessage().contains("Due: "));
        // return book
        ReturnResponse returnResponse = blockingStub4.returnBook(ReturnRequest.newBuilder().setIsbn(isbn).build());
        assertTrue(returnResponse.getIsSuccess());
        assertTrue(returnResponse.getMessage().contains("Bevin"));
        assertTrue(returnResponse.getMessage().contains("returned"));


        // error case: search with empty query
        searchResponse = blockingStub4.searchBooks(BookSearchRequest.newBuilder().setQuery("").build());
        assertFalse(searchResponse.getIsSuccess());
        assertEquals("missing field", searchResponse.getError());

        // error case: search with no results
        searchResponse = blockingStub4.searchBooks(BookSearchRequest.newBuilder().setQuery("Harry Potter").build());
        assertFalse(searchResponse.getIsSuccess());
        assertEquals("no books found matching query", searchResponse.getError());

        // error case: borrow invalid isbn
        borrowResponse = blockingStub4.borrowBook(BorrowRequest.newBuilder().setIsbn("111-010100").setBorrowerName("John").build());
        assertFalse(borrowResponse.getIsSuccess());
        assertEquals("book not found", borrowResponse.getError());
    }


    // library persistence test
    @Test
    public void libraryPersistence() throws Exception
    {
        String isbn = "978-0451524935";
        // return so available
        blockingStub4.returnBook(ReturnRequest.newBuilder().setIsbn(isbn).build());

        // borrow book to verify saveBooks() works
        BorrowResponse borrowResponse = blockingStub4.borrowBook(BorrowRequest.newBuilder().setIsbn(isbn).setBorrowerName("Bevin").build());
        assertTrue(borrowResponse.getIsSuccess());

        // verify library_data.json created
        File file = new File("library_data.json");
        assertTrue(file.exists());

        String fileData = new String(Files.readAllBytes(file.toPath()));
        assertTrue(fileData.contains(isbn));
        assertTrue(fileData.contains("Bevin"));
        assertTrue(fileData.contains("\"is_borrowed\": true"));

        // check server shows as borrowed
        BookListResponse listResponse = blockingStub4.listBooks(Empty.newBuilder().build());
        Book book = null;
        for (Book b : listResponse.getBooksList())
        {
            if (b.getIsbn().equals(isbn))
            {
                book = b;
                break;
            }
        }
        assertNotNull(book);
        assertTrue(book.getIsBorrowed());
        assertEquals("Bevin", book.getBorrowedBy());
        // return
        blockingStub4.returnBook(ReturnRequest.newBuilder().setIsbn(isbn).build());
    }

}