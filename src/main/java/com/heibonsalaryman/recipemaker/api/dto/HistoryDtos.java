package com.heibonsalaryman.recipemaker.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public final class HistoryDtos {

    private HistoryDtos() {
    }

    public record CreateRequest(UUID recipeId, LocalDateTime cookedAt, Integer servings,
                                String nutritionTotalJson, String nutritionPerServingJson,
                                String tagsJson, String mainIngredientsJson) {
    }
}
