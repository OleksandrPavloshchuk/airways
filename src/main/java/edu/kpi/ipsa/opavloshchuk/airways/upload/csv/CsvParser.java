package edu.kpi.ipsa.opavloshchuk.airways.upload.csv;

import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightValidator;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvParser {

    private final byte[] content;
    private final List<Flight> flights = new ArrayList<>();
    private final Map<Integer, String> errors = new LinkedHashMap<>();

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
                if (counter > 0) {
                    final Flight flight = parseRow(row);
                    final Map<String, String> validationErrors = new FlightValidator().apply(flight);
                    if (validationErrors.isEmpty()) {
                        flights.add(flight);
                    } else {
                        // TODO add errors
                    }
                }
            }
        }
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public Map<Integer, String> getErrors() {
        return errors;
    }

    private Flight parseRow(String row) {
        final String[] str = row.split("[;]");
        final Flight result = new Flight();
        result.setNumber(parseInt(str, 0));
        result.setFrom(parseInt(str, 1));
        result.setTo(parseInt(str, 2));
        result.setCost(parseInt(str, 3));
        result.setDepartureTime(parseInt(str, 4));
        result.setArrivalTime(parseInt(str, 5));
        result.setMandatory(Boolean.parseBoolean(str[6].trim()));
        return result;
    }

    private static int parseInt(String[] str, int i) {
        return Integer.parseInt(str[i].trim());
    }

}
