package com.heibonsalaryman.recipemaker.web.dto;

import com.heibonsalaryman.recipemaker.domain.StorageType;
import java.time.LocalDate;
import java.util.UUID;

public record PantryRowDto(
    UUID id,
    String name,
    Double quantity,
    String unit,
    StorageType storageType,
    LocalDate purchasedAt,
    LocalDate effectiveExpiresAt,
    Long daysToExpire,
    boolean hasOverride,
    Double expireConfidence
) {
}
