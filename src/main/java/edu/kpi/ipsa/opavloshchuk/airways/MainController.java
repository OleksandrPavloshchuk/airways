package edu.kpi.ipsa.opavloshchuk.airways;

import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightsStorage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    
    private final FlightsStorage sourceFlightStorage = new FlightsStorage();
    private final FlightsStorage heuristicResultFlightStorage = new FlightsStorage();
    private final FlightsStorage finalResultFlightStorage = new FlightsStorage();

    @GetMapping("/")
    public String home(Model model) {
        return goHome(model);
    }
    
    @PostMapping("/")
    public String addFlight(@ModelAttribute Flight flight, Model model) {
        sourceFlightStorage.store(flight);
        return goHome(model);
    }
    
    @GetMapping("/remove")
    public String removeFlight(@RequestParam(name="number", required = true) int number, Model model) {
        sourceFlightStorage.remove(number);
        return goHome(model);
    }
    
    @GetMapping("/calculate")
    public String calculate(Model model) {
        // TODO calculate
        sourceFlightStorage.list().forEach( f -> heuristicResultFlightStorage.store(f));
        
        return goHome(model);
    }
    
    private String goHome(Model model) {
        model.addAttribute("flight", new Flight());
        model.addAttribute("source", sourceFlightStorage.list());        
        model.addAttribute("heuristicResult", heuristicResultFlightStorage.list());    
        model.addAttribute("finalResult", finalResultFlightStorage.list());    
        return "home";
    }
}