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

import com.sun.net.httpserver.*;


public class RESTaurantBooking {

    static Random rng = new Random(System.currentTimeMillis());
    static List<Table> tables = new ArrayList<Table>();
    static List<Booking> bookings = new ArrayList<Booking>();

    public static int randomNumberOfSeats() {
        return (rng.nextInt(3) + 1) * 2;
    }

    public static int randomNumberOfPeople() {
        return rng.nextInt(6)+1;
    }

    public static int randomNumberOfMinutes() {
        return rng.nextInt(4)*15;
    }

    public static void main(String[] args) {

        for(int i = 0; i < 25; i++) {
            Table t = new Table(i, randomNumberOfSeats());
            tables.add(t);
        }   

        int failed = 0;
        int total = 0;

        for(int i = 0; i < 15; i++) {
            for (int j = 9; j < 17; j++) {
                boolean bookingFailed = true;
                int count = 0;
                while (bookingFailed && count < 5) {
                    LocalTime bookingTime = LocalTime.of(j, randomNumberOfMinutes());
                    Booking newBooking = new Booking(randomNumberOfPeople(), bookingTime);
                    if(newBooking.tableID == -1) {
                        count++;
                        continue;
                    }
                    bookings.add(newBooking);
                    bookingFailed = false;
                    break;
                }
                if(bookingFailed) {
                    failed++;
                }
                total++;
            }
        }

        visualizeBookings();
    }


    public static int getBestTable(Booking booking) {
        int bestTable = -1;
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
