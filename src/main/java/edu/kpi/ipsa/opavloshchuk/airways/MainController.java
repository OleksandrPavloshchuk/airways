package edu.kpi.ipsa.opavloshchuk.airways;

import java.util.List;
import java.util.ArrayList;
import edu.kpi.ipsa.opavloshchuk.airways.calculation.Calculator;
import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightValidator;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightsStorage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Scope("session")
public class MainController {

    private final FlightsStorage sourceFlightStorage = new FlightsStorage();
    private final List<List<Flight>> cycles = new ArrayList<>();
    private final List<Flight> mandatoryFlightsWithoutCycles = new ArrayList<>();
    private final Map<String, String> validationErrors = new HashMap<>();

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("flight", new Flight());
        model.addAttribute("source", sourceFlightStorage.list());
        model.addAttribute("cycles", cycles);
        model.addAttribute("mandatoryFlightsWithoutCycles", mandatoryFlightsWithoutCycles);
        model.addAttribute("validationErrors", validationErrors);
        return "home";
    }

    @PostMapping("/")
    public String addFlight(@ModelAttribute Flight flight, Model model) {
        validationErrors.clear();
        final Map<String, String> valErrors = new FlightValidator().apply(flight);
        if (valErrors.isEmpty()) {
            sourceFlightStorage.store(flight);
        } else {
            validationErrors.putAll(valErrors);
        }
        return home(model);
    }

    @GetMapping("/remove")
    public String removeFlight(@RequestParam(name = "number", required = true) int number, Model model) {
        sourceFlightStorage.remove(number);
        return home(model);
    }

    @GetMapping("/calculate")
    public String calculate(Model model) {
        cycles.clear();
        mandatoryFlightsWithoutCycles.clear();
        final Calculator calculator = new Calculator(sourceFlightStorage.list());
        calculator.perform();
        cycles.addAll(calculator.getCycles());
        mandatoryFlightsWithoutCycles.addAll(calculator.getMandatoryFlightsWithoutCycles());
        return home(model);
    }

}
