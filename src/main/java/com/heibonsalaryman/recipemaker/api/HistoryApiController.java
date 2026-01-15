package com.heibonsalaryman.recipemaker.api;

import com.heibonsalaryman.recipemaker.api.dto.HistoryDtos;
import com.heibonsalaryman.recipemaker.domain.CookLog;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.repository.CookLogRepository;
import com.heibonsalaryman.recipemaker.repository.RecipeRepository;
import com.heibonsalaryman.recipemaker.util.WeekUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
public class HistoryApiController {

    private final CookLogRepository cookLogRepository;
    private final RecipeRepository recipeRepository;

    public HistoryApiController(CookLogRepository cookLogRepository, RecipeRepository recipeRepository) {
        this.cookLogRepository = cookLogRepository;
        this.recipeRepository = recipeRepository;
    }

    @GetMapping
    public List<CookLog> list(@RequestParam("from")
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                              @RequestParam("to")
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = LocalDateTime.of(to, LocalTime.MAX);
        return cookLogRepository.findByCookedAtBetween(start, end);
    }

    @PostMapping
    public CookLog create(@RequestBody HistoryDtos.CreateRequest request) {
        CookLog log = new CookLog();
        Recipe recipe = null;
        if (request.recipeId() != null) {
            recipe = recipeRepository.findById(request.recipeId())
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        }
        log.setRecipe(recipe);
        LocalDateTime cookedAt = request.cookedAt() == null ? LocalDateTime.now() : request.cookedAt();
        log.setCookedAt(cookedAt);
        log.setWeekStart(WeekUtil.getWeekStart(cookedAt.toLocalDate()));
        log.setServings(request.servings());
        log.setNutritionTotalJson(request.nutritionTotalJson());
        log.setNutritionPerServingJson(request.nutritionPerServingJson());
        log.setTagsJson(request.tagsJson());
        log.setMainIngredientsJson(request.mainIngredientsJson());
        return cookLogRepository.save(log);
    }
}
