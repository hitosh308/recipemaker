package com.heibonsalaryman.recipemaker.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.heibonsalaryman.recipemaker.domain.CookLog;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.domain.WeekStatus;
import com.heibonsalaryman.recipemaker.repository.CookLogRepository;
import com.heibonsalaryman.recipemaker.repository.RecipeRepository;
import com.heibonsalaryman.recipemaker.service.ShoppingService;
import com.heibonsalaryman.recipemaker.service.WeekPlanService;
import com.heibonsalaryman.recipemaker.util.WeekUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
public class WeekRecipeController {

    private final WeekPlanService weekPlanService;
    private final RecipeRepository recipeRepository;
    private final ShoppingService shoppingService;
    private final CookLogRepository cookLogRepository;
    private final ObjectMapper objectMapper;

    public WeekRecipeController(WeekPlanService weekPlanService,
                                RecipeRepository recipeRepository,
                                ShoppingService shoppingService,
                                CookLogRepository cookLogRepository,
                                ObjectMapper objectMapper) {
        this.weekPlanService = weekPlanService;
        this.recipeRepository = recipeRepository;
        this.shoppingService = shoppingService;
        this.cookLogRepository = cookLogRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{weekStart}/recipes/{recipeId}")
    public String detail(@PathVariable("weekStart")
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                         @PathVariable("recipeId") UUID recipeId,
                         Model model) {
        WeekPlan plan = weekPlanService.getOrCreateWeek(weekStart);
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        ParseResult<List<IngredientView>> ingredientsResult = parseIngredients(recipe.getIngredientsJson());
        ParseResult<List<String>> stepsResult = parseSteps(recipe.getStepsJson());
        ParseResult<List<MissingIngredientData>> missingResult = parseMissingIngredients(recipe.getMissingIngredientsJson());
        ParseResult<List<NutritionItem>> nutritionResult = parseNutrition(recipe.getNutritionPerServingJson());
        ParseResult<List<String>> tagsResult = parseStringArray(recipe.getTagsJson());

        model.addAttribute("plan", plan);
        model.addAttribute("recipe", recipe);
        model.addAttribute("backUrl", buildBackUrl(plan));
        model.addAttribute("ingredients", ingredientsResult.value());
        model.addAttribute("ingredientsParseError", ingredientsResult.error());
        model.addAttribute("steps", stepsResult.value());
        model.addAttribute("stepsParseError", stepsResult.error());
        model.addAttribute("missingIngredients", missingResult.value());
        model.addAttribute("missingIngredientsParseError", missingResult.error());
        model.addAttribute("nutritionItems", nutritionResult.value());
        model.addAttribute("nutritionParseError", nutritionResult.error());
        model.addAttribute("tags", tagsResult.value());
        model.addAttribute("tagsParseError", tagsResult.error());
        model.addAttribute("showCookButton", plan.getStatus() == WeekStatus.CONFIRMED);
        return "weeks/recipe-detail";
    }

    @PostMapping("/{weekStart}/recipes/{recipeId}/add-missing-to-shopping")
    public String addMissingToShopping(@PathVariable("weekStart")
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                                       @PathVariable("recipeId") UUID recipeId,
                                       RedirectAttributes redirectAttributes) {
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        ParseResult<List<MissingIngredientData>> missingResult = parseMissingIngredients(recipe.getMissingIngredientsJson());
        if (missingResult.error()) {
            redirectAttributes.addFlashAttribute("errorMessage", "不足食材の解析に失敗しました。");
            return "redirect:/weeks/" + weekStart + "/recipes/" + recipeId;
        }
        List<ShoppingService.MissingIngredientInput> inputs = missingResult.value().stream()
            .map(item -> new ShoppingService.MissingIngredientInput(
                item.name(),
                item.quantityValue(),
                item.unit(),
                item.shelfLifeDays()
            ))
            .toList();
        if (inputs.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "不足食材はありませんでした。");
            return "redirect:/weeks/" + weekStart + "/recipes/" + recipeId;
        }
        int count = shoppingService.addMissingIngredientsToShopping(inputs);
        redirectAttributes.addFlashAttribute("successMessage", "不足食材を買い物リストに追加しました (" + count + " 件)。");
        return "redirect:/weeks/" + weekStart + "/recipes/" + recipeId;
    }

