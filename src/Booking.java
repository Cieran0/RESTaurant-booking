import java.time.LocalTime;

public class Booking {
    
    int tableID;
    int size;
    LocalTime time;

    public Booking(int size, LocalTime time) {
        this.size = size;
        this.time = time;

        this.tableID = RESTaurantBooking.getBestTable(this);
    }


}
