package edu.kpi.ipsa.opavloshchuk.airways.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Cycle {

    private final List<Flight> flights = new ArrayList<>();
    private final String tag;

    public Cycle(Cycle src, Flight flight) {
        flights.addAll(src.getFlights());
        flights.add(flight);
        tag = src.getTag();
    }

    public Cycle(Flight flight) {
        flights.add(flight);
        this.tag = String.format("from: %d, departure time: %d", flight.getFrom(), flight.getDepartureTime());
    }

    public void addFlight(Flight flight) {
        flights.add(flight);
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public String getTag() {
        return tag;
    }

    public boolean contains(Flight flight) {
        return flights.contains(flight);
    }

    public boolean containsMandatory() {
        return flights.stream().anyMatch(Flight::isMandatory);
    }

    public Stream<Flight> getMandatory() {
        return flights.stream().filter(Flight::isMandatory);
    }

    public Flight getLast() {
        return flights.get(flights.size() - 1);
    }

    public boolean containsBeforeLast() {
        return flights.size() >= 2;
    }

    public Flight getBeforeLast() {
        return flights.get(flights.size() - 2);
    }

    public int getReturnPoint() {
        return flights.get(0).getFrom();
    }

    public int getMaxMandatoryFlightValue() {
        return getMandatory().mapToInt(Flight::getCost).max().orElseThrow();
    }

    public int getValue(Function<Integer, Integer> waitTimeValueCalculator) {
        int result = 0;
        for (int i = 0; i < flights.size(); i++) {
            result += flights.get(i).getCost() - waitTimeValueCalculator.apply(getWaitTime(i));
        }
        return result;
    }

    private int getWaitTime(int index) {
        return index == 0
                ? 0
                : flights.get(index).getDepartureTime() - flights.get(index - 1).getArrivalTime();
    }

}
