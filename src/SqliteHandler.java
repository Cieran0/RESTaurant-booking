import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class SqliteHandler {

    public static void reloadTables() {
        String url = "jdbc:sqlite:./bookings.db";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found: " + e.getMessage());
        }
        try (Connection conn = DriverManager.getConnection(url)) {
            

            if (conn != null) {
                System.out.println("Connected to the database.");


                String selectSQL = "SELECT * FROM Tables;";
                try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(selectSQL)) {
                    List<Table> newTables = new ArrayList<Table>(); 
                    newTables.add(0, new Table(0, -1));
                    while (rs.next()) {
                        int id = rs.getInt("TableNumber");
                        int seats = rs.getInt("Seats");
                        newTables.add(id, new Table(id, seats));
                    }
                    RESTaurantBooking.tables = newTables;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void reloadBookings() {
        String url = "jdbc:sqlite:./bookings.db";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found: " + e.getMessage());
        }
        try (Connection conn = DriverManager.getConnection(url)) {
            

            if (conn != null) {
                System.out.println("Connected to the database.");


                String selectSQL = "SELECT * FROM Booking;";
                try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(selectSQL)) {
                    List<Booking> newBookings = new ArrayList<Booking>(); 
                    while (rs.next()) {
                        int id = rs.getInt("TableNo");
                        int size = rs.getInt("Size");
                        String time_string = rs.getString("Time");
                        String lastName = rs.getString("Last Name");

                        LocalTime time = LocalTime.parse(time_string);

                        newBookings.add(new Booking(size, time, lastName, id));
                    }
                    RESTaurantBooking.bookings = newBookings;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void writeBooking(Booking booking) {
        String url = "jdbc:sqlite:./bookings.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Connected to the database.");
    
                String maxIdSQL = "SELECT MAX(ID) AS maxId FROM Booking;";
                int newId = 1;
    
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(maxIdSQL)) {
                    if (rs.next()) {
                        newId = rs.getInt("maxId") + 1;
                    }
                }
    
                String insertSQL = "INSERT INTO Booking (ID, TableNo, Size, Time, [Last Name]) VALUES (?, ?, ?, ?, ?);";
    
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setInt(1, newId);
                    pstmt.setInt(2, booking.tableID);
                    pstmt.setInt(3, booking.size);
                    pstmt.setString(4, booking.time.toString());
                    pstmt.setString(5, booking.lastName);
    
                    int rowsInserted = pstmt.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("Booking successfully added to the database with ID: " + newId);
                    } else {
                        System.out.println("Failed to add booking to the database.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

}
