package example.grpcclient;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import service.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

/**
 * Implementation of library service
 */
public class LibraryImpl extends LibraryGrpc.LibraryImplBase
{
    private static final String INITIAL_FILE = "books.json";                // initial file to load books
    private static final String PERSISTENT_FILE = "library_data.json";      // file to persist library data
    private final ArrayList<BookData> library;                              // list of books

    // constructor
    public LibraryImpl()
    {
        super();
        library = new ArrayList<>();
        loadBooks();
    }

    /**
     * Inner class for book data
     */
    static class BookData
    {
        String title;
        String author;
        String isbn;
        boolean isBorrowed;
        String borrowedBy;
        String returnBy;
        // constructor
        public BookData(String title, String author, String isbn, boolean isBorrowed, String borrowedBy, String returnBy)
        {
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.isBorrowed = isBorrowed;
            this.borrowedBy = borrowedBy;
            this.returnBy = returnBy;
        }
        // convert to proto
        Book toProto()
        {
            return Book.newBuilder().setTitle(title).setAuthor(author).setIsbn(isbn).setIsBorrowed(isBorrowed).setBorrowedBy(borrowedBy).setReturnBy(returnBy).build();
        }
    }

    /**
     * Lists all books in library
     */
    @Override
    public void listBooks(Empty request, StreamObserver<BookListResponse> responseObserver)
    {
        System.out.println("Library: listBooks");
        BookListResponse.Builder response = BookListResponse.newBuilder();

        // check if library empty
        if (library.isEmpty())
        {
            response.setIsSuccess(false).setError("no books in library yet");
        }
        // add books to response
        else
        {
            for (BookData book : library)
            {
                response.addBooks(book.toProto());
            }
            response.setIsSuccess(true);
        }
        BookListResponse r = response.build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }

    @Override
    public void searchBooks(BookSearchRequest request, StreamObserver<BookListResponse> responseObserver)
    {
        System.out.println("Library: searchBooks with query = " + request.getQuery());
        BookListResponse.Builder response = BookListResponse.newBuilder();
        String query = request.getQuery().trim();

        // check if query or library empty
        if (query.isEmpty())
        {
            response.setIsSuccess(false).setError("missing field");
        }
        else if (library.isEmpty())
        {
            response.setIsSuccess(false).setError("no books in library yet");
        }
        // search for books matching
        else
        {
            ArrayList<Book> found = new ArrayList<>();
            for (BookData book : library)
            {
                // check if title or author match
                if ((book.title.toUpperCase().contains(query.toUpperCase())) || (book.author.toUpperCase().contains(query.toUpperCase())))
                {
                    found.add(book.toProto());
                }
            }
            // check if any books found
            if (found.isEmpty())
            {
                response.setIsSuccess(false).setError("no books found matching query");
            }
            else
            {
                response.setIsSuccess(true).addAllBooks(found);
            }
        }
        BookListResponse r = response.build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }

    @Override
    public void borrowBook(BorrowRequest request, StreamObserver<BorrowResponse> responseObserver)
    {
        System.out.println("Library: borrowBook with isbn = " + request.getIsbn());
        BorrowResponse.Builder response = BorrowResponse.newBuilder();
        
        // get isbn and borrower
        String isbn = request.getIsbn().trim();
        String borrower = request.getBorrowerName().trim();

        // check if fields missing
        if (isbn.isEmpty())
        {
            response.setIsSuccess(false).setError("missing field");
        }
        else if (borrower.isEmpty())
        {
            response.setIsSuccess(false).setError("borrower name is required");
        }
        // find book
        else
        {
            BookData book = findByIsbn(isbn);
            if (book == null)
            {
                response.setIsSuccess(false).setError("book not found");
            }
            else if (book.isBorrowed)
            {
                response.setIsSuccess(false).setError("book is already borrowed");
            }
            // borrow book
            else
            {
                book.isBorrowed = true;
                book.borrowedBy = borrower;
                book.returnBy = LocalDate.now().plusWeeks(2).toString();
                saveBooks();
                response.setIsSuccess(true).setMessage("\"" +book.title + "\" borrowed by " + book.borrowedBy + ". Due: " + book.returnBy);
            }
        }
        BorrowResponse r = response.build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }

