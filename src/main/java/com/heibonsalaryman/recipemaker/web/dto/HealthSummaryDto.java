package com.heibonsalaryman.recipemaker.web.dto;

public record HealthSummaryDto(
    int totalCalories,
    int avgCalories,
    double avgProtein,
    double avgSalt
) {
}
