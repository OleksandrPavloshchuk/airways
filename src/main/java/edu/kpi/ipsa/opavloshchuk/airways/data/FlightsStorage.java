package edu.kpi.ipsa.opavloshchuk.airways.data;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class FlightsStorage {

    private final Map<Integer, Flight> data = new TreeMap<>();

    public void store(Flight flight) {
        if( flight != null) {
            data.put(flight.getNumber(), flight);
        }
    }
    
    public Flight get(int number) {
        return data.get(number);
    }
    
    public void remove(int number) {
        if( data.containsKey(number)) {
            data.remove(number);
        }
    }
    
    public List<Flight> list() {
        return new ArrayList<>(data.values());
    }

}
