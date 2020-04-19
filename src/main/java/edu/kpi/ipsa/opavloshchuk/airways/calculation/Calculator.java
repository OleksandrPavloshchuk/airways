package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Calculator {

    private final List<Flight> allFlights = new ArrayList<>();
    private final List<List<Flight>> cycles = new ArrayList<>();
    private final List<Flight> mandatoryFlights = new ArrayList<>();
    private final List<Flight> mandatoryFlightsWithoutCycles = new ArrayList<>();

    public Calculator(List<Flight> allFlights) {
        if( allFlights==null ) {
            throw new IllegalArgumentException( "all flights list is null");
        }
        // Робляться копії списків для можливості видалення елементів в процесі роботи алгоритму
        this.allFlights.addAll(allFlights);
        mandatoryFlights.addAll(this.allFlights.stream()
                .filter(Flight::isMandatory)
                .collect(Collectors.toList()));
    }
    
    public void perform() {
        while (!mandatoryFlights.isEmpty()) {
            final Optional<Flight> originOpt = getMostExpenciveMandatoryFlight();
            if (originOpt.isPresent()) {
                final Flight origin = originOpt.get();
                final Optional<List<Flight>> cycleOpt = searchCycle(origin);
                removeFromSource(origin);
                if (cycleOpt.isPresent()) {
                    final List<Flight> cycle = cycleOpt.get();
                    cycles.add(cycle);
                    removeFromSource(cycle);
                } else {
                    mandatoryFlightsWithoutCycles.add(origin);
                }
            }
        }
    }

    public List<List<Flight>> getCycles() {
        return cycles;
    }
    
    public List<Flight> getMandatoryFlightsWithoutCycles() {
        return mandatoryFlightsWithoutCycles;
    }

    private void removeFromSource(Flight flight) {
        mandatoryFlights.remove(flight);
        allFlights.remove(flight);
    }

    private void removeFromSource(List<Flight> route) {
        mandatoryFlights.removeAll(route);
        allFlights.removeAll(route);
    }

    private Optional<List<Flight>> searchCycle(Flight origin) {
        return detectCycles(merge(new ArrayList<>(), origin), origin.getFrom())
                .stream().min((c1, c2) -> getCost(c1) - getCost(c2));
    }

    private static int getCost(List<Flight> route) {
        int result = 0;
        for (int i = 0; i < route.size(); i++) {
            result += getCost(route, i);
        }
        return result;
    }

    private Optional<Flight> getMostExpenciveMandatoryFlight() {
        return mandatoryFlights.stream().max((f1, f2) -> f1.getCost() - f2.getCost());
    }

    private List<List<Flight>> detectCycles(List<Flight> base, int home) {
        final List<List<Flight>> result = new ArrayList<>();
        // Останній рейс в маршруті:
        final Flight last = base.get(base.size() - 1);
        if (last.getTo() == home) {
            // Останній рейс у ланцюжку повертається додому - цикл знайдено:
            result.add(base);
            return result;
        } else if (base.size() > 1) {
            // Час відправлення наступного рейсу має бути після часу попереднього:
            final Flight beforeLast = base.get(base.size() - 2);
            if (last.getDepartureTime() < beforeLast.getArrivalTime()) {
                return result;
            }
        }
        // Для всіх сусідніх рейсів:
        getNeighbours(last)
                .map(next -> merge(base, next))
                .map(cycle -> detectCycles(cycle, home))
                .forEach(newCycles -> result.addAll(newCycles));
        return result;
    }

    private static List<Flight> merge(List<Flight> base, Flight next) {
        final List<Flight> route = new ArrayList<>(base);
        route.add(next);
        return route;
    }

    private Stream<Flight> getNeighbours(Flight flight) {
        return allFlights.stream()
                .filter(next -> next.getFrom() == flight.getTo());
    }

    private static int getCost(List<Flight> route, int index) {
        final Flight thisFlight = route.get(index);
        int result = route.get(index).getCost();
        if (index == 0) {
            return result;
        }
        // В маршруті є як мінімум два рейси - знайти проміжок часу між ними:
        final int waitTime = thisFlight.getDepartureTime() - route.get(index - 1).getArrivalTime();
        return result - getWaitCost(waitTime);
    }

    private static int getWaitCost(int time) {
        final double waitPrice = 0.0; // TODO for a while
        return (int) Math.round(time * waitPrice);
    }

}
