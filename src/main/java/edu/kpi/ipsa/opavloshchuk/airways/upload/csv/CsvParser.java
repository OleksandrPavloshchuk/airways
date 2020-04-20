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

public class CsvParser {

    private final byte[] content;
    private final List<Flight> flights = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<String> parseErrors = new ArrayList<>();

    public CsvParser(byte[] content) {
        this.content = content;
    }

    public void perform() throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            int counter = -1;
            while (true) {
                counter++;
                final String row = reader.readLine();
                if (row == null) {
                    break;
                }
                if (counter > 0 && !row.isEmpty()) {
                    parseErrors.clear();
                    final Flight flight = parseRow(counter, row);
                    if (parseErrors.isEmpty()) {
                        final Map<String, String> validationErrors = new FlightValidator().apply(flight);
                        if (validationErrors.isEmpty()) {
                            flights.add(flight);
                        } else {
                            consumeValidationErrors(counter, validationErrors);
                        }
                    } else {
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

    private Flight parseRow(int rowNum, String row) {
        final String[] str = row.split("[,]");
        final Flight result = new Flight();
        result.setNumber(parseInt(rowNum, str, 0));
        result.setFrom(parseInt(rowNum, str, 1));
        result.setTo(parseInt(rowNum, str, 2));
        result.setCost(parseInt(rowNum, str, 3));
        result.setDepartureTime(parseInt(rowNum, str, 4));
        result.setArrivalTime(parseInt(rowNum, str, 5));
        result.setMandatory(parseBool(rowNum, str, 6));
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
