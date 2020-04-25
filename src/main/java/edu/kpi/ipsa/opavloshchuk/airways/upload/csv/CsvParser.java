package edu.kpi.ipsa.opavloshchuk.airways.upload.csv;

import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightValidator;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Завантажити рейси із CSV-файлу
 * 
 * Файл повинен мати таку структуру:
 * 
 * number,from,to,income,expenses,departureTime,arrivalTime,mandatory
 * number,from,to,income,expenses,departureTime,arrivalTime,mandatory
 * ...
 * 
 * Поля розділені комами. Перший рядок вважається заголовком та ігнорується.
 * Поле 'mandatory' може бути true або false, всі інші - цілі числа.
 */
public class CsvParser {

    private final byte[] content; // CSV-контент для парсингу
    private final List<Flight> flights = new ArrayList<>(); // отриманий список рейсів
    private final List<String> errors = new ArrayList<>(); // загальний список помилок
    private final List<String> parseErrors = new ArrayList<>(); // тимчасовий список помилок парсингу одного рядка

    public CsvParser(byte[] content) {
        if( content==null ) {
            throw new IllegalArgumentException("content is null");            
        }
        this.content = content;
    }

    /**
     * Розпарсити контент і сформувати список рейсів та список помилок
     * 
     * @throws IOException 
     */
    public void perform() throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)))) {
            // Лічильник потрібен для повідомлень про помилки
            int counter = -1;
            while (true) {
                counter++;
                final String row = reader.readLine();
                if (row == null) {
                    // Контент прочитано до кінця
                    break;
                }
                // Пропустити перший рядок і всі порожні рядки
                if (counter > 0 && !row.isEmpty()) {
                    parseErrors.clear();
                    final Flight flight = parseRow(counter, row);
                    if (parseErrors.isEmpty()) {
                        // Помилок парсингу нема - валідувати новий рейс
                        final Map<String, String> validationErrors = new FlightValidator().apply(flight);
                        if (validationErrors.isEmpty()) {
                            // Помилок валідації нема - добавити рейс до результату
                            flights.add(flight);
                        } else {
                            // Є помилки валідації - добавити їх до списку
                            consumeValidationErrors(counter, validationErrors);
                        }
                    } else {
                        // Є помилки парсингу - добавити їх до списку
                        errors.addAll(parseErrors);
                    }                            
                }
            }
        }
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public List<String> getErrors() {
        return errors;
    }

    private void consumeValidationErrors(int rowNum, Map<String, String> validationErrors) {
        validationErrors.values().forEach(text -> errors.add(String.format("row %d: %s", rowNum, text)));
    }

    /**
     * Розпарсити рядок і створити рейс
     * 
     * @param rowNum
     * @param row
     * @return 
     */
    private Flight parseRow(int rowNum, String row) {
        final String[] str = row.split("[,]");
        final Flight result = new Flight();
        result.setNumber(parseInt(rowNum, str, 0));
        result.setFrom(parseInt(rowNum, str, 1));
        result.setTo(parseInt(rowNum, str, 2));
        result.setIncome(parseInt(rowNum, str, 3));
        result.setExpenses(parseInt(rowNum, str, 4));
        result.setDepartureTime(parseInt(rowNum, str, 5));
        result.setArrivalTime(parseInt(rowNum, str, 6));
        result.setMandatory(parseBool(rowNum, str, 7));
        return result;
    }

    private int parseInt(int rowNum, String[] str, int i) {
        return parse(rowNum, str, i, Integer::parseInt, -1);
    }

    private boolean parseBool(int rowNum, String[] str, int i) {
        return parse(rowNum, str, i, Boolean::parseBoolean, false);
    }

    private <T> T parse(int rowNum, String[] str, int i, Function<String, T> parser, T invalidValue) {
        final String v = str[i].trim();
        try {
            return parser.apply(v);
        } catch (Exception ex) {
            parseErrors.add(String.format("row %d, column %d: unexpected value '%s'", rowNum, i, v));
            return invalidValue;
        }
    }

}
