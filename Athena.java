import java.util.Scanner;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.DriverManager;
import java.sql.Date;

public class Project {

    private static String connectionUrl = "jdbc:sqlserver://cxp-sql-02\\djp161;"
        + "database=university;"
        + "user=sa;"
        + "password=c?QoD2K.]^b:}(;"
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

    public static void customerUseCases() {

        Scanner sc = new Scanner(System.in);
        boolean done = false;

        System.out.println("Welcome? Let's start by figuring out who you are.");
        int customerID = getCustomerID();

        while (!done) {
            System.out.println("Please enter a number to select an action:");
            System.out.println("1. Make a search");
            System.out.println("2. Check out a book from this library");
            System.out.println("3. Request a book from another library");
            System.out.println("4. Manage holds");
            System.out.println("5. Manage balance");
            System.out.println("6. Return to main menu")

            int selection = sc.nextInt();

            switch (selection) {
                case 1:
                    break;
                case 2:
                    checkOutBook(customerID);
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
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

    }

    public static void main(String[] args) {

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
                sc.close();
                break;
             default:
                System.out.println("Please input a number between 1 and 3 to make your selection.");
                break;
            }

        }
    }
    
}
