package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.service.HealthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public String health(Model model) {
        model.addAttribute("summaries", healthService.summarizeLastDays(14));
        return "health";
    }
}
