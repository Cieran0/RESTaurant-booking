import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.Timestamp;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.sql.*;
import com.sun.net.httpserver.*;


public class RESTaurantBooking {

    static Random rng = new Random(System.currentTimeMillis());
    static List<Table> tables = new ArrayList<Table>();
    static List<Booking> bookings = new ArrayList<Booking>();

    public static void main(String[] args) {
        
        SqliteHandler.reloadTables();        
        SqliteHandler.reloadBookings(); 

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/bookings", new ApiHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port 8080...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean addBooking(int size, LocalTime time, String lastName) {
        SqliteHandler.reloadTables(); 
        SqliteHandler.reloadBookings(); 

        Booking booking = new Booking(size, time, lastName);

        if(booking.tableID == 0) {
            return false;
        }

        bookings.add(booking);

        SqliteHandler.writeBooking(booking);

        return true;
    }

    public static List<LocalTime> availableTimes(int size) {
        List<LocalTime> availableTimes = new ArrayList<>();

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(15)) {
            if(tableAvailable(size, time)) {
                availableTimes.add(time);
            }
        }

        return availableTimes;
    }


    public static boolean tableAvailable(int size, LocalTime time) {
        List<Integer> availableTables = new ArrayList<>();
    
        Map<Integer, List<Booking>> tableBookings = new HashMap<>();
        for (Booking b : bookings) {
            tableBookings.computeIfAbsent(b.tableID, k -> new ArrayList<>()).add(b);
        }
    
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).seats >= size) {
                availableTables.add(i);
            }
        }
    
        for (int tableId : availableTables) {
            List<Booking> tableBookingList = tableBookings.getOrDefault(tableId, new ArrayList<>());
    
            boolean isValidTable = true;
            for (Booking existingBooking : tableBookingList) {
                Duration duration = Duration.between(time, existingBooking.time);
                if (duration.toMinutes() < 75) {
                    isValidTable = false;
                    break;
                }
            }
    
            if (isValidTable) {
                return true;
            }
        }
    
        return false;
    }

    public static int getBestTable(Booking booking) {
        int bestTable = 0;
        int bestTimeGap = Integer.MAX_VALUE;
        int smallestSeatDifference = Integer.MAX_VALUE;
    
        List<Integer> availableTables = new ArrayList<>();
    
        Map<Integer, List<Booking>> tableBookings = new HashMap<>();
        for (Booking b : bookings) {
            tableBookings.computeIfAbsent(b.tableID, k -> new ArrayList<>()).add(b);
        }
    
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).seats >= booking.size) {
                availableTables.add(i);
            }
        }
    
        for (int tableId : availableTables) {
            Table table = tables.get(tableId);
            List<Booking> tableBookingList = tableBookings.getOrDefault(tableId, new ArrayList<>());
    
            boolean isValidTable = true;
            for (Booking existingBooking : tableBookingList) {
                Duration duration = Duration.between(booking.time, existingBooking.time);
                if (duration.toMinutes() < 75) {
                    isValidTable = false;
                    break;
                }
            }
    
            if (isValidTable) {
                int seatDifference = Math.abs(table.seats - booking.size);
                int closestTimeGap = Integer.MAX_VALUE;
    
                for (Booking otherBooking : tableBookingList) {
                    Duration duration = Duration.between(booking.time, otherBooking.time);
                    closestTimeGap = Math.min(closestTimeGap, (int) duration.toMinutes());
                }
    
                if (closestTimeGap < bestTimeGap || 
                   (closestTimeGap == bestTimeGap && seatDifference < smallestSeatDifference)) {
                    bestTimeGap = closestTimeGap;
                    smallestSeatDifference = seatDifference;
                    bestTable = tableId;
                }
            }
        }
    
        return bestTable;
    }

    public static void visualizeBookings() {
        System.out.println("Table Booking Visualization:");
        System.out.println("---------------------------");
    
        for (Table table : tables) {
            System.out.print("Table " + table.number + " (" + table.seats + " seats): ");
    
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(17, 0);
    
            for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(15)) {
                boolean isBooked = false;
                int numPeople = 0;
    
                for (Booking booking : bookings) {
                    if (booking.tableID == table.number && booking.time.equals(time)) {
                        isBooked = true;
                        numPeople = booking.size;
                        break;
                    }
                }
    
                if (isBooked) {
                    System.out.print("[" + time.toString() + ": " + numPeople + " people] ");
                } else {
                    
                    boolean isAvailable = true;
    
                    for (Booking booking : bookings) {
                        if (booking.tableID == table.number) {
                            // Check for bookings within the next 75 minutes
                            Duration duration = Duration.between(time, booking.time);
                            if (Math.abs(duration.toMinutes()) < 75) {
                                isAvailable = false;
                                break; // No need to check further
                            }
                        }
                    }
    
                    if (isAvailable) {
                        System.out.print("[" + time.toString() + ": Available] ");
                    } else {
                        System.out.print("[" + time.toString() + ": Unavailable (Booking within 75 minutes)] ");
                    }
                }
            }
    
            System.out.println();
        }
    }
}