    @PostMapping("/{weekStart}/recipes/{recipeId}/cook")
    public String cook(@PathVariable("weekStart")
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                       @PathVariable("recipeId") UUID recipeId,
                       RedirectAttributes redirectAttributes) {
        WeekPlan plan = weekPlanService.getOrCreateWeek(weekStart);
        if (plan.getStatus() != WeekStatus.CONFIRMED) {
            redirectAttributes.addFlashAttribute("errorMessage", "確定済みの週のみ調理履歴に追加できます。");
            return "redirect:/weeks/" + weekStart + "/recipes/" + recipeId;
        }
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        CookLog log = new CookLog();
        log.setRecipe(recipe);
        log.setCookedAt(LocalDateTime.now());
        log.setWeekStart(WeekUtil.getWeekStart(log.getCookedAt().toLocalDate()));
        log.setServings(recipe.getServings());
        log.setNutritionPerServingJson(recipe.getNutritionPerServingJson());
        log.setNutritionTotalJson(buildNutritionTotal(recipe.getNutritionPerServingJson(), recipe.getServings()));
        log.setTagsJson(recipe.getTagsJson());
        log.setMainIngredientsJson(recipe.getMainIngredientsJson());
        cookLogRepository.save(log);
        return "redirect:/history";
    }

    private String buildBackUrl(WeekPlan plan) {
        LocalDate start = plan.getWeekStart();
        if (plan.getStatus() == WeekStatus.GENERATING) {
            return "/weeks/" + start + "/candidates";
        }
        return "/weeks/" + start;
    }

