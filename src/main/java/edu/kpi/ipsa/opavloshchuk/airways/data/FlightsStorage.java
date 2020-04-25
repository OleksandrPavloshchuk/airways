package edu.kpi.ipsa.opavloshchuk.airways.data;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Контейнер рейсів
 *
 * В даній імплементації тимчасовий
 */
public class FlightsStorage {

    // Внутрішній контейнер рейсів. Забезпечує унікальність рейсу за номером
    private final Map<Integer, Flight> data = new HashMap<>();

    /**
     * Зберегти рейс flight. Коли такий рейс вже є в контейнері, то він буде переписаний.
     *
     * @param flight
     */
    public void store(Flight flight) {
        if (flight != null) {
            data.put(flight.getNumber(), flight);
        }
    }

    /**
     * Видалити рейс із номером number із контейнера
     *
     * @param number
     */
    public void remove(int number) {
        if (data.containsKey(number)) {
            data.remove(number);
        }
    }

    /**
     * Список рейсів, відсортований за часом відправлення
     *
     * @return
     */
    public List<Flight> list() {
        final List<Flight> result = new ArrayList<>(data.values());
        result.sort((f1, f2) -> f1.getDepartureTime() - f2.getDepartureTime());
        return result;
    }

    /**
     * Очистити контейнер
     */
    public void clear() {
        data.clear();
    }

}
