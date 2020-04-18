package edu.kpi.ipsa.opavloshchuk.airways;

import java.util.List;
import java.util.ArrayList;
import edu.kpi.ipsa.opavloshchuk.airways.calculation.Calculator;
import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightsStorage;
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
    private final List<List<Flight>> result = new ArrayList<>();

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("flight", new Flight());
        model.addAttribute("source", sourceFlightStorage.list());
        model.addAttribute("result", result);
        return "home";
    }

    @PostMapping("/")
    public String addFlight(@ModelAttribute Flight flight, Model model) {
        sourceFlightStorage.store(flight);
        return home(model);
    }

    @GetMapping("/remove")
    public String removeFlight(@RequestParam(name = "number", required = true) int number, Model model) {
        sourceFlightStorage.remove(number);
        return home(model);
    }

    @GetMapping("/calculate")
    public String calculate(Model model) {
        result.clear();
        result.addAll(new Calculator(sourceFlightStorage).get());
        return home(model);
    }

}