    private ParseResult<List<IngredientView>> parseIngredients(String json) {
        if (json == null || json.isBlank()) {
            return new ParseResult<>(List.of(), false);
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return new ParseResult<>(List.of(), true);
            }
            List<IngredientView> ingredients = new ArrayList<>();
            for (JsonNode entry : node) {
                String name = extractText(entry, List.of("name", "ingredient", "item", "food"));
                if (name == null || name.isBlank()) {
                    name = entry.isTextual() ? entry.asText() : "不明";
                }
                String quantity = extractText(entry, List.of("quantity", "amount", "qty"));
                String unit = extractText(entry, List.of("unit"));
                boolean priority = entry.path("stockPriority").asBoolean(false)
                    || entry.path("useStockFirst").asBoolean(false)
                    || entry.path("inventoryPriority").asBoolean(false);
                ingredients.add(new IngredientView(name, quantity, unit, priority));
            }
            return new ParseResult<>(ingredients, false);
        } catch (Exception ex) {
            return new ParseResult<>(List.of(), true);
        }
    }

    private ParseResult<List<String>> parseSteps(String json) {
        if (json == null || json.isBlank()) {
            return new ParseResult<>(List.of(), false);
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return new ParseResult<>(List.of(), true);
            }
            List<String> steps = new ArrayList<>();
            for (JsonNode entry : node) {
                String text = entry.isTextual() ? entry.asText() : extractText(entry, List.of("step", "description", "text"));
                if (text != null && !text.isBlank()) {
                    steps.add(text);
                }
            }
            return new ParseResult<>(steps, false);
        } catch (Exception ex) {
            return new ParseResult<>(List.of(), true);
        }
    }

    private ParseResult<List<MissingIngredientData>> parseMissingIngredients(String json) {
        if (json == null || json.isBlank()) {
            return new ParseResult<>(List.of(), false);
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return new ParseResult<>(List.of(), true);
            }
            List<MissingIngredientData> missing = new ArrayList<>();
            for (JsonNode entry : node) {
                String name = extractText(entry, List.of("name", "ingredient", "item", "food"));
                if (name == null || name.isBlank()) {
                    name = entry.isTextual() ? entry.asText() : "不明";
                }
                List<String> quantityKeys = List.of("quantity", "amount", "qty");
                String quantityText = extractText(entry, quantityKeys);
                Double quantityValue = parseNumberFromNode(entry, quantityKeys, quantityText);
                String unit = extractText(entry, List.of("unit"));
                Integer shelfLifeDays = entry.has("shelfLifeDays") && entry.get("shelfLifeDays").isNumber()
                    ? entry.get("shelfLifeDays").asInt()
                    : null;
                missing.add(new MissingIngredientData(name, quantityText, quantityValue, unit, shelfLifeDays));
            }
            return new ParseResult<>(missing, false);
        } catch (Exception ex) {
            return new ParseResult<>(List.of(), true);
        }
    }

    private ParseResult<List<NutritionItem>> parseNutrition(String json) {
        if (json == null || json.isBlank()) {
            return new ParseResult<>(List.of(), false);
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isObject()) {
                return new ParseResult<>(List.of(new NutritionItem("栄養", json)), false);
            }
            Map<String, String> labelMap = new LinkedHashMap<>();
            labelMap.put("calories", "カロリー");
            labelMap.put("protein_g", "たんぱく質");
            labelMap.put("protein", "たんぱく質");
            labelMap.put("fat_g", "脂質");
            labelMap.put("fat", "脂質");
            labelMap.put("carbs_g", "炭水化物");
            labelMap.put("carbs", "炭水化物");
            labelMap.put("salt_g", "塩分");
            labelMap.put("salt", "塩分");
            labelMap.put("sugar_g", "糖質");
            labelMap.put("sugar", "糖質");
            List<NutritionItem> items = new ArrayList<>();
            for (Map.Entry<String, String> entry : labelMap.entrySet()) {
                if (node.has(entry.getKey()) && !node.get(entry.getKey()).isNull()) {
                    items.add(new NutritionItem(entry.getValue(), node.get(entry.getKey()).asText()));
                }
            }
            return new ParseResult<>(items, false);
        } catch (Exception ex) {
            return new ParseResult<>(List.of(), true);
        }
    }

    private ParseResult<List<String>> parseStringArray(String json) {
        if (json == null || json.isBlank()) {
            return new ParseResult<>(List.of(), false);
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return new ParseResult<>(List.of(), true);
            }
            List<String> values = new ArrayList<>();
            for (JsonNode entry : node) {
                if (entry.isTextual()) {
                    values.add(entry.asText());
                }
            }
            return new ParseResult<>(values, false);
        } catch (Exception ex) {
            return new ParseResult<>(List.of(), true);
        }
    }

    private String extractText(JsonNode node, List<String> keys) {
        if (node == null) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull()) {
                return value.asText();
            }
        }
        return null;
    }

    private Double parseDouble(String textValue, JsonNode fallbackNode) {
        if (fallbackNode != null && fallbackNode.isNumber()) {
            return fallbackNode.asDouble();
        }
        if (textValue == null || textValue.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(textValue);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double parseNumberFromNode(JsonNode node, List<String> keys, String textValue) {
        if (node != null && node.isObject()) {
            for (String key : keys) {
                JsonNode candidate = node.get(key);
                if (candidate != null && candidate.isNumber()) {
                    return candidate.asDouble();
                }
            }
        }
        return parseDouble(textValue, null);
    }

    private String buildNutritionTotal(String nutritionPerServingJson, Integer servings) {
        if (nutritionPerServingJson == null || nutritionPerServingJson.isBlank() || servings == null) {
            return nutritionPerServingJson;
        }
        try {
            JsonNode node = objectMapper.readTree(nutritionPerServingJson);
            if (!node.isObject()) {
                return nutritionPerServingJson;
            }
            ObjectNode total = objectMapper.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isNumber()) {
                    total.put(entry.getKey(), value.asDouble() * servings);
                } else {
                    total.set(entry.getKey(), value);
                }
            });
            return objectMapper.writeValueAsString(total);
        } catch (Exception ex) {
            return nutritionPerServingJson;
        }
    }

    public record IngredientView(String name, String quantity, String unit, boolean stockPriority) {
    }

    public record MissingIngredientData(String name, String quantityText, Double quantityValue, String unit,
                                        Integer shelfLifeDays) {
    }

    public record NutritionItem(String label, String value) {
    }

    public record ParseResult<T>(T value, boolean error) {
    }
}
