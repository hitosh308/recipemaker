package com.heibonsalaryman.recipemaker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heibonsalaryman.recipemaker.domain.CookLog;
import com.heibonsalaryman.recipemaker.repository.CookLogRepository;
import com.heibonsalaryman.recipemaker.web.dto.DailyNutritionDto;
import com.heibonsalaryman.recipemaker.web.dto.HealthSummaryDto;
import com.heibonsalaryman.recipemaker.web.dto.HealthViewModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);

    private final CookLogRepository cookLogRepository;
    private final ObjectMapper objectMapper;

    public HealthService(CookLogRepository cookLogRepository, ObjectMapper objectMapper) {
        this.cookLogRepository = cookLogRepository;
        this.objectMapper = objectMapper;
    }

    public HealthViewModel getHealthSummary(int days) {
        int normalizedDays = normalizeDays(days);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(normalizedDays - 1L);
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        List<CookLog> logs = cookLogRepository.findByCookedAtBetween(from, to);

        Map<LocalDate, NutritionTotals> totalsByDate = new LinkedHashMap<>();
        for (int i = 0; i < normalizedDays; i++) {
            totalsByDate.put(startDate.plusDays(i), new NutritionTotals());
        }

        for (CookLog log : logs) {
            if (log.getCookedAt() == null) {
                continue;
            }
            LocalDate date = log.getCookedAt().toLocalDate();
            NutritionTotals dailyTotals = totalsByDate.get(date);
            if (dailyTotals == null) {
                continue;
            }
            NutritionTotals extracted = extractNutrition(log);
            dailyTotals.add(extracted);
        }

        List<DailyNutritionDto> dailyList = new ArrayList<>();
        NutritionTotals total = new NutritionTotals();
        int maxCalories = 0;
        for (Map.Entry<LocalDate, NutritionTotals> entry : totalsByDate.entrySet()) {
            NutritionTotals dayTotals = entry.getValue();
            total.add(dayTotals);
            DailyNutritionDto dto = toDailyDto(entry.getKey(), dayTotals);
            dailyList.add(0, dto);
            maxCalories = Math.max(maxCalories, dto.calories());
        }

        HealthSummaryDto summary = buildSummary(total, normalizedDays);
        boolean hasData = !logs.isEmpty();
        return new HealthViewModel(normalizedDays, startDate, endDate, summary, dailyList, hasData, maxCalories);
    }

    private int normalizeDays(int days) {
        return days == 30 ? 30 : 14;
    }

    private NutritionTotals extractNutrition(CookLog log) {
        String totalJson = log.getNutritionTotalJson();
        if (totalJson != null && !totalJson.isBlank()) {
            return parseNutrition(totalJson, 1);
        }

        String perServingJson = log.getNutritionPerServingJson();
        if (perServingJson == null || perServingJson.isBlank()) {
            return new NutritionTotals();
        }

        int servings = log.getServings() == null || log.getServings() <= 0 ? 1 : log.getServings();
        return parseNutrition(perServingJson, servings);
    }

    private NutritionTotals parseNutrition(String json, int multiplier) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node == null || !node.isObject()) {
                return new NutritionTotals();
            }
            NutritionTotals totals = new NutritionTotals();
            totals.calories = readNumber(node, "calories") * multiplier;
            totals.protein = readNumber(node, "protein") * multiplier;
            totals.fat = readNumber(node, "fat") * multiplier;
            totals.carbs = readNumber(node, "carbs") * multiplier;
            totals.salt = readNumber(node, "salt") * multiplier;
            totals.sugar = readNumber(node, "sugar") * multiplier;
            return totals;
        } catch (Exception ex) {
            logger.warn("Failed to parse nutrition json", ex);
            return new NutritionTotals();
        }
    }

    private double readNumber(JsonNode node, String key) {
        if (!node.has(key) || node.get(key).isNull()) {
            return 0.0;
        }
        JsonNode value = node.get(key);
        if (value.isNumber()) {
            return value.asDouble();
        }
        if (value.isTextual()) {
            try {
                return Double.parseDouble(value.asText());
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private DailyNutritionDto toDailyDto(LocalDate date, NutritionTotals totals) {
        return new DailyNutritionDto(
            date,
            (int) Math.round(totals.calories),
            roundToOneDecimal(totals.protein),
            roundToOneDecimal(totals.fat),
            roundToOneDecimal(totals.carbs),
            roundToOneDecimal(totals.salt),
            roundToOneDecimal(totals.sugar)
        );
    }

    private HealthSummaryDto buildSummary(NutritionTotals totals, int days) {
        int totalCalories = (int) Math.round(totals.calories);
        int avgCalories = (int) Math.round(totals.calories / days);
        double avgProtein = roundToOneDecimal(totals.protein / days);
        double avgSalt = roundToOneDecimal(totals.salt / days);
        return new HealthSummaryDto(totalCalories, avgCalories, avgProtein, avgSalt);
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static class NutritionTotals {
        private double calories;
        private double protein;
        private double fat;
        private double carbs;
        private double salt;
        private double sugar;

        private void add(NutritionTotals other) {
            this.calories += other.calories;
            this.protein += other.protein;
            this.fat += other.fat;
            this.carbs += other.carbs;
            this.salt += other.salt;
            this.sugar += other.sugar;
        }
    }
}
