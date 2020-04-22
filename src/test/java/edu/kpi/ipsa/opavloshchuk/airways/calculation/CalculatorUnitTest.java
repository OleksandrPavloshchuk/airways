package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Cycle;
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
        final Calculator calculator = new Calculator(new FlightsStorage().list());
        calculator.perform();
        final List<Cycle> actualCycles = calculator.getCycles();
        final List<Flight> actualWithoutCycles = calculator.getMandatoryFlightsWithoutCycles();
        Assertions.assertNotNull(actualCycles);
        Assertions.assertTrue(actualCycles.isEmpty());
        Assertions.assertTrue(actualWithoutCycles.isEmpty());
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
        final Calculator calculator = new Calculator(storage.list());
        calculator.perform();
        final List<Cycle> actualCycles = calculator.getCycles();
        final List<Flight> actualWithoutCycles = calculator.getMandatoryFlightsWithoutCycles();        
        Assertions.assertNotNull(actualCycles);
        Assertions.assertTrue(actualCycles.isEmpty());
        Assertions.assertFalse(actualWithoutCycles.isEmpty());
        assertEquals( Arrays.asList(1), actualWithoutCycles);
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
                .withMandatory(false)
                .build());
        final Calculator calculator = new Calculator(storage.list());
        calculator.perform();
        final List<Cycle> actualCycles = calculator.getCycles();
        final List<Flight> actualWithoutCycles = calculator.getMandatoryFlightsWithoutCycles();     
        Assertions.assertNotNull(actualCycles);
        Assertions.assertTrue(actualCycles.isEmpty());
        Assertions.assertTrue(actualWithoutCycles.isEmpty());
    }

    @Test
    public void singleCycle() {
        final FlightsStorage storage = new FlightsStorage();
        storage.store(new FlightBuilder()
                .withNumber(1)
                .withFrom(1)
                .withTo(2)
                .withCost(4)
                .withDepartureTime(100)
                .withArrivalTime(160)
                .withMandatory(true)
                .build());
        storage.store(new FlightBuilder()
                .withNumber(2)
                .withFrom(2)
                .withTo(3)
                .withCost(1)
                .withDepartureTime(200)
                .withArrivalTime(310)
                .withMandatory(false)
                .build());
        storage.store(new FlightBuilder()
                .withNumber(3)
                .withFrom(3)
                .withTo(1)
                .withCost(5)
                .withDepartureTime(400)
                .withArrivalTime(480)
                .withMandatory(false)
                .build());
        final Calculator calculator = new Calculator(storage.list());
        calculator.perform();        
        final List<Cycle> actualCycles = calculator.getCycles();
        final List<Flight> actualWithoutCycles = calculator.getMandatoryFlightsWithoutCycles();    
        Assertions.assertNotNull(actualCycles);
        Assertions.assertTrue(actualCycles.size() == 1);
        assertEquals(Arrays.asList(1, 2, 3), actualCycles.get(0).getFlights());   
        Assertions.assertTrue(actualWithoutCycles.isEmpty());
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
