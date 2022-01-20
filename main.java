package at.koodi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Random;

public class Main {
    private static void PrintMenu() {
        System.out.println("Welcome to tikape-app. List of available commands:");
        System.out.println("1. Create database");
        System.out.println("2. Add a new location");
        System.out.println("3. Add a new customer");
        System.out.println("4. Add a new package");
        System.out.println("5. Add a new event");
        System.out.println("6. Get all events for a package");
        System.out.println("7. Get all packages for a customer and number of events");
        System.out.println("8. Get all events for a location on a given day");
        System.out.println("9. Performance test");
        System.out.println("Type in the number of the command you want to take. Type q to exit");
    }

    private static void CreateDatabase(Connection connection) {
        System.out.println("Creating databases");
        System.out.println("=========================================");
        System.out.println("Creating tables...");

        String sqlQuery =
                "IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Locations')\n "
                + "BEGIN\n "
                + "CREATE TABLE Locations(id int identity not null primary key, description varchar(255) not null)\n"
                + "END;"
                + "IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = N'Customers')\n"
                + "BEGIN\n"
                + "CREATE TABLE Customers(id int identity not null primary key, name varchar(255) not null)\n"
                + "END;"
                + "IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = N'Packages')\n"
                + "BEGIN\n"
                + "CREATE TABLE Packages(id int identity not null primary key, trackingcode varchar(255) not null, customer_id int FOREIGN KEY REFERENCES Customers(id))\n"
                + "END;"
                + "IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = N'Events')\n"
                + "BEGIN\n"
                + "CREATE TABLE Events(id int identity not null primary key, timestamp datetime not null default getdate(), package_id int FOREIGN KEY REFERENCES Packages(id), location_id int FOREIGN KEY REFERENCES Locations(id), description varchar(255) not null)\n"
                + "END;";

        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);

            statement.execute();

