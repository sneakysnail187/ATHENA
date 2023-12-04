import java.util.Scanner;
import java.util.ArrayList;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.DriverManager;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.Objects;
import java.sql.Types;

public class Athena {
    private static String connectionUrl = "jdbc:sqlserver://cxp-sql-03\\mxr659;"
            + "database=athena;"
            + "user=sa;"
            + "password=YmQID.p4)D0s=K;"
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "loginTimeout=3;";

    private static Scanner sc = new Scanner(System.in);
    private static Integer userID = null;

    public static void newCustomer() {
        String getLocalLibrariesProc = "{call dbo.getLibraryByZip(?)}";
        String insertUserProc = "{call dbo.insertUser(?, ?, ?, ?, ?, ?)}";
        String firstName, lastName, middleInitial;
        Integer libID = null;
        String zip = "";
        Boolean validZip = false;
        while (!validZip) {
            System.out.println("Please enter your zipcode:");
            zip = sc.nextLine();
            try {
                @SuppressWarnings("unused")
                int z = Integer.parseInt(zip);
                if (zip.length() == 5) {validZip = true;}
                else {System.out.println("Please use a 5 digit zip");}
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format");
            }
        }
        System.out.println("Please enter your first name:");
        firstName = sc.nextLine();
        System.out.println("Please enter your last name:");
        lastName = sc.nextLine();
        System.out.println("Please enter your middle initial:");
        middleInitial = sc.nextLine();
        System.out.println("Please enter your DOB of form YYYY-MM-DD:");
        String dateraw = sc.nextLine();
        Date date = Date.valueOf(dateraw);

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsGetLibs = connection.prepareCall(getLocalLibrariesProc);)
        {
            prepsGetLibs.setString(1, zip);
            ResultSet resSet = prepsGetLibs.executeQuery();

            connection.commit();
            int i = 1;
            ArrayList<Integer> ids = new ArrayList<>();
            if (!resSet.isBeforeFirst()) {
                System.out.println("We're sorry, but there appear to be no libraries in this area.");
            } else {
                System.out.println("Select your prefered library:");
                while (resSet.next()) {
                    String bname = resSet.getString("branch_name");
                    ids.add(resSet.getInt("ID"));
                    System.out.println(i + ": " + bname);
                    i++;
                }
                int selection = sc.nextInt();
                sc.nextLine();
                libID = ids.get(selection - 1);
            }

            CallableStatement prepsInsertUser = connection.prepareCall(insertUserProc);
            prepsInsertUser.setString(1, firstName);
            prepsInsertUser.setString(2, middleInitial);
            prepsInsertUser.setString(3, lastName);
            prepsInsertUser.setInt(4, libID);
            prepsInsertUser.setDate(5, date);
            prepsInsertUser.registerOutParameter(6, java.sql.Types.INTEGER);
            prepsInsertUser.execute();
            connection.commit();
            userID = prepsInsertUser.getInt(6);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void returningCustomer() {
        String validateUser = "{call dbo.validateUser(?, ?, ?)}";

        boolean validated = false;
        while(!validated){
            System.out.println("Welcome!");
            System.out.println("Please enter your account ID:");
            int ID = sc.nextInt();
            sc.nextLine();
            System.out.println("Please enter your DOB of form YYYY-MM-DD:");
            String dateraw = sc.nextLine();
            Date date = Date.valueOf(dateraw);

            try (Connection connection = DriverManager.getConnection(connectionUrl);
                 CallableStatement prepsValidateUser = connection.prepareCall(validateUser);)
            {
                prepsValidateUser.setInt(1, ID);
                prepsValidateUser.setDate(2, date);
                prepsValidateUser.registerOutParameter(3, java.sql.Types.INTEGER);

                prepsValidateUser.execute();
                connection.commit();
                int res = prepsValidateUser.getInt(3);

                if (res == 1) {
                    userID = ID;
                    System.out.println("Succcesfully logged in");
                    validated = true;
                } else {
                    System.out.println("Error logging in, please try again");
                }
            }catch (SQLException E) {
                E.printStackTrace();
            }
        }
    }

    public static void addBook(int libraryID) {

        String bookTitle, authorFirstName, authorLastName;
        String bookISBN, bookPublisher;
        Date datePublished;

        String selectBook = "{call dbo.selectBook(?)}";
        String insertBook = "{call dbo.insertBook(?, ?, ?, ?, ?)}";
        String selectAuthor = "{call dbo.selectAuthor(?, ?, ?)}";
        String insertAuthor = "{call dbo.insertAuthor(?, ?, ?, ?)}";
        String insertWrote = "{call dbo.insertWrote(?, ?)}";
        String insertPhysicalCopy = "{call dbo.insertPhysicalCopy(?, ?, ?)}";

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsSelectBook = connection.prepareCall(selectBook);
             CallableStatement prepsInsertBook = connection.prepareCall(insertBook);
             CallableStatement prepsSelectAuthor = connection.prepareCall(selectAuthor);
             CallableStatement prepsInsertAuthor = connection.prepareCall(insertAuthor);
             CallableStatement prepsInsertWrote = connection.prepareCall(insertWrote);
             CallableStatement prepsInsertPhysicalCopy = connection.prepareCall(insertPhysicalCopy);)
        {
            // Get book info and insert into books

            System.out.println("What is the ISBN of the book you are adding?");
            String isbn = sc.nextLine();
            prepsSelectBook.setString(1, isbn);

            ResultSet books = prepsSelectBook.executeQuery();

            if (books.next()) {
                System.out.println(String.format("Book found: %s", books.getString("title")));
            } else {
                System.out.println("Book not found, please enter remaining information:");

                prepsInsertBook.setString(1, isbn);

                System.out.println("What is the title of the book you are adding?");
                String title = sc.nextLine();
                prepsInsertBook.setString(2, title);

                System.out.println("What is the genre of the book you are adding?");
                String genre = sc.nextLine();
                prepsInsertBook.setString(3, genre);

                System.out.println("Who is the publisher of the book you are adding?");
                String publisher = sc.nextLine();
                prepsInsertBook.setString(4, publisher);

                System.out.println("When was the book you are adding published? Enter in format YYYY-MM-DD");
                Date datePub = Date.valueOf(sc.nextLine());
                prepsInsertBook.setDate(5, datePub);

                prepsInsertBook.execute();
                System.out.println("Row inserted into book table");
            }

            // Get author info and check for existence, or insert into author

            System.out.println("What is the author's last name?");
            String lastName = sc.nextLine();
            prepsSelectAuthor.setString(1, lastName);

            boolean doneWithFirstName = false;
            String firstName = null;
            while (!doneWithFirstName) {
                System.out.println("Will you be entering a first name for the author? Y/N");
                String resp = sc.nextLine().toLowerCase();
                if (resp.equals("y")) {
                    System.out.println("What is the author's first name?");
                    firstName = sc.nextLine();
                    doneWithFirstName = true;
                } else if (resp.equals("n")) {
                    firstName = null;
                    doneWithFirstName = true;
                } else {
                    System.out.println("Invalid input, please input Y or N");
                }
            }
            if (Objects.isNull(firstName)) {
                prepsSelectAuthor.setNull(2, Types.VARCHAR);
            } else {
                prepsSelectAuthor.setString(2, firstName);
            }

            boolean doneWithMiddleInitial = false;
            String middleInitial = null;
            while (!doneWithMiddleInitial) {
                System.out.println("Will you be entering a middle initial for the author? Y/N");
                String resp = sc.nextLine().toLowerCase();
                if (resp.equals("y")) {
                    System.out.println("What is the author's middle initial?");
                    middleInitial = sc.nextLine();
                    if (middleInitial.length() > 1) {
                        System.out.println("Middle initial must be only one character long.");
                    } else {
                        doneWithMiddleInitial = true;
                    }
                } else if (resp.equals("n")) {
                    middleInitial = null;
                    doneWithMiddleInitial = true;
                } else {
                    System.out.println("Invalid input, please input Y or N");
                }
            }
            if (Objects.isNull(middleInitial)) {
                prepsSelectAuthor.setNull(3, Types.VARCHAR);
            } else {
                prepsSelectAuthor.setString(3, middleInitial);
            }

            ResultSet authors = prepsSelectAuthor.executeQuery();
            int authorID;

            if (!authors.next()) {
                System.out.println("No existing author found with this name, inserting a new row.");

                prepsInsertAuthor.setString(1, lastName);

                if (Objects.isNull(middleInitial)) {
                    prepsInsertAuthor.setNull(2, Types.VARCHAR);
                } else {
                    prepsInsertAuthor.setString(3, middleInitial);
                }

                if (Objects.isNull(middleInitial)) {
                    prepsInsertAuthor.setNull(3, Types.VARCHAR);
                } else {
                    prepsInsertAuthor.setString(3, middleInitial);
                }

                prepsInsertAuthor.registerOutParameter(4, Types.INTEGER);

                prepsInsertAuthor.execute();

                authorID = prepsInsertAuthor.getInt(4);
                System.out.println(String.format("A new row has been inserted into the author table, with ID %d", authorID));
            }
            else {
                System.out.println("The following author(s) were found that fit your search:");
                do {
                    System.out.println(String.format("%d: %s %s %s", authors.getInt("ID"), authors.getString("first_name"), authors.getString("middle_initial"), authors.getString("last_name")));
                } while (authors.next());

                System.out.println("Please enter the ID of the author who wrote the book.");
                authorID = sc.nextInt();
                sc.nextLine();
            }

            // insert into wrote

            prepsInsertWrote.setString(1, isbn);
            prepsInsertWrote.setInt(2, authorID);
            prepsInsertWrote.execute();

            // insert into physical copy

            prepsInsertPhysicalCopy.setString(1, isbn);
            prepsInsertPhysicalCopy.setInt(2, libraryID);
            prepsInsertPhysicalCopy.registerOutParameter(3, Types.INTEGER);

            System.out.println("How many copies of the book did the library acquire?");
            int numCopies = sc.nextInt();
            sc.nextLine();

            for (int i = 0; i < numCopies; i++) {
                prepsInsertPhysicalCopy.execute();
                connection.commit();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void search() {
        int customerID = userID;
        String searchProc = "";
        String info = "";
        Date datePub = new Date(System.currentTimeMillis());

        System.out.println("How would you like to search?");
        System.out.println("1. By title");
        System.out.println("2. By author");
        System.out.println("3. By publisher");
        System.out.println("4. By date published");
        System.out.println("5. By genre");
        System.out.println("6. Return to main menu");

        int selection = sc.nextInt();

        switch (selection) {
            case 1:
                searchProc = "{call.dbo.selectBookTitle(?)}";
                System.out.println("Enter title");
                info = sc.nextLine();
                break;
            case 2:
                searchProc = "{call.dbo.selectBookAuthor(?)}";
                System.out.println("Enter author's full name without spaces");
                info = sc.nextLine();
                break;
            case 3:
                searchProc = "{call.dbo.selectBookPub(?)}";
                System.out.println("Enter publisher");
                info = sc.nextLine();
                break;
            case 4:
                searchProc = "{call.dbo.selectBookDate(?)}";
                System.out.println("Enter date published in form: YYYY-MM-DD:");
                datePub = Date.valueOf(sc.nextLine());
                break;
            case 5:
                searchProc = "{call.dbo.selectBookGenre(?)}";
                System.out.println("Enter genre");
                info = sc.nextLine();
                break;
            case 6:
                return;
            default:
                System.out.println("Please input a number between 1 and 6 to make your selection.");
                break;
        }
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsInsertCheckedOut = connection.prepareCall(searchProc, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);)
        {
            if(selection == 3){
                prepsInsertCheckedOut.setDate(1, datePub);
            }
            else{
                prepsInsertCheckedOut.setString(1, info);
            }


            //prepsInsertCheckedOut.setDate(3, new Date(System.currentTimeMillis()));

            ResultSet r = prepsInsertCheckedOut.executeQuery();

            connection.commit();

            while (r.next()) {
                String title = r.getString("title");
                String genre = r.getString("genre");
                String publisher = r.getString("publisher");
                Date date = r.getDate("date_published");
                String lib = r.getString("library_ID");
                System.out.println("Title: " + title + "Genre: " + genre
                        + "Publisher: " + publisher + "Date Published: " + date + "Library ID: " + lib);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void placeHold() {
        String insertHoldProc = "{call dbo.insertHold(?, ?, ?)}";

        System.out.println("Please enter the book ID you would like to place on hold:");
        int bookId = sc.nextInt();
        sc.nextLine();
        System.out.println("When would you like this held until (YYYY-MM-DD)?");
        String dateraw = sc.nextLine();
        Date date = Date.valueOf(dateraw);

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsInsertHold = connection.prepareCall(insertHoldProc);)
        {
            prepsInsertHold.setInt(1, userID);
            prepsInsertHold.setInt(2, bookId);
            prepsInsertHold.setDate(3, date);
            prepsInsertHold.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeHold() {
        String removeHoldProc = "{call dbo.removeHold(?, ?)}";
        System.out.println("Please enter the book ID you would like to remove from hold:");
        int bookId = sc.nextInt();
        sc.nextLine();

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsRemoveHold = connection.prepareCall(removeHoldProc);)
        {
            prepsRemoveHold.setInt(1, bookId);
            prepsRemoveHold.setInt(2, userID);
            prepsRemoveHold.execute();
            connection.commit();

            System.out.println("Book ID " + bookId + " removed from hold");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void viewHolds() {
        String viewHoldProc = "{call dbo.viewHolds(?)}";

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsViewHold = connection.prepareCall(viewHoldProc);)
        {
            prepsViewHold.setInt(1, userID);
            ResultSet rs = prepsViewHold.executeQuery();
            connection.commit();

            System.out.println("Your current holds are:");
            while(rs.next()) {
                System.out.println("Book " + rs.getInt("book_ID") +
                        " on hold until " + rs.getString("hold_until"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageHolds() {
        boolean done = false;

        while (!done) {
            System.out.println("Please enter a number to select an action:");
            System.out.println("1. Place a hold");
            System.out.println("2. Cancel a hold");
            System.out.println("3. View my holds");
            System.out.println("4. Exit");
            int selection = sc.nextInt();
            sc.nextLine();
            switch (selection) {
                case 1:
                    placeHold();
                    break;
                case 2:
                    removeHold();
                    break;
                case 3:
                    viewHolds();
                    break;
                case 4:
                    done = true;
                    break;
                default:
                    System.out.println("Please input a number between 1 and 4 to make your selection.");
                    break;
            }
        }
    }

    public static void requestBook() {
        String requestProc = "{call dbo.insertRequest(?,?,?)}";
        int customerID = userID;

        String bookID;
        System.out.println("What is the ID of the book you would like to request?");
        bookID = sc.nextLine();
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsSelectBook = connection.prepareCall(requestProc);)
        {
            prepsSelectBook.setString(1, bookID);
            prepsSelectBook.setInt(2, getCustomerLibID(customerID));
            prepsSelectBook.setInt(3, getBookLibraryID(bookID));

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Request commited");
    }

    public static void checkOutBook(int customerID) {

        String checkOutProc = "{call dbo.insertCheckedOut(?, ?)}";
        String selectBook = "{call dbo.selectBookFromCopyID(?)}";

        int bookID;
        Scanner sc = new Scanner(System.in);

        boolean haveValidBookID = false;

        while (!haveValidBookID) {

            System.out.println("What is the ID of the book you would like to check out today?");
            bookID = sc.nextInt();
            sc.nextLine();

            // TODO verify book ID with select statement
            try (Connection connection = DriverManager.getConnection(connectionUrl);
                 CallableStatement prepsSelectBook = connection.prepareCall(selectBook);)
            {
                prepsSelectBook.setInt(1, bookID);
                ResultSet rs = prepsSelectBook.executeQuery();

                if (rs.next()) {
                    String isbn = rs.getString("isbn");
                    String title = rs.getString("title");
                    String author_name = rs.getString("author_name");
                    System.out.println(String.format("Attempting to check out %s: %s by %s", isbn, title, author_name));
                    haveValidBookID = true;
                }

                else {
                    System.out.println("Could not find the book with that ID, please try again.");
                }

            }
            catch (SQLException e) {
                e.printStackTrace();
            }

            try (Connection connection = DriverManager.getConnection(connectionUrl);
                 CallableStatement prepsInsertCheckedOut = connection.prepareCall(checkOutProc);)
            {
                prepsInsertCheckedOut.setInt(1, customerID);
                prepsInsertCheckedOut.setInt(2, bookID);

                boolean success = prepsInsertCheckedOut.execute();

                connection.commit();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getBookLibraryID(String bookIsbn) {

        String checkOutProc = "{call dbo.selectBookLibraryID(?)}";
        int libID = 0;

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsInsertCheckedOut = connection.prepareCall(checkOutProc,
                     ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);)
        {
            prepsInsertCheckedOut.setString(1, bookIsbn);

            ResultSet r = prepsInsertCheckedOut.executeQuery();
            libID = r.getInt("library_ID");
            connection.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return libID;
    }

    public static int getCustomerLibID(int customerID){

        String checkOutProc = "{call dbo.selectLibraryID(?, ?, ?)}";
        int libID = 0;

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement prepsInsertCheckedOut = connection.prepareCall(checkOutProc,
                     ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);)
        {
            prepsInsertCheckedOut.setInt(1, customerID);

            ResultSet r = prepsInsertCheckedOut.executeQuery();
            libID = r.getInt("ID");
            connection.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return libID;
    }

    public static void customerUseCases() {

        boolean done = false;

        System.out.println("Welcome! Let's start by figuring out who you are.");
        Scanner sc = new Scanner(System.in);

        while (!done) {
            System.out.println("Please enter a number to select an action:");
            System.out.println("1. Make a search");
            System.out.println("2. Check out a book from this library");
            System.out.println("3. Request a book from another library");
            System.out.println("4. Manage holds");
            System.out.println("5. Return to main menu");

            int selection = sc.nextInt();

            switch (selection) {
                case 1:
                    search();
                    break;
                case 2:
                    checkOutBook(userID);
                    break;
                case 3:
                    requestBook();
                    break;
                case 4:
                    manageHolds();
                    break;
                case 5:
                    done = true;
                    userID = null;
                    break;
                default:
                    System.out.println("Please input a number between 1 and 5 to make your selection.");
                    break;
            }

        }

    }

    public static void librarianUseCases() {
        Scanner sc = new Scanner(System.in);
        boolean done = false;

        System.out.println("Welcome! Which library are you working at?");
        System.out.println("Please enter the zip code of your library:");
        int zip = sc.nextInt();

        // TODO get libraryID from zip code
        int libraryID = 0;

        while (!done) {
            System.out.println("Please enter a number to select an action:");
            System.out.println("1. Add a book");
            System.out.println("2. Return to main menu");

            int selection = sc.nextInt();

            switch (selection) {
                case 1:
                    addBook(libraryID);
                    break;
                case 2:
                    done = true;
                    break;
                default:
                    System.out.println("Please input a number between 1 and 2 to make your selection.");
                    break;
            }
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        boolean done = false;

        System.out.println("Welcome to ATHENA.");

        while (!done) {
            System.out.println("How would you like to access the system today?");
            System.out.println("1. I am a new customer");
            System.out.println("2. I am a returning customer");
            System.out.println("3. I am a librarian");
            System.out.println("4. Exit");

            int selection = sc.nextInt();
            sc.nextLine();

            switch (selection) {
                case 1:
                    newCustomer();
                    customerUseCases();
                    break;
                case 2:
                    returningCustomer();
                    customerUseCases();
                    break;
                case 3:
                    librarianUseCases();
                    break;
                case 4:
                    done = true;
                    sc.close();
                    break;
                default:
                    System.out.println("Please input a number between 1 and 4 to make your selection.");
                    break;
            }
        }
    }
}