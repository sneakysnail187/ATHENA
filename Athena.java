import java.util.Scanner;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Date;

public class Athena {

    private static String connectionUrl = "jdbc:sqlserver://cxp-sql-02\\djp161;"
        + "database=ATHENA;"
        + "user=sa;"
        + "password=;YC'BY?!h!b37yx0;"
        + "encrypt=true;"
        + "trustServerCertificate=true;"
        + "loginTimeout=3;";

    public static void addBook(int libraryID) {

        Scanner sc = new Scanner(System.in);
        String bookTitle, authorFirstName, authorLastName;
        String bookISBN, bookPublisher;
        Date datePublished;

        String selectAuthor = "{call dbo.selectAuthor(?, ?, ?)}";
        String insertAuthor = "{call dbo.insertAuthor(?, ?, ?)}";
        String insertWrote = "{call dbo.insertWrote(?, ?)}";
        String insertPhysicalCopy = "{call dbo.insertPhysicalCopy(?, ?)}";

    }

    public static void search(int customerID) throws ParseException {
        Scanner sc = new Scanner(System.in);
        boolean done  = false;
        String searchProc = "";
        String title = "";
        String pub = "";
        String genre = "";
        Date datePub = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");

        System.out.println("What is the ID of the book are you searching for?");
        int bookISBN = sc.nextInt();

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
                    searchProc = "{call.dbo.selectBookTitle(?,?,?)}";
                    System.out.println("Enter title");
                    title = sc.nextLine();
                    break;
                case 2:
                    searchProc = "{call.dbo.selectBookAuthor(?,?,?,?)}";
                    break;
                case 3:
                    searchProc = "{call.dbo.selectBookPub(?,?,?)}";
                    System.out.println("Enter publisher");
                    pub = sc.nextLine();
                    break;
                case 4:
                    searchProc = "{call.dbo.selectBookDate(?,?,?)}";
                    System.out.println("Enter date published");
                    datePub = (Date) formatter.parse(sc.nextLine());
                    break;
                case 5:
                    searchProc = "{call.dbo.selectBookGenre(?,?,?)}";
                    System.out.println("Enter genre");
                    genre = sc.nextLine();
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
            prepsInsertCheckedOut.setInt(1, bookISBN);
            prepsInsertCheckedOut.setInt(2, getCustomerLibID(customerID));
            switch (selection) {
                case 1: 
                    prepsInsertCheckedOut.setString(3, title);
                    break;
                case 2:
                    //prepsInsertCheckedOut.setString(3, title);
                    break;
                case 3:
                    prepsInsertCheckedOut.setString(3, pub);
                    break;
                case 4:
                    prepsInsertCheckedOut.setDate(3, datePub);
                    break;
                case 5:
                    prepsInsertCheckedOut.setString(3, genre);
                    break;
                default:
                    break;
            }
            //prepsInsertCheckedOut.setDate(3, new Date(System.currentTimeMillis()));

            prepsInsertCheckedOut.execute();

            connection.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageHolds(int customerID) {
        // TODO implement this use case
    }

    public static void requestBook(int customerID) {
        Scanner sc = new Scanner(System.in);

        String requestProc = "{call dbo.insertRequest(?,?,?,?)}";

        int bookID;
        System.out.println("What is the ID of the book you would like to request?");
        bookID = sc.nextInt();

    }

    public static void manageBalance(int customerID) {
        // TODO implement this use case
    }

    public static void checkOutBook(int customerID) {

        String checkOutProc = "{call dbo.insertCheckedOut(?, ?, ?)}";

        int bookID;
        Scanner sc = new Scanner(System.in);

        System.out.println("What is the ID of the book you would like to check out today?");
        bookID = sc.nextInt();

        // TODO verify book ID with select statement

        try (Connection connection = DriverManager.getConnection(connectionUrl);
            CallableStatement prepsInsertCheckedOut = connection.prepareCall(checkOutProc);) 
        {
            prepsInsertCheckedOut.setInt(1, customerID);
            prepsInsertCheckedOut.setInt(2, bookID);
            prepsInsertCheckedOut.setDate(3, new Date(System.currentTimeMillis()));

            prepsInsertCheckedOut.execute();

            connection.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getCustomerID() {
        Scanner sc = new Scanner(System.in);

        String customerFirstName, customerLastName;
        Date customerDOB;

        int customerID = 0;

        System.out.println("Please enter your first name:");
        customerFirstName = sc.nextLine();

        System.out.println("Please enter your last name:");
        customerLastName = sc.nextLine();

        // TODO figure out how to deal with dates in JDBC
        System.out.println();


        sc.close();
        return customerID;
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

    public static void customerUseCases() throws ParseException {

        Scanner sc = new Scanner(System.in);
        boolean done = false;

        System.out.println("Welcome! Let's start by figuring out who you are.");
        int customerID = getCustomerID();

        while (!done) {
            System.out.println("Please enter a number to select an action:");
            System.out.println("1. Make a search");
            System.out.println("2. Check out a book from this library");
            System.out.println("3. Request a book from another library");
            System.out.println("4. Manage holds");
            System.out.println("5. Manage balance");
            System.out.println("6. Return to main menu");

            int selection = sc.nextInt();

            switch (selection) {
                case 1: 
                    search(customerID); 
                    break;
                case 2:
                    checkOutBook(customerID);
                    break;
                case 3:
                    requestBook(customerID);
                    break;
                case 4:
                    manageHolds(customerID);
                    break;
                case 5:
                    manageBalance(customerID);
                    break;
                case 6:
                    done = true;
                    break;
                default:
                    System.out.println("Please input a number between 1 and 6 to make your selection.");
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

    public static void main(String[] args) throws ParseException {

        Scanner sc = new Scanner(System.in);
        boolean done = false;

        System.out.println("Welcome to ATHENA.");
        
        while (!done){
            System.out.println("How would you like to access the system today?");
            System.out.println("1. As a customer");
            System.out.println("2. As a librarian");
            System.out.println("3. Exit");

            int selection = sc.nextInt();

            switch (selection) {
             case 1: 
                customerUseCases();
                break;
             case 2:
                librarianUseCases();
                break;
             case 3:
                done = true;
                break;
             default:
                System.out.println("Please input a number between 1 and 3 to make your selection.");
                break;
            }

        }
        sc.close();
    }
    
}
