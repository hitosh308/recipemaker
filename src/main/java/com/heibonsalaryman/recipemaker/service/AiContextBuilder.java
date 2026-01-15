package com.heibonsalaryman.recipemaker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heibonsalaryman.recipemaker.domain.CookLog;
import com.heibonsalaryman.recipemaker.domain.PantryItem;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(AiContextBuilder.class);

    private final ObjectMapper objectMapper;

    public AiContextBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildPantryContext(List<PantryItem> items, LocalDate today, int limit) {
        List<PantryContextItem> contextItems = items.stream()
            .map(item -> toPantryContextItem(item, today))
            .sorted(Comparator
                .comparing(PantryContextItem::effectiveExpiresAt, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(PantryContextItem::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .limit(limit)
            .toList();
        return writeJson(contextItems, "[]");
    }

    public String buildRecentHistory(List<CookLog> logs) {
        List<HistoryItem> historyItems = logs.stream()
            .map(this::toHistoryItem)
            .filter(Objects::nonNull)
            .toList();
        return writeJson(historyItems, "[]");
    }

    public String defaultConstraints() {
        return "一週間の献立として偏りが少ないこと。"
            + "味付け・調理法・主菜の種類が連続しないようにする。"
            + "塩分・糖分が高すぎないように配慮し、たんぱく質を一定量含める。"
            + "調理時間が60分を超えるものは避ける。";
    }

    private PantryContextItem toPantryContextItem(PantryItem item, LocalDate today) {
        LocalDate effectiveExpiresAt = item.getExpiresAtOverride() != null
            ? item.getExpiresAtOverride()
            : item.getExpiresAtPredicted();
        Long daysToExpire = effectiveExpiresAt == null ? null : ChronoUnit.DAYS.between(today, effectiveExpiresAt);
        return new PantryContextItem(
            item.getName(),
            item.getQuantity(),
            item.getUnit(),
            item.getStorageType() == null ? null : item.getStorageType().name(),
            effectiveExpiresAt,
            daysToExpire
        );
    }

    private HistoryItem toHistoryItem(CookLog log) {
        if (log.getRecipe() == null) {
            return null;
        }
        return new HistoryItem(
            log.getRecipe().getTitle(),
            parseStringList(log.getTagsJson()),
            parseStringList(log.getMainIngredientsJson())
        );
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            log.debug("Failed to parse history list: {}", ex.getMessage());
            return List.of();
        }
    }

    private String writeJson(Object value, String fallback) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            log.warn("Failed to write AI context JSON: {}", ex.getMessage());
            return fallback;
        }
    }

    public record PantryContextItem(String name, Double quantity, String unit, String storageType,
                                    LocalDate effectiveExpiresAt, Long daysToExpire) {
    }

    public record HistoryItem(String title, List<String> tags, List<String> mainIngredients) {
    }
}
