package com.heibonsalaryman.recipemaker.web.dto;

import java.time.LocalDate;
import java.util.List;

public record HealthViewModel(
    int days,
    LocalDate startDate,
    LocalDate endDate,
    HealthSummaryDto summary,
    List<DailyNutritionDto> dailyList,
    boolean hasData,
    int maxCalories
) {
}
