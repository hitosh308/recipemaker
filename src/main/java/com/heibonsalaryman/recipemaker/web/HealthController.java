package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.service.HealthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public String health(@RequestParam(value = "days", defaultValue = "14") int days, Model model) {
        model.addAttribute("pageTitle", "健康ダッシュボード");
        model.addAttribute("viewModel", healthService.getHealthSummary(days));
        return "health/index";
    }
}
