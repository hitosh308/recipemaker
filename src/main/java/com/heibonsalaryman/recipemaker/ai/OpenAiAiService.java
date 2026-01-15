package com.heibonsalaryman.recipemaker.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class OpenAiAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiAiService.class);

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private final DummyAiService fallbackService;

    public OpenAiAiService(OpenAiClient openAiClient, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
        this.fallbackService = new DummyAiService();
    }

    @Override
    public ShelfLifeEstimate estimateShelfLifeDays(String name, StorageType storageType, String assumptions) {
        String systemPrompt = """
            You estimate shelf life for ingredients. Respond with JSON only.
            The JSON must be:
            {"days": number, "confidence": number, "assumptions": "string"}
            """.trim();
        String userPrompt = """
            Ingredient: %s
            Storage: %s
            Assumptions: %s
            Provide a single JSON object and nothing else.
            """.formatted(name, storageType, assumptions);
        try {
            String json = openAiClient.createJsonResponse(systemPrompt, userPrompt);
            ShelfLifeEstimateResponse response = objectMapper.readValue(json, ShelfLifeEstimateResponse.class);
            int days = response.days() == null ? 0 : response.days();
            double confidence = response.confidence() == null ? 0.0 : response.confidence();
            String responseAssumptions = response.assumptions() == null ? assumptions : response.assumptions();
            return new ShelfLifeEstimate(days, confidence, responseAssumptions);
        } catch (Exception ex) {
            log.warn("Shelf life estimate failed for {}: {}", name, ex.getMessage());
            return new ShelfLifeEstimate(0, 0.0, assumptions);
        }
    }

    @Override
    public List<Recipe> generateWeeklyRecipeCandidates(LocalDate weekStart, String constraints, String pantryContext,
                                                       String recentHistory) {
        String systemPrompt = """
            You are a recipe planner. Respond with JSON only and no extra text.
            JSON format:
            {
              "candidates": [
                {
                  "title": "string",
                  "servings": number,
                  "cookTimeMinutes": number,
                  "ingredients": [{"name":"string","quantity":number,"unit":"string"}],
                  "steps": ["string"],
                  "missingIngredients": [{"name":"string","quantity":number,"unit":"string","shelfLifeDays":number}],
                  "nutritionPerServing": {
                    "calories": number,
                    "protein": number,
                    "fat": number,
                    "carbs": number,
                    "salt": number,
                    "sugar": number
                  },
                  "tags": ["string"],
                  "mainIngredients": ["string"]
                }
              ],
              "notes": "string"
            }
            """.trim();
        String userPrompt = """
            Week start: %s
            Constraints: %s
            Pantry context (JSON): %s
            Recent history (JSON): %s
            Requirements:
            - Use pantry items close to expiration first.
            - Avoid repeating recent recipes or similar patterns.
            - Provide balanced nutrition and avoid extreme cook times (>60 min).
            - Return at least 3 candidates.
            Provide JSON only.
            """.formatted(weekStart, nullToValue(constraints, "N/A"),
            nullToValue(pantryContext, "[]"),
            nullToValue(recentHistory, "[]"));

        long start = System.nanoTime();
        try {
            String json = openAiClient.createJsonResponse(systemPrompt, userPrompt);
            WeeklyCandidatesResponse response = objectMapper.readValue(json, WeeklyCandidatesResponse.class);
            List<Recipe> recipes = mapToRecipes(response);
            long duration = Duration.ofNanos(System.nanoTime() - start).toMillis();
            log.info("OpenAI generated {} candidates for week {} in {} ms",
                recipes.size(), weekStart, duration);
            if (recipes.isEmpty()) {
                return fallbackCandidates(weekStart, "empty_response");
            }
            return recipes;
        } catch (Exception ex) {
            log.warn("OpenAI candidate generation failed for week {}: {}", weekStart, ex.getMessage());
            return fallbackCandidates(weekStart, "error");
        }
    }

    private List<Recipe> mapToRecipes(WeeklyCandidatesResponse response) {
        if (response == null || response.candidates() == null) {
            return List.of();
        }
        return response.candidates().stream()
            .map(this::mapToRecipe)
            .toList();
    }

    private Recipe mapToRecipe(WeeklyCandidate candidate) {
        Recipe recipe = new Recipe();
        recipe.setTitle(candidate.title() == null ? "Untitled Recipe" : candidate.title());
        recipe.setServings(candidate.servings());
        recipe.setCookTimeMinutes(candidate.cookTimeMinutes());
        recipe.setStepsJson(writeJson(candidate.steps() == null ? List.of() : candidate.steps()));
        recipe.setIngredientsJson(writeJson(candidate.ingredients() == null ? List.of() : candidate.ingredients()));
        recipe.setMissingIngredientsJson(writeJson(candidate.missingIngredients() == null
            ? List.of()
            : candidate.missingIngredients()));
        recipe.setNutritionPerServingJson(writeJson(candidate.nutritionPerServing() == null
            ? Collections.emptyMap()
            : candidate.nutritionPerServing()));
        recipe.setTagsJson(writeJson(candidate.tags() == null ? List.of() : candidate.tags()));
        recipe.setMainIngredientsJson(writeJson(candidate.mainIngredients() == null ? List.of() : candidate.mainIngredients()));
        recipe.setSource("OpenAI");
        return recipe;
    }

    private List<Recipe> fallbackCandidates(LocalDate weekStart, String reason) {
        List<Recipe> recipes = fallbackService.generateWeeklyRecipeCandidates(weekStart, null, null, null);
        recipes.forEach(recipe -> recipe.setSource("OpenAI-fallback-" + reason));
        return recipes;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            if (value instanceof java.util.Map<?, ?>) {
                return "{}";
            }
            return "[]";
        }
    }

    private String nullToValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record ShelfLifeEstimateResponse(Integer days, Double confidence, String assumptions) {
    }

    public record WeeklyCandidatesResponse(List<WeeklyCandidate> candidates, String notes) {
    }

    public record WeeklyCandidate(String title,
                                  Integer servings,
                                  Integer cookTimeMinutes,
                                  List<Ingredient> ingredients,
                                  List<String> steps,
                                  List<MissingIngredient> missingIngredients,
                                  Object nutritionPerServing,
                                  List<String> tags,
                                  List<String> mainIngredients) {
    }

    public record Ingredient(String name, Double quantity, String unit) {
    }

    public record MissingIngredient(String name, Double quantity, String unit, Integer shelfLifeDays) {
    }
}
