package com.heibonsalaryman.recipemaker.api;

import com.heibonsalaryman.recipemaker.service.HealthService;
import com.heibonsalaryman.recipemaker.web.dto.HealthViewModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthApiController {

    private final HealthService healthService;

    public HealthApiController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/summary")
    public HealthViewModel summary(@RequestParam(value = "days", defaultValue = "14") int days) {
        return healthService.getHealthSummary(days);
    }
}