            System.out.println("Finished creating the database. Returning to main menu.\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when creating the database. Returning to main menu.\n");
        }
    }

    private static void AddNewLocation(Connection connection, String location) {
        System.out.println("Adding a new location");
        System.out.println("=========================================");

        String sqlQuery =
                "IF NOT EXISTS(SELECT * FROM Locations WHERE description=?)\n"
                + "BEGIN\n"
                + "INSERT INTO Locations(description) VALUES(?)\n"
                + "END;"
                + "ELSE SELECT * FROM Locations WHERE description=?;";

        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, location);
            statement.setString(2, location);
            statement.setString(3, location);

            statement.execute();

            if (statement.getResultSet() != null) {
                System.out.println(
                        "Location "
                        + location
                        + " already exists. Returning to main menu.\n");
            } else {
                System.out.println("Finished adding a new location. Returning to main menu.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when adding location. Returning to main menu.\n");
        }
    }

    private static void AddNewCustomer(Connection connection, String name) {
        System.out.println("Adding a new customer");
        System.out.println("=========================================");

        String sqlQuery =
                "IF NOT EXISTS(SELECT * FROM Customers WHERE name=?)\n"
                + "BEGIN\n"
                + "INSERT INTO Customers(name) VALUES (?)\n"
                + "END;"
                + "ELSE SELECT * FROM Customers WHERE name=?;";

        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, name);
            statement.setString(2, name);
            statement.setString(3, name);

            statement.execute();

            if (statement.getResultSet() != null) {
                System.out.println(
                        "Customer "
                        + name
                        + " already exists. Returning to main menu.\n");
            } else {
                System.out.println("Finished adding a new customer. Returning to main menu.\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when adding customer. Returning to main menu. \n");
        }
    }

    private static void AddNewPackage(Connection connection, String trackingCode, String customer) {
        System.out.println("Adding a new package to the system");
        System.out.println("=========================================");

        String sqlQuery = "SELECT * FROM Customers WHERE name=?;";

        int customerID = -1;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, customer);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Customer "
                        + customer
                        + " does not exist. Unable to add package. Returning to main menu.\n");

                return;
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    customerID = statement.getResultSet().getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when finding customer. Returning to main menu. \n");

            return;
        }

        if (customerID == -1) {
            return;
        }

        sqlQuery =
                "IF NOT EXISTS(SELECT * FROM Packages WHERE trackingcode=?)\n"
                + "BEGIN\n"
                + "INSERT INTO Packages(trackingcode, customer_id) VALUES (?, ?)\n"
                + "END;"
                + "ELSE SELECT * FROM Packages WHERE trackingcode=?;";

        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, trackingCode);
            statement.setString(2, trackingCode);
            statement.setInt(3, customerID);
            statement.setString(4, trackingCode);

            statement.execute();

            if (statement.getResultSet() != null) {
                System.out.println(
                        "Package with code "
                        + trackingCode
                        + " already exists. Returning to main menu.\n");
            } else {
                System.out.println("Finished adding a new package. Returning to main menu.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when adding package. Returning to main menu.\n");
        }
    }

    private static void AddNewEvent(Connection connection, String location, String trackingCode, String description) {
        System.out.println("Adding a new event");
        System.out.println("=========================================");

        String sqlQuery = "SELECT * FROM Locations WHERE description=?;";

        int locationId = -1;
        int packageId = -1;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, location);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Location "
                        + location
                        + " does not exist. Unable to add event. Returning to main menu.\n");

                return;
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    locationId = statement.getResultSet().getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when finding location. Returning to main menu.\n");

            return;
        }

        if (locationId == -1) {
            return;
        }

        sqlQuery = "SELECT * FROM Packages WHERE trackingCode=?;";

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, trackingCode);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Package with code "
                        + trackingCode
                        + " does not exist. Unable to add event. Returning to main menu.\n");

                return;
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    packageId = statement.getResultSet().getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when finding package. Returning to main menu.\n");

            return;
        }

        if (packageId == -1) {
            return;
        }

        sqlQuery = "INSERT INTO Events(package_id, location_id, description) VALUES (?, ?, ?);";

        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            statement.setInt(1, packageId);
            statement.setInt(2, locationId);
            statement.setString(3, description);

            statement.execute();

            System.out.println("Finished adding an event for package. Returning to main menu.\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when adding an event. Returning to main menu.\n");
        }
    }

    private static void GetAllEventsForPackage(Connection connection, String trackingCode) {
        System.out.println("Getting all events for a package: " + trackingCode);
        System.out.println("=========================================");

        String sqlQuery = "SELECT * FROM Packages WHERE trackingcode=?;";

        int packageId = -1;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, trackingCode);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Package with code "
                        + trackingCode
                        + " does not exist. Unable to retrieve events. Returning to main menu.\n");

                return;
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    packageId = statement.getResultSet().getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when finding package. Returning to main menu.\n");

            return;
        }

        if (packageId == -1) {
            System.out.println("Error in package id. Returning to main menu");

            return;
        }

        sqlQuery = "SELECT * FROM Events WHERE package_id=?";

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setInt(1, packageId);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Package with code "
                        + trackingCode
                        + " has no events. Returning to main menu.\n");
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    System.out.println(
                            statement.getResultSet().getString("timestamp")
                            + " "
                            + statement.getResultSet().getString("description"));

                System.out.println("No more events.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when fetching events. Returning to main menu.\n");
        }
    }

    private static void GetAllPackagesForCustomerWithNumbers(Connection connection, String customerName) {
        System.out.println("Getting all packages for a customer showing numbers of events");
        System.out.println("=========================================");

        String sqlQuery = "SELECT * FROM Customers WHERE name=?;";

        int customerId = -1;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, customerName);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Customer with name "
                        + customerName
                        + " does not exist. Returning to main menu.\n");

                return;
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    customerId = statement.getResultSet().getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when finding customer. Returning to main menu. \n");

            return;
        }

        if (customerId == -1) {
            return;
        }

        sqlQuery =
                "SELECT p.trackingcode, count(E.location_id) as events "
                + "FROM Packages p JOIN Events E ON p.id = E.package_id "
                + "WHERE p.customer_id = ? GROUP BY p.trackingcode;";

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setInt(1, customerId);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Customer "
                        + customerName
                        + " has no packages with events. Returning to main menu.\n");
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    System.out.println(
                            statement.getResultSet().getString("trackingcode")
                            + " "
                            + statement.getResultSet().getString("events"));

                System.out.println("No more packages with events.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when fetching packages. Returning to main menu.\n");
        }
    }

    private static void GetAllEventsForLocationOnGivenDay(Connection connection, String location, String date) {
        System.out.println("Getting all events for a location for a given day");
        System.out.println("=========================================");

        String sqlQuery = "SELECT * FROM Locations WHERE description=?;";

        int locationId = -1;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, location);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Location with name "
                        + location
                        + " does not exist. Returning to main menu.\n");
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    locationId = statement.getResultSet().getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when finding location. Returning to main menu.\n");
        }

        if (locationId == -1) {
            return;
        }

        sqlQuery = "SELECT * FROM Events WHERE location_id = ? AND datediff(day, timestamp, ?) = 0;";

        try {
            PreparedStatement statement = connection.prepareStatement(
                    sqlQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setInt(1, locationId);
            statement.setString(2, date);

            statement.execute();

            if (!statement.getResultSet().isBeforeFirst()) {
                System.out.println(
                        "Date "
                        + date
                        + " has no packages at the location. Returning to main menu.\n");
            } else {
                statement.getResultSet().beforeFirst();
                while (statement.getResultSet().next())
                    System.out.println(
                            statement.getResultSet().getString("timestamp")
                            + " "
                            + statement.getResultSet().getString("description"));
                System.out.println("No more events at the location for the day.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error when fetching events at location. Returning to main menu.\n");
        }
    }

    private static void DoPerformanceTest(Connection connection) {
        System.out.println("Starting performance test");
        System.out.println("=========================================");

        String sqlQuery1 = "INSERT INTO Locations (description) VALUES (?)";
        String sqlQuery2 = "INSERT INTO Customers (name) VALUES (?)";
        String sqlQuery3 = "INSERT INTO Packages (trackingcode, customer_id) VALUES ( ?, ?)";
        String sqlQuery4 = "INSERT INTO Events (package_id, location_id, description) VALUES( ?, ?, ?)";
        String sqlQuery5 = "SELECT count(id) FROM Packages WHERE customer_id = ?";
        String sqlQuery6 = "SELECT count(description) FROM Events WHERE package_id = ?";

        Random rand = new Random();

        try {
            connection.setAutoCommit(false);

            var startTime = System.currentTimeMillis();

            Statement sqlStatement = connection.createStatement();

            sqlStatement.execute("SET NOCOUNT ON");
            sqlStatement.execute("BEGIN TRANSACTION");

            PreparedStatement sqlCommand = connection.prepareStatement(sqlQuery1);
            for (int i = 1; i <= 1000; i++) {
                sqlCommand.setString(1, "P" + i);
                sqlCommand.addBatch();
            }
            sqlCommand.executeBatch();
            sqlCommand.close();

            sqlStatement.execute("COMMIT");

            var endTime = System.currentTimeMillis();

            System.out.println(
                    "Finished stage 1 in "
                    + (endTime - startTime));

            startTime = System.currentTimeMillis();

            sqlStatement.execute("BEGIN TRANSACTION");

            sqlCommand = connection.prepareStatement(sqlQuery2);
            for (int i = 1; i <= 1000; i++) {
                sqlCommand.setString(1, "A" + i);
                sqlCommand.addBatch();
            }
            sqlCommand.executeBatch();
            sqlCommand.close();

            sqlStatement.execute("COMMIT");

            endTime = System.currentTimeMillis();

            System.out.println(
                    "Finished stage 2 in "
                    + (endTime - startTime));

            startTime = System.currentTimeMillis();

            sqlStatement.execute("BEGIN TRANSACTION");

            sqlCommand = connection.prepareStatement(sqlQuery3);
            for (int i = 1; i <= 1000; i++) {
                sqlCommand.setString(1, "TC" + i);
                sqlCommand.setInt(2, rand.nextInt(1000));
                sqlCommand.addBatch();
            }
            sqlCommand.executeBatch();
            sqlCommand.close();

            sqlStatement.execute("COMMIT");

            endTime = System.currentTimeMillis();

            System.out.println(
                    "Finished stage 3 in "
                    + (endTime - startTime));

            startTime = System.currentTimeMillis();

            for (int mul = 1; mul <= 1000; mul++) {
                sqlStatement.execute("BEGIN TRANSACTION");

                sqlCommand = connection.prepareStatement(sqlQuery4);
                for (int i = 1; i <= 1000; i++) {
                    sqlCommand.setInt(1, rand.nextInt(1000));
                    sqlCommand.setInt(2, rand.nextInt(1000));
                    sqlCommand.setString(3, "Package registered.");
                    sqlCommand.addBatch();
                }
                sqlCommand.executeBatch();
                sqlCommand.close();

                sqlStatement.execute("COMMIT");
            }

            endTime = System.currentTimeMillis();

            System.out.println(
                    "Finished stage 4 in "
                    + (endTime - startTime));

            sqlStatement.execute("SET NOCOUNT OFF");

            connection.setAutoCommit(true);

            startTime = System.currentTimeMillis();

            for (int i = 1; i <= 1000; i++) {
                sqlCommand = connection.prepareStatement(sqlQuery5);
                sqlCommand.setInt(1, rand.nextInt(1000));
                sqlCommand.execute();
                sqlCommand.getResultSet().close();
                sqlCommand.close();
            }

            endTime = System.currentTimeMillis();

            System.out.println(
                    "Finished stage 5 in "
                    + (endTime - startTime));

            startTime = System.currentTimeMillis();

            for (int i = 1; i <= 1000; i++) {
                sqlCommand = connection.prepareStatement(sqlQuery6);
                sqlCommand.setInt(1, rand.nextInt(1000));
                sqlCommand.execute();
                sqlCommand.getResultSet().close();
                sqlCommand.close();
            }

            endTime = System.currentTimeMillis();

            System.out.println(
                    "Finished stage 6 in "
                    + (endTime - startTime));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Above error during performance test");
        }

        System.out.println("Finished performance test. Returning to main menu.");
    }

    private static Connection ConnectToDatabase(String url) {
        try {
            Connection connection = DriverManager.getConnection(url);

            String schema = connection.getSchema();

            System.out.println(
                    "Successful connection - Schema: "
                    + schema);

            return connection;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    private static void DoCommand(Connection connection, int commandIndex) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        switch (commandIndex) {
            case 0:
                System.out.println("Thank you for using tikape-app. (c) Tommi Venemies 2020");
                break;
            case 1:
                CreateDatabase(connection);
                break;
            case 2:
                System.out.println("Enter the name of the location to add:");

                try {
                    String location = br.readLine();

                    if (location.isBlank() || location.isEmpty()) {
                        System.out.println("Given location is empty. Returning to menu.\n");
                    } else {
                        AddNewLocation(connection, location);
                    }
                }
                catch (Exception e) {
                    System.out.println("Error in input. Returning to menu.\n");
                }

                break;
            case 3:
                System.out.println("Enter the name of the customer to add:");

                try {
                    String name = br.readLine();

                    if (name.isBlank() || name.isEmpty()) {
                        System.out.println("Given name is empty. Returning to menu.\n");
                    } else {
                        AddNewCustomer(connection, name);
                    }
                }
                catch (Exception e) {
                    System.out.println("Error in input. Returning to menu.\n");
                }

                break;
            case 4:
                System.out.println("Enter the name of the customer the package is for:");

                try {
                    String customer = br.readLine();

                    if (customer.isBlank() || customer.isEmpty()) {
                        System.out.println("Given name is empty. Returning to menu.\n");
                    } else {
                        System.out.println("Enter the tracking code for the package:");

                        String code = br.readLine();

                        if (code.isBlank() || code.isEmpty()) {
                            System.out.println("Given code is empty. Returning to menu.\n");
                        } else {
                            AddNewPackage(connection, code, customer);
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("Error in input. Returning to menu.\n");
                }

                break;
            case 5:
                System.out.println("Enter the location of the event:");
                try {
                    String locale = br.readLine();

                    if (locale.isBlank() || locale.isEmpty()) {
                        System.out.println("Given location is empty. Returning to menu.\n");
                    } else {
                        System.out.println("Enter the tracking code for the package:");

                        String code = br.readLine();

                        if (code.isBlank() || code.isEmpty()) {
                            System.out.println("Given code is empty. Returning to menu.\n");
                        } else {
                            System.out.println("Enter the description of the event:");

                            String description = br.readLine();

                            if (description.isBlank() || description.isEmpty()) {
                                System.out.println("Given description is empty. Returning to menu.\n");
                            } else {
                                AddNewEvent(connection, locale, code, description);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("Error in input. Returning to menu.\n");
                }

                break;
            case 6:
                System.out.println("Enter the code of the package to track:");

                try {
                    String packcode = br.readLine();

                    if (packcode.isBlank() || packcode.isEmpty()) {
                        System.out.println("Given code is empty. Returning to menu.\n");
                    } else {
                        GetAllEventsForPackage(connection, packcode);
                    }
                }
                catch (Exception e) {
                    System.out.println("Error in input. Returning to menu.\n");
                }

                break;
            case 7:
                System.out.println("Enter the name of the customer whose packages to track:");

                try {
                    String trackcustomer = br.readLine();

                    if (trackcustomer.isBlank() || trackcustomer.isEmpty()) {
                        System.out.println("Given name is empty. Returning to menu.\n");
                    } else {
                        GetAllPackagesForCustomerWithNumbers(connection, trackcustomer);
                    }
                }
                catch (Exception e) {
                    System.out.println("Error in input. Returning to menu.\n");
                }

                break;
            case 8:
                System.out.println("Enter the location to list:");

                try {
                    String loc = br.readLine();

                    if (loc.isBlank() || loc.isEmpty()) {
                        System.out.println("Given location is empty. Returning to menu.\n");
                    } else {
                        System.out.println("Enter the date (YYYY-MM-DD format):");

                        String date = br.readLine();

                        if (date.isBlank() || date.isEmpty()) {
                            System.out.println("Given date is empty. Returning to menu.\n");
                        } else {
                            GetAllEventsForLocationOnGivenDay(connection, loc, date);
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("Error in input. Returning to menu.\n");
                }

                break;
            case 9:
                DoPerformanceTest(connection);

                break;
            default:
                System.out.println("Unknown command. Enter a single digit between 1 and 9 or q to quit");

                break;
        }

        PrintMenu();
    }

    public static void main(String[] args) throws IOException {
        Connection connection = null;

        var hostName = "yourdatabaseaddress.windows.net";
        var dbName = "yourdbname";
        var user = "yourusername";
        var password = "yourpassword";
        var url = String.format(
                "jdbc:sqlserver:// %s:1433;database=%s;user=%s;password=%s;encrypt=true;"
                + "hostNameInCertificate=*.database.windows.net;loginTimeout=30;sendStringParameters AsUnicode=false;",
                hostName,
                dbName,
                user,
                password);

        connection = ConnectToDatabase(url);

        var br = new BufferedReader(new InputStreamReader(System.in));

        PrintMenu();

        var bExitApp = false;

        do {
            String command = br.readLine();

            int commandIndex = 0;

            if (command.matches("q"))
                bExitApp = true;
            else {
                try {
                    commandIndex = Integer.parseInt(command);
                } catch (NumberFormatException nfe) {
                    commandIndex = -1;
                }
            }

            DoCommand(connection, commandIndex);
        } while (!bExitApp);

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

