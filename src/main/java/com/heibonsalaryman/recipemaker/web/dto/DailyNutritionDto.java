package com.heibonsalaryman.recipemaker.web.dto;

import java.time.LocalDate;

public record DailyNutritionDto(
    LocalDate date,
    int calories,
    double protein,
    double fat,
    double carbs,
    double salt,
    double sugar
) {
}
