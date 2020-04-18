package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightsStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Calculator implements Supplier<List<List<Flight>>> {

    private final List<Flight> allFlights;
    private final List<List<Flight>> routes = new ArrayList<>();
    private final List<Flight> mandatoryFlights = new ArrayList<>();

    public Calculator(FlightsStorage storage) {
        // Робляться копії списків для можливості видалення елементів в процесі роботи алгоритму
        this.allFlights = new ArrayList<>(storage.list());
        mandatoryFlights.addAll(this.allFlights.stream()
                .filter(Flight::isMandatory)
                .collect(Collectors.toList()));
    }

    @Override
    public List<List<Flight>> get() {
        while (!mandatoryFlights.isEmpty()) {
            final Flight origin = getMostExpenciveMandatoryFlight();
            final List<Flight> route = searchRoute(origin);
            mandatoryFlights.remove(origin);
            allFlights.remove(origin);
            if (route != null) {
                routes.add(route);
                mandatoryFlights.removeAll(route);
                allFlights.removeAll(route);
            }
        }
        return routes;
    }

    private List<Flight> searchRoute(Flight origin) {
        final int home = origin.getFrom();

        final List<Flight> base = new ArrayList<>();
        base.add(origin);
        final List<List<Flight>> cycles = buildCycles(base, home);
        if (cycles.isEmpty()) {
            return null;
        }

        final List<Flight> minCycle = cycles.stream().min((c1, c2) -> getCost(c1) - getCost(c2)).orElse(null);
        return minCycle;
    }

    private int getCost(List<Flight> route) {
        return route.stream().collect(Collectors.summingInt(Flight::getCost));
    }

    private Flight getMostExpenciveMandatoryFlight() {
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

        // Для всіх сусідніх рейсів:
        getNeighbours(last).stream()
                .map(next -> merge(base, next))
                .map(route -> buildCycles(route, home))
                .forEach(newRoutes -> result.addAll(newRoutes));
        return result;
    }

    private List<Flight> merge(List<Flight> base, Flight next) {
        final List<Flight> route = new ArrayList<>(base);
        route.add(next);
        return route;
    }

    private List<Flight> getNeighbours(Flight flight) {
        return allFlights.stream()
                .filter(next -> next.getFrom() == flight.getTo())
                .collect(Collectors.toList());
    }

}
