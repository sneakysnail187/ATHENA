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

public class Athena {
    private static String connectionUrl = "jdbc:sqlserver://cxp-sql-03\\mxr659;"
            + "database=athena;"
            + "user=sa;"
            + "password=[YOUR_PASSWORD];"
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

    public static void manageHolds() {

    }

    public static void manageBalance() {

    }

    public static void requestBook() {
        Scanner sc = new Scanner(System.in);

        String requestProc = "{call dbo.insertRequest(?,?,?,?)}";

        int bookID;
        System.out.println("What is the ID of the book you would like to request?");
        bookID = sc.nextInt();
    }

    public static void checkOutBook() {

        String checkOutProc = "{call dbo.insertCheckedOut(?, ?, ?)}";

        int bookID;
        Scanner sc = new Scanner(System.in);

        System.out.println("What is the ID of the book you would like to check out today?");
        bookID = sc.nextInt();

        // TODO verify book ID with select statement

        try (Connection connection = DriverManager.getConnection(connectionUrl);
            CallableStatement prepsInsertCheckedOut = connection.prepareCall(checkOutProc);) 
        {
            prepsInsertCheckedOut.setInt(1, userID);
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
        Scanner scanner = new Scanner(System.in);

        String customerFirstName, customerLastName;
        Date customerDOB;

        int customerID = 0;

        System.out.println("Please enter your first name:");
        customerFirstName = scanner.nextLine();

        System.out.println("Please enter your last name:");
        customerLastName = scanner.nextLine();

        // TODO figure out how to deal with dates in JDBC
        // System.out.println();
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

    public static void customerUseCases() {


        boolean done = false;

        System.out.println("Welcome! Let's start by figuring out who you are.");
        int customerID = getCustomerID();
        Scanner sc = new Scanner(System.in);
        if(userID == null) {
            userID = getCustomerID();
        }

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
                    search();
                    break;
                case 2:
                    checkOutBook();
                    break;
                case 3:
                    requestBook();
                    break;
                case 4:
                    manageHolds();
                    break;
                case 5:
                    manageBalance();
                    break;
                case 6:
                    done = true;
                    userID = null;
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

            switch (selection) {
                case 1:
                    newCustomer();
                    break;
                case 2:
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
                    System.out.println("Please input a number between 1 and 3 to make your selection.");
                    break;
            }
        }
    }
}
