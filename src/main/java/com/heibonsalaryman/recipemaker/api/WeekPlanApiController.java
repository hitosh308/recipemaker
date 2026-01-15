package com.heibonsalaryman.recipemaker.api;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heibonsalaryman.recipemaker.api.dto.WeekDtos;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.WeekCandidate;
import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.service.WeekPlanService;

@RestController
@RequestMapping("/api/weeks")
public class WeekPlanApiController {

    private final WeekPlanService weekPlanService;

    public WeekPlanApiController(WeekPlanService weekPlanService) {
        this.weekPlanService = weekPlanService;
    }

    @GetMapping
    public List<WeekDtos.WeekSummary> listWeeks(@RequestParam("month") String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return weekPlanService.getOrCreateWeeks(yearMonth).stream()
            .map(this::toSummary)
            .toList();
    }

    @GetMapping("/{weekStart}")
    public WeekDtos.WeekDetail getWeek(@PathVariable("weekStart")
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        WeekPlan plan = weekPlanService.getWeek(weekStart);
        List<WeekCandidate> candidates = weekPlanService.getLatestCandidates(plan);
        return toDetail(plan, candidates);
    }

    @PostMapping("/{weekStart}/generate")
    public WeekDtos.WeekDetail generate(@PathVariable("weekStart")
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        WeekPlan plan = weekPlanService.generate(weekStart);
        List<WeekCandidate> candidates = weekPlanService.getLatestCandidates(plan);
        return toDetail(plan, candidates);
    }

    @PostMapping("/{weekStart}/regenerate")
    public WeekDtos.WeekDetail regenerate(@PathVariable("weekStart")
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        WeekPlan plan = weekPlanService.regenerate(weekStart);
        List<WeekCandidate> candidates = weekPlanService.getLatestCandidates(plan);
        return toDetail(plan, candidates);
    }

    @PostMapping("/{weekStart}/confirm")
    public WeekDtos.WeekDetail confirm(@PathVariable("weekStart")
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                                       @RequestBody WeekDtos.ConfirmRequest request) {
        WeekPlan plan = weekPlanService.confirm(weekStart, request.recipeId());
        List<WeekCandidate> candidates = weekPlanService.getLatestCandidates(plan);
        return toDetail(plan, candidates);
    }

    private WeekDtos.WeekSummary toSummary(WeekPlan plan) {
        WeekDtos.RecipeSummary summary = toRecipeSummary(plan.getConfirmedRecipe());
        return new WeekDtos.WeekSummary(plan.getWeekStart(), plan.getWeekEnd(), plan.getStatus(), summary);
    }

    private WeekDtos.WeekDetail toDetail(WeekPlan plan, List<WeekCandidate> candidates) {
        List<WeekDtos.WeekCandidate> candidateDtos = candidates.stream()
            .map(candidate -> new WeekDtos.WeekCandidate(candidate.getId(), toRecipeSummary(candidate.getRecipe())))
            .toList();
        return new WeekDtos.WeekDetail(plan.getWeekStart(), plan.getWeekEnd(), plan.getStatus(),
            toRecipeSummary(plan.getConfirmedRecipe()), candidateDtos);
    }

    private WeekDtos.RecipeSummary toRecipeSummary(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
        return new WeekDtos.RecipeSummary(recipe.getId(), recipe.getTitle());
    }
}
