package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Cycle;
import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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

    /**
     * Визначити цикли рейсів та обов'язкові рейси, що не потрапили до циклів
     */
    public void perform() {
        final List<Cycle> allCycles = new ArrayList<>();

        while (!allFlights.isEmpty()) {
            final Flight origin = getEarliestFlight();
            getCyclesWithMandatoryFlights(origin).forEach(allCycles::add);
            // Цей рейс оброблений, і він вже нам не потрібний
            allFlights.remove(origin);
        }

        final Function<Cycle, Integer> valueCalculator = cycle -> cycle.getValue(Calculator::getWaitCost);

        while (!mandatoryFlights.isEmpty()) {
            final Flight mostValuable = getMostValuableMandatoryFlight();
            final Optional<Cycle> cheapestCycleWithMostValuableMandatoryFlight = allCycles.stream()
                    .filter(cycle -> cycle.contains(mostValuable))
                    .min((c1, c2) -> valueCalculator.apply(c1) - valueCalculator.apply(c2));
            mandatoryFlights.remove(mostValuable);
            if (cheapestCycleWithMostValuableMandatoryFlight.isPresent()) {
                cycles.add(cheapestCycleWithMostValuableMandatoryFlight.get());
            } else {
                mandatoryFlightsWithoutCycles.add(mostValuable);
            }
        }
    }

    public List<Cycle> getCycles() {
        return cycles;
    }

    public List<Flight> getMandatoryFlightsWithoutCycles() {
        return mandatoryFlightsWithoutCycles;
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
                .min((f1, f2) -> f1.getDepartureTime() - f2.getDepartureTime())
                .orElseThrow();
    }

    /**
     * Знайти найважливіший обов'язковий рейс
     *
     * @return
     */
    private Flight getMostValuableMandatoryFlight() {
        return mandatoryFlights.stream()
                .max((f1, f2) -> f1.getCost() - f2.getCost())
                .orElseThrow();
    }

    /**
     * Рекурсивний пошук циклів
     *
     * @param base маршрут для перевірки
     * @param home точка, куди треба вернутися
     * @return
     */
    private List<Cycle> detectCycles(Cycle base) {
        final List<Cycle> result = new ArrayList<>();
        // Останній рейс в маршруті:
        final Flight last = base.getLast();
        if (base.containsBeforeLast()) {
            // Час відправлення останнього рейсу має бути після часу прибуття передостаннього:
            final Flight beforeLast = base.getBeforeLast();
            if (last.getDepartureTime() < beforeLast.getArrivalTime()) {
                return result;
            }
            if (last.getTo() == base.getReturnPoint()) {
                // Останній рейс у ланцюжку повертається додому - цикл знайдено:
                result.add(base);
                return result;
            }
        }
        // Продовжити рекурсивно для всіх сусідніх рейсів:
        getNeighbours(last)
                .map(flight -> detectCycles(new Cycle(base, flight)))
                .forEach(newCycles -> result.addAll(newCycles));
        return result;
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
