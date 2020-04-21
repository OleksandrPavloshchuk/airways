package edu.kpi.ipsa.opavloshchuk.airways.calculation;

import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Calculator {

    private final List<Flight> allFlights = new ArrayList<>();
    private final List<List<Flight>> cycles = new ArrayList<>();
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
            final Stream<List<Flight>> detectedCycles = getCyclesWithMandatoryFlights(origin);
            // Цей рейс оброблений, і він вже нам не потрібний
            allFlights.remove(origin);
            detectedCycles.forEach(cycle -> acquire(cycle));
        }
        // Сортувати цикли: за найважливішими обов'язковими польотами і за найдешевшими циклами:
        cycles.sort(Calculator::compareCycles);
    }

    public List<List<Flight>> getCycles() {
        return cycles;
    }

    public List<Flight> getMandatoryFlightsWithoutCycles() {
        return mandatoryFlightsWithoutCycles;
    }

    /**
     * Порівняння циклів
     * 
     * @param cycle1
     * @param cycle2
     * @return 
     */
    private static int compareCycles(List<Flight> cycle1, List<Flight> cycle2) {
        final Flight mostVal1 = getMostValuableMandatoryFlight(cycle1);
        final Flight mostVal2 = getMostValuableMandatoryFlight(cycle1);
        final int diff = mostVal1.getCost() - mostVal2.getCost();
        if (diff != 0) {
            return diff;
        }
        return getCost(cycle2) - getCost(cycle1);
    }

    /**
     * Додати цикл до обраних
     *
     * @param cycle
     */
    private void acquire(List<Flight> cycle) {
        cycles.add(cycle);
        mandatoryFlightsWithoutCycles.removeAll(cycle);
        allFlights.removeAll(cycle);
    }

    /**
     * Знайти цикл із обов'язковим рейсом origin, що має найменшу вартість
     *
     * @param origin
     * @return
     */
    private Stream<List<Flight>> getCyclesWithMandatoryFlights(Flight origin) {
        return detectCycles(merge(new ArrayList<>(), origin), origin.getFrom())
                .stream()
                .filter(cycle -> cycle.stream().anyMatch(Flight::isMandatory));
    }

    /**
     * Порахувати вартість циклу
     *
     * @param cycle
     * @return
     */
    private static int getCost(List<Flight> cycle) {
        int result = 0;
        for (int i = 0; i < cycle.size(); i++) {
            result += getCost(cycle, i);
        }
        return result;
    }

    /**
     * Знайти рейс, що вилітає найраніше
     *
     * @return
     */
    private Flight getEarliestFlight() {
        return allFlights.stream()
                .min((f1, f2) -> f1.getDepartureTime() - f2.getDepartureTime())
                .orElseThrow(() -> new IllegalArgumentException("no flights"));
    }

    /**
     * Знайти найдорожчий обов'язковий рейс (TODO ще буде треба)
     *
     * @return
     */
    private static Flight getMostValuableMandatoryFlight(List<Flight> cycle) {
        return cycle.stream()
                .filter(Flight::isMandatory)
                .max((f1, f2) -> f1.getCost() - f2.getCost())
                .orElseThrow(() -> new IllegalArgumentException("cycle is empty"));
    }

    /**
     * Рекурсивний пошук циклів
     *
     * @param base маршрут для перевірки
     * @param home точка, куди треба вернутися
     * @return
     */
    private List<List<Flight>> detectCycles(List<Flight> base, int home) {
        final List<List<Flight>> result = new ArrayList<>();
        // Останній рейс в маршруті:
        final Flight last = base.get(base.size() - 1);
        if (base.size() > 1) {
            // Час відправлення останнього рейсу має бути після часу прибуття передостаннього:
            final Flight beforeLast = base.get(base.size() - 2);
            if (last.getDepartureTime() < beforeLast.getArrivalTime()) {
                return result;
            }
            if (last.getTo() == home) {
                // Останній рейс у ланцюжку повертається додому - цикл знайдено:
                result.add(base);
                return result;
            }
        }
        // Продовжити рекурсивно для всіх сусідніх рейсів:
        getNeighbours(last)
                .map(next -> merge(base, next))
                .map(cycle -> detectCycles(cycle, home))
                .forEach(newCycles -> result.addAll(newCycles));
        return result;
    }

    /**
     * Добавити елемент у хвіст масиву
     *
     * @param base
     * @param next
     * @return
     */
    private static List<Flight> merge(List<Flight> base, Flight next) {
        final List<Flight> route = new ArrayList<>(base);
        route.add(next);
        return route;
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
     * Порахувати вартість рейсу з урахуванням часу очікування між рейсами
     *
     * @param cycle цикл
     * @param index позиція рейсу у циклі
     * @return
     */
    private static int getCost(List<Flight> cycle, int index) {
        final Flight thisFlight = cycle.get(index);
        int result = thisFlight.getCost();
        if (index == 0) {
            return result;
        }
        // У циклі є як мінімум два рейси - знайти проміжок часу між ними:
        final int waitTime = thisFlight.getDepartureTime() - cycle.get(index - 1).getArrivalTime();
        return result - getWaitCost(waitTime);
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
