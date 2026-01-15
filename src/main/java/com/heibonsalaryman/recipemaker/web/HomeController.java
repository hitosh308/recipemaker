package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.service.WeekPlanService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
        List<WeekCalendarRow> calendar = weeks.stream()
            .map(week -> new WeekCalendarRow(week, buildWeekDays(week.getWeekStart())))
            .toList();
        model.addAttribute("month", target);
        model.addAttribute("calendar", calendar);
        model.addAttribute("currentMonth", YearMonth.now());
        return "home";
    }

    private List<LocalDate> buildWeekDays(LocalDate weekStart) {
        return IntStream.range(0, 7)
            .mapToObj(weekStart::plusDays)
            .toList();
    }

    public record WeekCalendarRow(WeekPlan plan, List<LocalDate> days) {
    }
}
