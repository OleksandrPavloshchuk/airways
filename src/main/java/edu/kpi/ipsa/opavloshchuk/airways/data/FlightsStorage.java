package edu.kpi.ipsa.opavloshchuk.airways.data;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FlightsStorage {

    private final Map<Integer, Flight> data = new HashMap<>();

    public void store(Flight flight) {
        if (flight != null) {
            data.put(flight.getNumber(), flight);
        }
    }

    public Flight get(int number) {
        return data.get(number);
    }

    public void remove(int number) {
        if (data.containsKey(number)) {
            data.remove(number);
        }
    }

    public List<Flight> list() {
        final List<Flight> result = new ArrayList<>(data.values());
        result.sort((f1, f2) -> f1.getDepartureTime() - f2.getDepartureTime());
        return result;
    }

}
