package com.heibonsalaryman.recipemaker.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoryRowDto(
    UUID id,
    LocalDateTime cookedAt,
    UUID recipeId,
    String recipeTitle,
    Integer servings,
    String nutritionSummary,
    LocalDate weekStart
) {
}
