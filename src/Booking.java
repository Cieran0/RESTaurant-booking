import java.time.LocalTime;

public class Booking {
    
    int tableID;
    int size;
    String lastName;
    LocalTime time;

    public Booking(int size, LocalTime time, String lastName) {
        this.size = size;
        this.time = time;
        this.lastName = lastName;
        this.tableID = RESTaurantBooking.getBestTable(this);
    }

    public Booking(int size, LocalTime time, String lastName, int tableID) {
        this.size = size;
        this.time = time;
        this.lastName = lastName;
        this.tableID = tableID;
    }


}
