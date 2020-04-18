package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightsStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Calculator implements Supplier<List<List<Flight>>> {

    private final List<Flight> source;
    private final List<List<Flight>> routes = new ArrayList<>();
    private final List<Flight> mandatoryFlights = new ArrayList<>();

    public Calculator(FlightsStorage storage) {
        this.source = storage.list();
        // Робиться копія списку для можливості видалення елементів в процесі роботи алгоритму
        mandatoryFlights.addAll(this.source.stream()
                .filter(Flight::isMandatory)
                .collect(Collectors.toList()));
    }

    @Override
    public List<List<Flight>> get() {
        while (!mandatoryFlights.isEmpty()) {
            final List<Flight> route = searchRoute();
            routes.add(route);
            mandatoryFlights.removeAll(route);
        }
        return routes;
    }

    private List<Flight> searchRoute() {
        final Flight originFlight = getMostExpenciveFlight();
        final int home = originFlight.getFrom();

        final List<Flight> base = new ArrayList<>();
        base.add(originFlight);
        final List<List<Flight>> cycles = buildCycles(base, home);
        if (cycles.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Flight> maxCycle = cycles.stream().max( (c1, c2) -> getCost(c1) - getCost(c2)).orElse(null);
        return maxCycle;
    }

    private int getCost(List<Flight> route) {
        return route.stream().collect(Collectors.summingInt(Flight::getCost));
    }

    private Flight getMostExpenciveFlight() {
        return mandatoryFlights.stream().max((f1, f2) -> f1.getCost() - f2.getCost()).orElse(null);
    }

    private List<List<Flight>> buildCycles(List<Flight> base, int home) {
        final List<List<Flight>> result = new ArrayList<>();
        // Останній рейс в маршруті:
        final Flight last = base.get(base.size() - 1);
        if (last.getTo() == home) {
            // Останній рейс повертається додому - цикл знайдено:
            result.add(base);
            return result;
        }
        // Коли сусідніх рейсів нема, то нема циклів:
        if (getNeighboursCount(last) == 0) {
            return Collections.emptyList();
        }

        // Для всіх сусідніх рейсів:
        source.stream()
                .filter(next -> canContinue(last, next))
                .forEach(next -> {
                    // Новий маршрут до сусіда:
                    final List<Flight> route = new ArrayList<>(base);
                    route.add(next);
                    final List<List<Flight>> nextCycles = buildCycles(route, home);
                    result.addAll(nextCycles);
                });

        return result;
    }

    /**
     *
     * @param flight
     * @return число сусідів flight
     */
    private long getNeighboursCount(Flight flight) {
        return source.stream()
                .filter(next -> canContinue(flight, next))
                .count();
    }

    /**
     *
     * @param thisFlight
     * @param nextFlight
     * @return чи може thisFlight продовжити nextFlight
     */
    private static boolean canContinue(Flight thisFlight, Flight nextFlight) {
        return thisFlight.getTo() == nextFlight.getFrom() && thisFlight.getArrivalTime() < nextFlight.getDepartureTime();
    }

}
