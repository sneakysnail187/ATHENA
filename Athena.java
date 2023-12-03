import java.util.Scanner;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.DriverManager;
import java.sql.Date;

public class Athena {

    public static void addBook(int libraryID) {

        String connectionUrl = "jdbc:sqlserver://cxp-sql-02\\djp161;"
        + "database=university;"
        + "user=sa;"
        + "password=*****"
        + "encrypt=true;"
        + "trustServerCertificate=true;"
        + "loginTimeout=3;";

        Scanner sc = new Scanner(System.in);
        String bookTitle, authorFirstName, authorLastName;
        String bookISBN, bookPublisher;
        Date datePublished;
        String 

        String selectAuthor = "{call dbo.selectAuthor(?, ?, ?)}";
        String insertAuthor = "{call dbo.insertAuthor(?, ?, ?)}";
        String insertWrote = "{call dbo.insertWrote(?, ?)}"
        String insertPhysicalCopy = "{call dbo.insertPhysicalCopy(?, ?)}"
        try (Connection connection =
            DriverManager.getConnection(connectionUrl);
            CallableStatement prepsStoredProc =
            connection.prepareCall(callStoredProc);)
        {
            connection.setAutoCommit(false);

            prepsStoredProc.setString(1, inpName);
            prepsStoredProc.setString(2, inpDeptName);
            prepsStoredProc.setInt(3, inpSalary);
            prepsStoredProc.registerOutParameter(4,
            java.sql.Types.INTEGER) ;
            prepsStoredProc.execute();

            System.out.println("Generated Identity: " +
            prepsStoredProc.getInt(4));

            connection.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void checkOutBook(int customerID) {
        
    }

    public static int getCustomerID() {
        Scanner sc = new Scanner(System.in);

        String customerFirstName, customerLastName;
        Date customerDOB;

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
                libraryUseCases();
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