    /**
     * Return borrowed book
     */
    @Override
    public void returnBook(ReturnRequest request, StreamObserver<ReturnResponse> responseObserver)
    {
        System.out.println("Library: returnBook with isbn = " + request.getIsbn());
        ReturnResponse.Builder response = ReturnResponse.newBuilder();
        
        // get isbn
        String isbn = request.getIsbn().trim();

        // check if field missing
        if (isbn.isEmpty())
        {
            response.setIsSuccess(false).setError("missing field");
        }
        // find book
        else
        {
            BookData book = findByIsbn(isbn);
            if (book == null)
            {
                response.setIsSuccess(false).setError("book not found");
            }
            else if (!book.isBorrowed)
            {
                response.setIsSuccess(false).setError("book is not borrowed");
            }
            // return book
            else
            {
                String borrower = book.borrowedBy;
                book.isBorrowed = false;
                book.borrowedBy = "";
                book.returnBy = "";
                saveBooks();
                response.setIsSuccess(true).setMessage("\"" +book.title+ "\" returned. Thank you, " +borrower+ "!");
            }
        }
        ReturnResponse r = response.build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }


    /**
     * Find book by ISBN
     */
    private BookData findByIsbn(String isbn)
    {
        for (BookData book : library)
        {
            if (book.isbn.equals(isbn))
            {
                return book;
            }
        }
        return null;
    }

    /**
     * Load books from library_data.json but load from books.json first run
     */
    private void loadBooks()
    {
        File file = new File(PERSISTENT_FILE);
        if (file.exists())
        {
            System.out.println("Library: loading from " + PERSISTENT_FILE);
            loadFromFile(file);
        }
        else
        {
            System.out.println("Library: first run, loading from " + INITIAL_FILE);
            loadInitially();
            saveBooks();
        }
    }

    /**
     * Load books from books.json first run
     */
    private void loadInitially()
    {
        File file = new File(INITIAL_FILE);
        // check if books.json is there
        if (!file.exists())
        {
            System.out.println("Initial file " + INITIAL_FILE + " not found. Library will start empty.");
            return;
        }
        // load books from file
        try
        {
            String data = new String(Files.readAllBytes(Paths.get(INITIAL_FILE)));
            JSONArray arr = new JSONArray(data);
            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject obj = arr.getJSONObject(i);
                String title = obj.getString("title");
                String author = obj.getString("author");
                String isbn = obj.getString("isbn");
                library.add(new BookData(title, author, isbn, false, "", ""));
            }
            System.out.println("Library: loaded " + library.size() + " books from " + INITIAL_FILE);
        }
        catch (Exception e)
        {
            System.out.println("Library: error loading books from " + INITIAL_FILE + ": " + e.getMessage());
        }
    }

    /**
     * Load books from library_data.json
     */
    private void loadFromFile(File file)
    {
        // load books from file
        try
        {
            String data = new String(Files.readAllBytes(file.toPath()));
            JSONArray arr = new JSONArray(data);
            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject obj = arr.getJSONObject(i);
                String title = obj.getString("title");
                String author = obj.getString("author");
                String isbn = obj.getString("isbn");
                boolean isBorrowed = obj.getBoolean("is_borrowed");
                String borrowedBy = obj.getString("borrowed_by");
                String returnBy = obj.getString("return_by");
                library.add(new BookData(title, author, isbn, isBorrowed, borrowedBy, returnBy));
            }
            System.out.println("Library: loaded " + library.size() + " books from " + file.getName());
        }
        catch (Exception e)
        {
            System.out.println("Library: error loading books from " + file.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Save books to library_data.json
     */
    private void saveBooks()
    {
        try
        {
            JSONArray arr = new JSONArray();
            for (BookData book : library)
            {
                JSONObject obj = new JSONObject();
                obj.put("title", book.title);
                obj.put("author", book.author);
                obj.put("isbn", book.isbn);
                obj.put("is_borrowed", book.isBorrowed);
                obj.put("borrowed_by", book.borrowedBy);
                obj.put("return_by", book.returnBy);
                arr.put(obj);
            }
            Files.write(Paths.get(PERSISTENT_FILE), arr.toString(2).getBytes());
        }
        catch (Exception e)
        {
            System.out.println("Library: error saving books to " + PERSISTENT_FILE + ": " + e.getMessage());
        }
    }
}
