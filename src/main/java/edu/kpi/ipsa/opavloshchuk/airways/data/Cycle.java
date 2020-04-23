package edu.kpi.ipsa.opavloshchuk.airways.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Цикл рейсів
 *
 * Цикл є закінченим, коли точка призначення отсаннього рейсу збігається з точкою відправлення
 * нульового.Цикл будується таким чином, щоб час прибуття кожного попереднього рейсу був меншим, ніж
 * час відправлення кожного наступного.
 */
public class Cycle {

    private final List<Flight> flights = new ArrayList<>();
    private final String tag;

    /**
     * Створити новий цикл із копії циклу src та рейсу flight в кінці із тим же тегом
     *
     * @param src
     * @param flight
     */
    public Cycle(Cycle src, Flight flight) {
        flights.addAll(src.getFlights());
        flights.add(flight);
        tag = src.getTag();
    }

    /**
     * Створити базовий цикл, що поки що складається лише із рейсу flight
     * (порожніх циклів не буває)
     *
     * @param flight
     */
    public Cycle(Flight flight) {
        flights.add(flight);
        // Допоміжна інформація про цикл: звідки він почався і коли
        this.tag = String.format("from: %d, departure time: %d", flight.getFrom(), flight.getDepartureTime());
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public String getTag() {
        return tag;
    }

    /**
     * Чи містить цикл рейс flight?
     *
     * @param flight
     * @return
     */
    public boolean contains(Flight flight) {
        return flights.contains(flight);
    }

    /**
     * Чи містить цикл обов'язкові рейси?
     *
     * @return
     */
    public boolean containsMandatory() {
        return flights.stream().anyMatch(Flight::isMandatory);
    }

    /**
     * Потік обов'язкових рейсів у цьому циклі
     *
     * @return
     */
    public Stream<Flight> getMandatory() {
        return flights.stream().filter(Flight::isMandatory);
    }

    /**
     * Останній рейс у цьому циклі (існує завжди)
     *
     * @return
     */
    public Flight getLast() {
        return flights.get(flights.size() - 1);
    }

    /**
     * Цикл містить передостанній рейс?
     *
     * @return
     */
    public boolean containsBeforeLast() {
        return flights.size() >= 2;
    }

    /**
     * Передостанній рейс у цьому циклі
     * (викликати цей метод лише після того, як предикат @see Cycle#containsBeforeLast повернув
     * true)
     *
     * @return
     */
    public Flight getBeforeLast() {
        return flights.get(flights.size() - 2);
    }

    /**
     * Стартова точка циклу - звідки вилетів нульовий рейс (існує завжди)
     *
     * @return
     */
    public int getReturnPoint() {
        return flights.get(0).getFrom();
    }

    /**
     * Витрати на цикл із витратами на очікування між рейсами включно
     *
     * @param waitTimeValueCalculator калькулятор для підрахунку витрат на очікування між рейсами
     * @return
     */
    public int getExpenses(Function<Integer, Integer> waitTimeValueCalculator) {
        int result = 0;
        for (int i = 0; i < flights.size(); i++) {
            result += flights.get(i).getExpenses() + waitTimeValueCalculator.apply(getWaitTime(i));
        }
        return result;
    }

    /**
     * Час очікування між рейсами
     *
     * @param index номер рейсу в циклі
     * @return
     */
    private int getWaitTime(int index) {
        return index == 0
                ? 0
                : flights.get(index).getDepartureTime() - flights.get(index - 1).getArrivalTime();
    }

}
