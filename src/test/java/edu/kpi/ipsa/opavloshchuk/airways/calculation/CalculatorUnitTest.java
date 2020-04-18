package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightsStorage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class CalculatorUnitTest {

    @Test
    public void empty() {
        final List<List<Flight>> actual = new Calculator(new FlightsStorage()).get();
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    public void singleMandatory() {
        final FlightsStorage storage = new FlightsStorage();
        storage.store(new FlightBuilder()
                .withNumber(1)
                .withFrom(1)
                .withTo(2)
                .withCost(100)
                .withDepartureTime(0)
                .withArrivalTime(100)
                .withMandatory(true)
                .build());
        final List<List<Flight>> actual = new Calculator(storage).get();
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    public void singleNonMandatory() {
        final FlightsStorage storage = new FlightsStorage();
        storage.store(new FlightBuilder()
                .withNumber(1)
                .withFrom(1)
                .withTo(2)
                .withCost(100)
                .withDepartureTime(0)
                .withArrivalTime(100)
                .withMandatory(true)
                .build());
        final List<List<Flight>> actual = new Calculator(storage).get();
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    public void singleCycle() {
        final FlightsStorage storage = new FlightsStorage();
        storage.store(new FlightBuilder()
                .withNumber(1)
                .withFrom(1)
                .withTo(2)
                .withCost(4)
                .withDepartureTime(0)
                .withArrivalTime(100)
                .withMandatory(false)
                .build());
        storage.store(new FlightBuilder()
                .withNumber(2)
                .withFrom(3)
                .withTo(1)
                .withCost(1)
                .withDepartureTime(200)
                .withArrivalTime(310)
                .withMandatory(true)
                .build());
        storage.store(new FlightBuilder()
                .withNumber(3)
                .withFrom(2)
                .withTo(3)
                .withCost(5)
                .withDepartureTime(100)
                .withArrivalTime(180)
                .withMandatory(true)
                .build());
        final List<List<Flight>> actual = new Calculator(storage).get();
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.size() == 1);
        assertEquals(Arrays.asList(3, 2, 1), actual.get(0));        
    }

    private void assertEquals(List<Integer> expected, List<Flight> actual) {
        final List<Integer> numbers = actual.stream().map(Flight::getNumber).collect(Collectors.toList());
        Assertions.assertEquals(expected, numbers);
    }

    private static class FlightBuilder {

        private final Flight flight = new Flight();

        private FlightBuilder withNumber(int number) {
            flight.setNumber(number);
            return this;
        }

        private FlightBuilder withFrom(int from) {
            flight.setFrom(from);
            return this;
        }

        private FlightBuilder withTo(int to) {
            flight.setTo(to);
            return this;
        }

        private FlightBuilder withCost(int cost) {
            flight.setCost(cost);
            return this;
        }

        private FlightBuilder withDepartureTime(int departureTime) {
            flight.setDepartureTime(departureTime);
            return this;
        }

        private FlightBuilder withArrivalTime(int arrivalTime) {
            flight.setArrivalTime(arrivalTime);
            return this;
        }

        private FlightBuilder withMandatory(boolean mandatory) {
            flight.setMandatory(mandatory);
            return this;
        }

        private Flight build() {
            return flight;
        }
    }

}
