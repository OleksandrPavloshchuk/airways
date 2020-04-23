package edu.kpi.ipsa.opavloshchuk.airways;

import java.util.List;
import java.util.ArrayList;
import edu.kpi.ipsa.opavloshchuk.airways.calculation.Calculator;
import edu.kpi.ipsa.opavloshchuk.airways.data.Cycle;
import edu.kpi.ipsa.opavloshchuk.airways.data.Flight;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightValidator;
import edu.kpi.ipsa.opavloshchuk.airways.data.FlightsStorage;
import edu.kpi.ipsa.opavloshchuk.airways.upload.csv.CsvParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Головний і єдиний контроллер односторінкового застосування
 */
@Controller
@Scope("session")
public class MainController {

    private final FlightsStorage sourceFlightStorage = new FlightsStorage();
    private final List<Cycle> cycles = new ArrayList<>();
    private final List<Flight> mandatoryFlightsWithoutCycles = new ArrayList<>();
    private final Map<String, String> validationErrors = new HashMap<>();
    private final List<String> importErrors = new ArrayList<>();

    // Відкрити головну сторінку
    @GetMapping("/")
    public String home(Model model) {
        clearErrors();
        return goHome(model);
    }

    /**
     * Отримати з форми новий рейс, провалідувати його, зберегти, коли валідний,
     * вивести помилки, коли невалідний і повернутися на головну сторінку
     *
     * @param flight новий рейс
     * @param model модель даних сторінки
     * @return назва головної сторінки
     */
    @PostMapping("/")
    public String addFlight(@ModelAttribute Flight flight, Model model) {
        clearErrors();
        final Map<String, String> valErrors = new FlightValidator().apply(flight);
        if (valErrors.isEmpty()) {
            sourceFlightStorage.store(flight);
        } else {
            validationErrors.putAll(valErrors);
        }
        return goHome(model);
    }

    /**
     * Видалити рейс за номером і повернутися на головну сторінку
     *
     * @param number номер рейсу
     * @param model модель даних сторінки
     * @return назва головної сторінки
     */
    @GetMapping("/remove")
    public String removeFlight(@RequestParam(name = "number", required = true) int number, Model model) {
        sourceFlightStorage.remove(number);
        clearErrors();
        return goHome(model);
    }

    /**
     * Завантажити CSV-файл
     *
     * @param file
     * @param redirectAttributes
     * @param model
     * @return
     */
    @PostMapping("/uploadCsv")
    public String uploadCsv(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes, Model model) throws IOException {
        cycles.clear();
        sourceFlightStorage.clear();
        mandatoryFlightsWithoutCycles.clear();
        clearErrors();
        final CsvParser parser = new CsvParser(file.getBytes());
        parser.perform();
        parser.getFlights().forEach(flight -> sourceFlightStorage.store(flight));
        importErrors.addAll(parser.getErrors());
        return goHome(model);
    }

    /**
     * Порахувати цикли і обов'язкові рейси поза циклами
     *
     * @param model
     * @return
     */
    @GetMapping("/calculate")
    public String calculate(Model model) {
        cycles.clear();
        mandatoryFlightsWithoutCycles.clear();
        clearErrors();
        final Calculator calculator = new Calculator(sourceFlightStorage.list());
        calculator.perform();
        cycles.addAll(calculator.getCycles());
        mandatoryFlightsWithoutCycles.addAll(calculator.getMandatoryFlightsWithoutCycles());
        return goHome(model);
    }

    /**
     * Заповнити модель даних і перейти на головну сторінку
     *
     * @param model
     * @return
     */
    private String goHome(Model model) {
        model.addAttribute("flight", new Flight());
        model.addAttribute("source", sourceFlightStorage.list());
        model.addAttribute("cycles", cycles);
        model.addAttribute("mandatoryFlightsWithoutCycles", mandatoryFlightsWithoutCycles);
        model.addAttribute("validationErrors", validationErrors);
        model.addAttribute("importErrors", importErrors);
        return "home";
    }
    
    /**
     * Почистити всі повідомлення про помилки
     */
    private void clearErrors() {
        importErrors.clear();
        validationErrors.clear();        
    }

}
