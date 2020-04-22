package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Cycle;
import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Calculator {

    private final List<Flight> allFlights = new ArrayList<>();
    private final List<Cycle> cycles = new ArrayList<>();
    private final List<Flight> mandatoryFlights = new ArrayList<>();
    private final List<Flight> mandatoryFlightsWithoutCycles = new ArrayList<>();

    public Calculator(List<Flight> allFlights) {
        if (allFlights == null) {
            throw new IllegalArgumentException("all flights list is null");
        }
        // Робляться копії списків для можливості видалення елементів в процесі роботи алгоритму
        this.allFlights.addAll(allFlights);
        mandatoryFlights.addAll(this.allFlights.stream()
                .filter(Flight::isMandatory)
                .collect(Collectors.toList()));
    }
    

    public List<Cycle> getCycles() {
        return cycles;
    }

    public List<Flight> getMandatoryFlightsWithoutCycles() {
        return mandatoryFlightsWithoutCycles;
    }    

    /**
     * Визначити цикли рейсів та обов'язкові рейси, що не потрапили до циклів
     */
    public void perform() {
        List<Cycle> allCycles = new ArrayList<>();

        while (!allFlights.isEmpty()) {
            final Flight origin = getEarliestFlight();
            getCyclesWithMandatoryFlights(origin).forEach(allCycles::add);
            allFlights.remove(origin);
        }

        while (!mandatoryFlights.isEmpty()) {
            final Flight mostValuable = getMandatoryFlightWithMaxIncome();
            final Optional<Cycle> cheapestOpt = allCycles.stream()
                    .filter(cycle -> cycle.contains(mostValuable))
                    .min((c1, c2) -> c1.getExpences(Calculator::getWaitCost) - c2.getExpences(Calculator::getWaitCost));
            mandatoryFlights.remove(mostValuable);
            if (cheapestOpt.isPresent()) {
                final Cycle cheapest = cheapestOpt.get();
                cycles.add(cheapest);
                allCycles = removeIntersectedCycles(allCycles, cheapest);
            } else {
                mandatoryFlightsWithoutCycles.add(mostValuable);
            }
        }
    }
    
    /**
     * Видалити всі цикли, що містять рейси, спільні із даним
    */
    private List<Cycle> removeIntersectedCycles(List<Cycle> list, Cycle baseCycle) {
        return list.stream()
                .filter( cycle -> cycle.getFlights().stream().noneMatch(flight -> baseCycle.contains(flight)))                
                .collect(Collectors.toList());
    }    

    /**
     * Знайти цикл, що починається із рейсу origin, що має найменшу вартість
     *
     * @param origin
     * @return
     */
    private Stream<Cycle> getCyclesWithMandatoryFlights(Flight origin) {
        return detectCycles(new Cycle(origin)).stream().filter(Cycle::containsMandatory);
    }

    /**
     * Знайти рейс, що вилітає найраніше
     *
     * @return
     */
    private Flight getEarliestFlight() {
        return allFlights.stream()
                .min((f1, f2) -> f1.getDepartureTime() - f2.getDepartureTime()).orElseThrow();
    }

    /**
     * Знайти найважливіший обов'язковий рейс
     *
     * @return
     */
    private Flight getMandatoryFlightWithMaxIncome() {
        return mandatoryFlights.stream()
                .max((f1, f2) -> f1.getIncome()- f2.getIncome()).orElseThrow();
    }

    /**
     * Рекурсивний пошук циклів
     *
     * @param base маршрут для перевірки
     * @param home точка, куди треба вернутися
     * @return
     */
    private List<Cycle> detectCycles(Cycle base) {
        final Flight last = base.getLast();
        if (base.containsBeforeLast()) {
            // Час відправлення останнього рейсу має бути після часу прибуття передостаннього:
            final Flight beforeLast = base.getBeforeLast();
            if (last.getDepartureTime() < beforeLast.getArrivalTime()) {
                return Collections.emptyList();
            }
            if (last.getTo() == base.getReturnPoint()) {
                // Останній рейс у ланцюжку повертається додому - цикл знайдено:
                return Arrays.asList(base);
            }
        }
        // Продовжити рекурсивно для всіх сусідніх рейсів:
        return getNeighbours(last)
                .map(flight -> detectCycles(new Cycle(base, flight)))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Знайти сусідні рейси, тобто ті, які відправляються із пункту призначення
     * рейсу flight
     *
     * @param flight
     * @return
     */
    private Stream<Flight> getNeighbours(Flight flight) {
        return allFlights.stream()
                .filter(next -> next.getFrom() == flight.getTo());
    }

    /**
     * Порахувати час очікування
     *
     * @param time
     * @return
     */
    private static int getWaitCost(int time) {
        final double waitPrice = 0.0; // TODO for a while
        return (int) Math.round(time * waitPrice);
    }

}
