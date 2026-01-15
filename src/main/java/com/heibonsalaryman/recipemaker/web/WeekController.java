package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.domain.WeekCandidate;
import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.domain.WeekStatus;
import com.heibonsalaryman.recipemaker.service.WeekPlanService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/weeks")
public class WeekController {

    private final WeekPlanService weekPlanService;

    public WeekController(WeekPlanService weekPlanService) {
        this.weekPlanService = weekPlanService;
    }

    @GetMapping("/{weekStart}")
    public String detail(@PathVariable("weekStart")
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                         Model model) {
        WeekPlan plan = weekPlanService.getOrCreateWeek(weekStart);
        List<WeekCandidate> candidates = weekPlanService.getLatestCandidates(plan);
        model.addAttribute("plan", plan);
        model.addAttribute("candidates", candidates);
        model.addAttribute("candidateCount", candidates.size());
        model.addAttribute("confirmedRecipe", plan.getConfirmedRecipe());
        model.addAttribute("confirmedRecipeMissing",
            plan.getStatus() == WeekStatus.CONFIRMED && plan.getConfirmedRecipe() == null);
        return "weeks/detail";
    }

    @PostMapping("/{weekStart}/generate")
    public String generate(@PathVariable("weekStart")
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                           RedirectAttributes redirectAttributes) {
        try {
            weekPlanService.generate(weekStart);
            return "redirect:/weeks/" + weekStart + "/candidates";
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/weeks/" + weekStart;
        }
    }

    @PostMapping("/{weekStart}/regenerate")
    public String regenerate(@PathVariable("weekStart")
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                             RedirectAttributes redirectAttributes) {
        try {
            weekPlanService.regenerate(weekStart);
            return "redirect:/weeks/" + weekStart + "/candidates";
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/weeks/" + weekStart;
        }
    }
}
