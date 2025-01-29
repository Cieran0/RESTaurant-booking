import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String response = "";

        switch (method) {
            case "GET":
                response = getAvailableTimes(exchange);
                break;
            case "POST":
                response = addBooking(exchange);
                break;
            case "PUT":
                response = updateBooking(exchange);
                break;
            case "DELETE":
                response = deleteBooking(exchange);
                break;
            default:
                response = "Unsupported Method!";
                exchange.sendResponseHeaders(405, response.length());
                return;
        }

        sendResponse(exchange, response);
    }

    private String getAvailableTimes(HttpExchange exchange) {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        if (!params.containsKey("size")) { 
            return "{ \"success\": false, \"error\": \"Missing size?\"}";
        }

        List<LocalTime> availableTimes = RESTaurantBooking.availableTimes(4);
        List<String> avaliable = new ArrayList<>();

        for (LocalTime time : availableTimes) {
            String minute = time.getMinute() == 0 ? "00" : ""+time.getMinute();
            avaliable.add(time.getHour() + ":" + minute);
        }

        return "{ \"success\": true, \"times\": " + avaliable.toString() + " }";
    }

    private String addBooking(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        if (params.containsKey("time") && params.containsKey("size") && params.containsKey("lastName")) {
            int size = Integer.parseInt(params.get("size"));
            LocalTime time = LocalTime.parse( params.get("time") );
            String lastName = params.get("lastName");

            if(!RESTaurantBooking.addBooking(size, time, lastName)) {
                return "{ \"success\": false, \"error\": \"No tables available\"}";
            }

            return "{ \"success\": true}";

        } else {
            return "{ \"success\": false, \"error\": \"Invalid parameters! Use ?size=int&time=time&lastName=str\"}";
        }
    }

    private String updateBooking(HttpExchange exchange) throws IOException {
        return  "{ \"success\": false, \"error\": \"Not implemented\"}";
    }

    private String deleteBooking(HttpExchange exchange) {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        if (params.containsKey("lastName")) {
            String lastName = params.get("lastName");
            boolean found = false;
            for(int i = 0; i < RESTaurantBooking.bookings.size() && !found; i++) {
                if(RESTaurantBooking.bookings.get(i).lastName.equals(lastName)) {
                    RESTaurantBooking.bookings.remove(i);
                    found = true;
                }
            }

            if (found) {
                return "{ \"success\": true }";
            }
            else {
                return "{ \"success\": false, \"error\": \"No booking found\"}";
            }
        } else {
            return "{ \"success\": false, \"error\": \"Invalid parameters! Use ?lastName=str\"}";
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
