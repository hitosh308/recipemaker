package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.service.WeekPlanService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final WeekPlanService weekPlanService;

    public HomeController(WeekPlanService weekPlanService) {
        this.weekPlanService = weekPlanService;
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "month", required = false) String month, Model model) {
        YearMonth target = month == null ? YearMonth.now() : YearMonth.parse(month);
        List<WeekPlan> weeks = weekPlanService.getOrCreateWeeks(target);
        model.addAttribute("month", target);
        model.addAttribute("weeks", weeks);
        return "home";
    }

    @GetMapping("/weeks/{weekStart}")
    public String weekDetail(@PathVariable("weekStart")
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                             Model model) {
        WeekPlan plan = weekPlanService.getWeek(weekStart);
        model.addAttribute("plan", plan);
        model.addAttribute("candidates", weekPlanService.getLatestCandidates(plan));
        return "week";
    }
}
