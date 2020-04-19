package edu.kpi.ipsa.opavloshchuk.airways.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FlightValidator implements Function<Flight, Map<String, String>> {

    @Override
    public Map<String, String> apply(Flight flight) {
        final Map<String, String> result = new HashMap<>();
        if (flight.getFrom() == flight.getTo()) {
            result.put("to", "Start and end points of flight are the same");
        }
        if (flight.getDepartureTime() > flight.getArrivalTime()) {
            result.put("arrivalTime", "arrival time is less than departure time");
        }
        return result;
    }

}
