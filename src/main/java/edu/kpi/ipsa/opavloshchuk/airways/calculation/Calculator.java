package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Cycle;
import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Calculator {

    private final List<Flight> allFlights = new ArrayList<>();
    private final List<Cycle> cycles = new ArrayList<>();
    private final List<Flight> mandatoryFlightsWithoutCycles = new ArrayList<>();

    public Calculator(List<Flight> allFlights) {
        if (allFlights == null) {
            throw new IllegalArgumentException("all flights list is null");
        }
        // Робляться копії списків для можливості видалення елементів в процесі роботи алгоритму
        this.allFlights.addAll(allFlights);
        mandatoryFlightsWithoutCycles.addAll(this.allFlights.stream()
                .filter(Flight::isMandatory)
                .collect(Collectors.toList()));
    }

    /**
     * Визначити цикли рейсів та обов'язкові рейси, що не потрапили до циклів
     */
    public void perform() {
        while (!allFlights.isEmpty()) {
            final Flight origin = getEarliestFlight();
            final Stream<Cycle> detectedCycles = getCyclesWithMandatoryFlights(origin);
            // Цей рейс оброблений, і він вже нам не потрібний
            allFlights.remove(origin);
            detectedCycles.forEach(this::acquire);
        }
        // Сортувати цикли: за найважливішими обов'язковими польотами і за найдешевшими циклами:
        distributeAndSortCycles();
        detectMandatoryFlightsWithoutCycles();
    }

    public List<Cycle> getCycles() {
        return cycles;
    }

    public List<Flight> getMandatoryFlightsWithoutCycles() {
        return mandatoryFlightsWithoutCycles;
    }

    /**
     * Розподілити список циклів на підсписки по максимальних значеннях обов'язкових польотів
     * а потім взяти із кожного списку мінімум по вартості
     */
    private void distributeAndSortCycles() {
        final Map<Integer, List<Cycle>> byMaxValue = new HashMap<>();
        cycles.forEach(cycle -> add(byMaxValue, cycle));
        final List<Cycle> temp = byMaxValue.values()
                .stream()
                .map(Calculator::getByMinValue)
                .collect(Collectors.toList());
        cycles.clear();
        cycles.addAll(temp);
    }

    /**
     * Добавити цикл до списку за максимальною ціною обов'язкового рейсу
     *
     * @param map
     * @param value
     */
    private void add(Map<Integer, List<Cycle>> map, Cycle value) {
        final int key = value.getMaxMandatoryFlightValue();
        List<Cycle> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        list.add(value);
    }

    /**
     * Прибрати зі списку обов'язкових ті, які потрапили до циклів
     */
    private void detectMandatoryFlightsWithoutCycles() {
        cycles.forEach(cycle -> cycle.getMandatory().forEach(mandatoryFlightsWithoutCycles::remove));
    }

    /**
     * Цикл із найменшим значенням
     *
     * @param cycles
     * @return
     */
    private static Cycle getByMinValue(List<Cycle> cycles) {
        final Function<Cycle, Integer> valueCalculator = cycle -> cycle.getValue(Calculator::getWaitCost);
        return cycles.stream()
                .min((c1, c2) -> valueCalculator.apply(c1) - valueCalculator.apply(c2))
                .orElseThrow();
    }

    /**
     * Додати цикл до обраних
     *
     * @param cycle
     */
    private void acquire(Cycle cycle) {
        cycles.add(cycle);
        allFlights.removeAll(cycle.getFlights());
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
