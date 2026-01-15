package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.ai.AiService;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.WeekCandidate;
import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.domain.WeekStatus;
import com.heibonsalaryman.recipemaker.repository.RecipeRepository;
import com.heibonsalaryman.recipemaker.repository.WeekCandidateRepository;
import com.heibonsalaryman.recipemaker.repository.WeekPlanRepository;
import com.heibonsalaryman.recipemaker.util.WeekUtil;
import com.heibonsalaryman.recipemaker.util.WeekUtil.WeekRange;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeekPlanService {

    private final WeekPlanRepository weekPlanRepository;
    private final WeekCandidateRepository weekCandidateRepository;
    private final RecipeRepository recipeRepository;
    private final AiService aiService;

    public WeekPlanService(WeekPlanRepository weekPlanRepository,
                           WeekCandidateRepository weekCandidateRepository,
                           RecipeRepository recipeRepository,
                           AiService aiService) {
        this.weekPlanRepository = weekPlanRepository;
        this.weekCandidateRepository = weekCandidateRepository;
        this.recipeRepository = recipeRepository;
        this.aiService = aiService;
    }

    @Transactional
    public List<WeekPlan> getOrCreateWeeks(YearMonth month) {
        List<WeekRange> ranges = WeekUtil.getWeeksForMonth(month);
        for (WeekRange range : ranges) {
            weekPlanRepository.findByWeekStart(range.weekStart())
                .orElseGet(() -> {
                    WeekPlan plan = new WeekPlan();
                    plan.setWeekStart(range.weekStart());
                    plan.setWeekEnd(range.weekEnd());
                    plan.setStatus(WeekStatus.NOT_CREATED);
                    return weekPlanRepository.save(plan);
                });
        }
        return ranges.stream()
            .map(range -> weekPlanRepository.findByWeekStart(range.weekStart()).orElseThrow())
            .sorted(Comparator.comparing(WeekPlan::getWeekStart))
            .toList();
    }

    public WeekPlan getWeek(LocalDate weekStart) {
        return weekPlanRepository.findByWeekStart(weekStart)
            .orElseThrow(() -> new IllegalArgumentException("Week plan not found"));
    }

    @Transactional
    public WeekPlan generate(LocalDate weekStart) {
        WeekPlan plan = getWeek(weekStart);
        if (plan.getStatus() != WeekStatus.NOT_CREATED) {
            throw new IllegalStateException("Week already generated");
        }
        plan.setStatus(WeekStatus.GENERATING);
        WeekPlan saved = weekPlanRepository.save(plan);
        createCandidates(saved, 1);
        return saved;
    }

    @Transactional
    public WeekPlan regenerate(LocalDate weekStart) {
        WeekPlan plan = getWeek(weekStart);
        if (plan.getStatus() != WeekStatus.GENERATING) {
            throw new IllegalStateException("Week is not generating");
        }
        int nextVersion = weekCandidateRepository.findTopByWeekPlanOrderByCandidateGroupVersionDesc(plan)
            .map(WeekCandidate::getCandidateGroupVersion)
            .orElse(0) + 1;
        createCandidates(plan, nextVersion);
        return plan;
    }

    @Transactional
    public WeekPlan confirm(LocalDate weekStart, UUID recipeId) {
        WeekPlan plan = getWeek(weekStart);
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        plan.setConfirmedRecipe(recipe);
        plan.setStatus(WeekStatus.CONFIRMED);
        return weekPlanRepository.save(plan);
    }

    public List<WeekCandidate> getLatestCandidates(WeekPlan plan) {
        Optional<WeekCandidate> latest = weekCandidateRepository.findTopByWeekPlanOrderByCandidateGroupVersionDesc(plan);
        if (latest.isEmpty()) {
            return List.of();
        }
        return weekCandidateRepository.findByWeekPlanAndCandidateGroupVersion(plan, latest.get().getCandidateGroupVersion());
    }

    private void createCandidates(WeekPlan plan, int candidateGroupVersion) {
        List<Recipe> recipes = aiService.generateWeeklyRecipeCandidates(plan.getWeekStart(), null, null, null);
        recipeRepository.saveAll(recipes);
        for (Recipe recipe : recipes) {
            WeekCandidate candidate = new WeekCandidate();
            candidate.setWeekPlan(plan);
            candidate.setRecipe(recipe);
            candidate.setCandidateGroupVersion(candidateGroupVersion);
            weekCandidateRepository.save(candidate);
        }
    }
}
