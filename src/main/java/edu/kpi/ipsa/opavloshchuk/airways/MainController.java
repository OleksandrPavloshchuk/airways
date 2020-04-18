package edu.kpi.ipsa.opavloshchuk.airways;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@RequestParam(name = "name", required = false, defaultValue = "<default name>") String name, Model model) {
        model.addAttribute("name", name);
        return "home";
    }
}
