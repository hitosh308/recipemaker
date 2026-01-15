package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.repository.CookLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HistoryController {

    private final CookLogRepository cookLogRepository;

    public HistoryController(CookLogRepository cookLogRepository) {
        this.cookLogRepository = cookLogRepository;
    }

    @GetMapping("/history")
    public String history(Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<?> logs = cookLogRepository.findByCookedAtBetween(now.minusDays(14), now.plusDays(1));
        model.addAttribute("logs", logs);
        return "history";
    }
}
