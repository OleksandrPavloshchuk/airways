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
        while (!mandatoryFlights.isEmpty()) {
            // Обов'язкові рейси обробляються в порядку спадання їхньої ціни
            final Optional<Flight> originOpt = getMostExpenciveMandatoryFlight();
            if (originOpt.isPresent()) {                
                final Flight origin = originOpt.get();
                final Optional<List<Flight>> cycleOpt = searchCycle(origin);
                // Цей рейс оброблений, і він вже нам не потрібний
                removeFromSource(origin);
                if (cycleOpt.isPresent()) {
                    // Добавити знайдений цикл до списку
                    final List<Flight> cycle = cycleOpt.get();
                    cycles.add(cycle);
                    removeFromSource(cycle);
                } else {
                    // Жодного циклу для рейсу не знайдено
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

    /**
     * Видалити flight зі списку всіх рейсів та обов'язкових рейсів
     * для запобігання повторного обліку
     * 
     * @param flight 
     */
    private void removeFromSource(Flight flight) {
        mandatoryFlights.remove(flight);
        allFlights.remove(flight);
    }

    /**
     * Видалити всі рейси із циклу cycle зі списку всіх рейсів та обов'язкових рейсів
     * для запобігання повторного обліку
     * 
     * @param cycle 
     */
    private void removeFromSource(List<Flight> cycle) {
        mandatoryFlights.removeAll(cycle);
        allFlights.removeAll(cycle);
    }

    /**
     * Знайти цикл із обов'язковим рейсом origin, що має найменшу вартість
     * 
     * @param origin
     * @return 
     */
    private Optional<List<Flight>> searchCycle(Flight origin) {
        // Пошук почати із масиву, що місить лише рейс origin
        return detectCycles(merge(new ArrayList<>(), origin), origin.getFrom())
                .stream().min((c1, c2) -> getCost(c1) - getCost(c2));
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
     * Знайти найдорожчий обов'язковий рейс
     * 
     * @return 
     */
    private Optional<Flight> getMostExpenciveMandatoryFlight() {
        return mandatoryFlights.stream().max((f1, f2) -> f1.getCost() - f2.getCost());
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
        if (last.getTo() == home) {
            // Останній рейс у ланцюжку повертається додому - цикл знайдено:
            result.add(base);
            return result;
        } else if (base.size() > 1) {
            // Час відправлення останнього рейсу має бути після часу прибуття передостаннього:
            final Flight beforeLast = base.get(base.size() - 2);
            if (last.getDepartureTime() < beforeLast.getArrivalTime()) {
                // Останній рейс відлітає раніше, ніж прибуває пере
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
     * Знайти сусідні рейси, тобто ті, які відправляються із пункту призначення рейсу flight
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
