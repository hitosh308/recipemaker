package com.heibonsalaryman.recipemaker.api.dto;

import com.heibonsalaryman.recipemaker.domain.WeekStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class WeekDtos {

    private WeekDtos() {
    }

    public record RecipeSummary(UUID id, String title) {
    }

    public record WeekSummary(LocalDate weekStart, LocalDate weekEnd, WeekStatus status, RecipeSummary confirmedRecipe) {
    }

    public record WeekCandidate(UUID id, RecipeSummary recipe) {
    }

    public record WeekDetail(LocalDate weekStart, LocalDate weekEnd, WeekStatus status, RecipeSummary confirmedRecipe,
                             List<WeekCandidate> candidates) {
    }

    public record ConfirmRequest(UUID recipeId) {
    }
}
