package edu.kpi.ipsa.opavloshchuk.airways.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Валідатор рейсів
 */
public class FlightValidator implements Function<Flight, Map<String, String>> {

    /**
     * Провалідувати рейс flight. Повернути асоціативний масив помилок по атрибутах рейса.
     * Коли масив помилок порожній, то рейс валідний.
     * 
     * @param flight
     * @return 
     */
    @Override
    public Map<String, String> apply(Flight flight) {
        final Map<String, String> result = new HashMap<>();
        // Місце відправлення та місце призначення мають бути різними
        if (flight.getFrom() == flight.getTo()) {
            result.put("to", "Start and end points of flight are the same");
        }
        // Час відправлення має бути меншим, ніж час прибуття
        if (flight.getDepartureTime() > flight.getArrivalTime()) {
            result.put("arrivalTime", "Arrival time is less than departure time");
        }
        return result;
    }

}
