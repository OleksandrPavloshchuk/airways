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

/**
 * Обчислювач потрібних циклів та обов'язкових рейсів поза циклами
 * 
 */
public class Calculator {

    private final List<Flight> allFlights = new ArrayList<>(); // всі рейси
    private final List<Cycle> cycles = new ArrayList<>(); // обчислені цикли
    private final List<Flight> mandatoryFlights = new ArrayList<>(); // обов'язкові рейси
    private final List<Flight> mandatoryFlightsWithoutCycles = new ArrayList<>(); // обчислені обов'язкові рейси поза циклами

    public Calculator(List<Flight> allFlights) {
        if (allFlights == null) {
            throw new IllegalArgumentException("all flights list is null");
        }
        // Робляться копії списків для можливості видалення елементів в процесі роботи алгоритму
        this.allFlights.addAll(allFlights);
        this.allFlights.stream().filter(Flight::isMandatory).forEach(mandatoryFlights::add);
    }

    public List<Cycle> getCycles() {
        return cycles;
    }

    public List<Flight> getMandatoryFlightsWithoutCycles() {
        return mandatoryFlightsWithoutCycles;
    }

    /**
     * Визначити цикли рейсів та обов'язкові рейси поза циклами
     */
    public void perform() {
        List<Cycle> allCycles = new ArrayList<>();

        // Для всіх рейсів
        while (!allFlights.isEmpty()) {
            // Порядок перебору - від рейсів, що починають найраніше
            final Flight origin = getEarliestFlight();
            // Знайти цикли, що містять обов'язкові рейси
            getCyclesWithMandatoryFlights(origin).forEach(allCycles::add);
            // Видалити рейс, для якого ми шукали цикли - він вже не потрібний
            allFlights.remove(origin);
        }

        // Для всіх обов'язкових рейсів
        while (!mandatoryFlights.isEmpty()) {
            // Порядок перебору - від рейсів із найбільшим прибутком
            final Flight mostValuable = getMandatoryFlightWithMaxIncome();
            // Знайти для такого рейсу найдешевший за витратами цикл
            final Optional<Cycle> cheapestOpt = getCheepestCycle(allCycles, mostValuable);
            // Видалити рейс, для якого ми шукали цикли - він вже не потрібний
            mandatoryFlights.remove(mostValuable);
            if (cheapestOpt.isPresent()) {
                final Cycle cheapest = cheapestOpt.get();
                // Добавити знайдений найдешевший цикл до результату
                cycles.add(cheapest);
                // Видалити всі цикли, які мають рейси, спільні зі знайденим - рейс відбувається лише раз
                allCycles = removeIntersectedCycles(allCycles, cheapest);
            } else {
                // Жодного циклу із таким рейсом не знайдено
                mandatoryFlightsWithoutCycles.add(mostValuable);
            }
        }
    }

    /**
     * Визначити мінімальний за витратами цикл із allCycles, що містить політ flight
     * 
     * @param allCycles
     * @param flight
     * @return 
     */
    private Optional<Cycle> getCheepestCycle(List<Cycle> allCycles, Flight flight) {
        return allCycles.stream()
                .filter(cycle -> cycle.contains(flight))
                .min((c1, c2) -> c1.getExpenses(Calculator::getWaitExpenses) - c2.getExpenses(Calculator::getWaitExpenses));
    }

    /**
     * Видалити всі цикли, що містять рейси, спільні із даним
     */
    private List<Cycle> removeIntersectedCycles(List<Cycle> list, Cycle baseCycle) {
        return list.stream()
                .filter(cycle -> cycle.getFlights().stream().noneMatch(flight -> baseCycle.contains(flight)))
                .collect(Collectors.toList());
    }

    /**
     * Цикли із обов'язковими рейсами, що починаються із рейсу origin
     *
     * @param origin
     * @return
     */
    private Stream<Cycle> getCyclesWithMandatoryFlights(Flight origin) {
        return detectCycles(new Cycle(origin)).stream().filter(Cycle::containsMandatory);
    }

    /**
     * Рейс, що вилітає найраніше
     *
     * @return
     */
    private Flight getEarliestFlight() {
        return allFlights.stream().min((f1, f2) -> f1.getDepartureTime() - f2.getDepartureTime()).orElseThrow();
    }

    /**
     * Обов'язковий рейс із найбільшим прибутком
     *
     * @return
     */
    private Flight getMandatoryFlightWithMaxIncome() {
        return mandatoryFlights.stream().max((f1, f2) -> f1.getIncome() - f2.getIncome()).orElseThrow();
    }

    /**
     * Рекурсивний пошук циклів
     *
     * @param base ланцюжок для перевірки
     * @return
     */
    private List<Cycle> detectCycles(Cycle base) {
        final Flight last = base.getLast();
        // Перевірити лише цикли, що мають хоча два рейси, отже, можуть бути замкненими
        if (base.containsBeforeLast()) {
            // Час відправлення останнього рейсу має бути після часу прибуття передостаннього:
            final Flight beforeLast = base.getBeforeLast();
            if (last.getDepartureTime() < beforeLast.getArrivalTime()) {
                // Далі ми рухатися по цьому ланцюжку не можемо - відсікаємо гілку
                return Collections.emptyList();
            }
            if (last.getTo() == base.getReturnPoint()) {
                // Останній рейс у ланцюжку повертається у початкову точку - цикл знайдено:
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
     * Рейси, які відправляються із пункту призначення рейсу flight
     *
     * @param flight
     * @return
     */
    private Stream<Flight> getNeighbours(Flight flight) {
        return allFlights.stream().filter(next -> next.getFrom() == flight.getTo());
    }

    /**
     * Втрати від очікування протягом часу time
     *
     * @param time
     * @return
     */
    private static int getWaitExpenses(int time) {
        final double waitPrice = 0.3; // TODO треба поекспериментувати із різними коефіцієнтами
        return (int) Math.round(time * waitPrice);
    }

}
