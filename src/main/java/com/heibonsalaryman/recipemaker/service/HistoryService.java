package com.heibonsalaryman.recipemaker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heibonsalaryman.recipemaker.domain.CookLog;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.repository.CookLogRepository;
import com.heibonsalaryman.recipemaker.web.dto.HistoryRowDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

    private final CookLogRepository cookLogRepository;
    private final ObjectMapper objectMapper;

    public HistoryService(CookLogRepository cookLogRepository, ObjectMapper objectMapper) {
        this.cookLogRepository = cookLogRepository;
        this.objectMapper = objectMapper;
    }

    public List<HistoryRowDto> listRecent(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(days);
        List<CookLog> logs = cookLogRepository.findByCookedAtBetweenOrderByCookedAtDesc(from, now);
        List<HistoryRowDto> rows = new ArrayList<>();
        for (CookLog log : logs) {
            Recipe recipe = log.getRecipe();
            UUID recipeId = recipe != null ? recipe.getId() : null;
            String recipeTitle = recipe != null ? recipe.getTitle() : (recipeId != null ? recipeId.toString() : "-");
            rows.add(new HistoryRowDto(
                log.getId(),
                log.getCookedAt(),
                recipeId,
                recipeTitle,
                log.getServings(),
                buildNutritionSummary(log.getNutritionTotalJson()),
                log.getWeekStart()
            ));
        }
        return rows;
    }

    public void deleteById(UUID id) {
        cookLogRepository.deleteById(id);
    }

    private String buildNutritionSummary(String nutritionTotalJson) {
        if (nutritionTotalJson == null || nutritionTotalJson.isBlank()) {
            return "栄養データなし";
        }
        try {
            JsonNode node = objectMapper.readTree(nutritionTotalJson);
            if (!node.isObject()) {
                return "栄養データなし";
            }
            Double calories = readNumber(node, "calories");
            Double protein = readNumber(node, "protein");
            Double salt = readNumber(node, "salt");
            List<String> parts = new ArrayList<>();
            if (calories != null) {
                parts.add("カロリー " + formatNumber(calories) + "kcal");
            }
            if (protein != null) {
                parts.add("たんぱく質 " + formatNumber(protein) + "g");
            }
            if (salt != null) {
                parts.add("塩分 " + formatNumber(salt) + "g");
            }
            if (parts.isEmpty()) {
                return "栄養データなし";
            }
            return String.join(" / ", parts);
        } catch (Exception ex) {
            return "栄養データなし";
        }
    }

    private Double readNumber(JsonNode node, String key) {
        if (!node.has(key) || node.get(key).isNull()) {
            return null;
        }
        JsonNode value = node.get(key);
        if (value.isNumber()) {
            return value.asDouble();
        }
        if (value.isTextual()) {
            try {
                return Double.parseDouble(value.asText());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format("%.1f", value);
    }
}
