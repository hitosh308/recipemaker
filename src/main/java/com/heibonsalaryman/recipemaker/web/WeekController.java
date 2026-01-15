package com.heibonsalaryman.recipemaker.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.WeekCandidate;
import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.domain.WeekStatus;
import com.heibonsalaryman.recipemaker.service.WeekPlanService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/weeks")
public class WeekController {

    private final WeekPlanService weekPlanService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeekController(WeekPlanService weekPlanService) {
        this.weekPlanService = weekPlanService;
    }

    @GetMapping("/{weekStart}/candidates")
    public String candidates(@PathVariable("weekStart")
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                             Model model) {
        WeekPlan plan = weekPlanService.getOrCreateWeek(weekStart);
        List<WeekCandidate> candidates = weekPlanService.getLatestCandidates(plan);
        List<CandidateView> candidateViews = candidates.stream()
            .map(candidate -> toCandidateView(candidate.getRecipe()))
            .toList();
        model.addAttribute("plan", plan);
        model.addAttribute("candidates", candidateViews);
        model.addAttribute("candidateCount", candidateViews.size());
        model.addAttribute("confirmedRecipe", plan.getConfirmedRecipe());
        return "weeks/candidates";
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

    @PostMapping("/{weekStart}/confirm")
    public String confirm(@PathVariable("weekStart")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                          @RequestParam("recipeId") UUID recipeId,
                          RedirectAttributes redirectAttributes) {
        try {
            weekPlanService.confirm(weekStart, recipeId);
            return "redirect:/weeks/" + weekStart;
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/weeks/" + weekStart;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/weeks/" + weekStart + "/candidates";
        }
    }

    private CandidateView toCandidateView(Recipe recipe) {
        int missingCount = countJsonArray(recipe.getMissingIngredientsJson());
        String nutritionSummary = buildNutritionSummary(recipe.getNutritionPerServingJson());
        List<String> tags = parseStringArray(recipe.getTagsJson());
        return new CandidateView(recipe, missingCount, nutritionSummary, tags);
    }

    private int countJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return 0;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.isArray() ? node.size() : 0;
        } catch (Exception ex) {
            return 0;
        }
    }

    private List<String> parseStringArray(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return List.of();
            }
            List<String> values = new ArrayList<>();
            for (JsonNode entry : node) {
                values.add(entry.asText());
            }
            return values;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String buildNutritionSummary(String json) {
        if (json == null || json.isBlank()) {
            return "-";
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isObject()) {
                return json;
            }
            Map<String, String> labelMap = Map.of(
                "calories", "カロリー",
                "protein", "たんぱく質",
                "salt", "塩分"
            );
            List<String> pieces = new ArrayList<>();
            for (Map.Entry<String, String> entry : labelMap.entrySet()) {
                if (node.has(entry.getKey())) {
                    pieces.add(String.format(Locale.JAPAN, "%s: %s", entry.getValue(), node.get(entry.getKey()).asText()));
                }
            }
            return pieces.isEmpty() ? json : String.join(" / ", pieces);
        } catch (Exception ex) {
            return json;
        }
    }

    public record CandidateView(Recipe recipe, int missingCount, String nutritionSummary, List<String> tags) {
    }
}
