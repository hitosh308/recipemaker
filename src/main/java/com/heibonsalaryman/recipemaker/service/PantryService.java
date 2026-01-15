package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.repository.PantryItemRepository;
import com.heibonsalaryman.recipemaker.web.dto.PantryRowDto;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PantryService {

    private final PantryItemRepository pantryItemRepository;

    public PantryService(PantryItemRepository pantryItemRepository) {
        this.pantryItemRepository = pantryItemRepository;
    }

    public List<PantryRowDto> listPantryItems(Optional<String> query) {
        List<PantryItem> items = query
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(pantryItemRepository::findByNameContainingIgnoreCase)
            .orElseGet(pantryItemRepository::findAll);

        LocalDate today = LocalDate.now();

        return items.stream()
            .map(item -> toRowDto(item, today))
            .sorted(Comparator
                .comparing(PantryRowDto::effectiveExpiresAt, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(PantryRowDto::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .toList();
    }

    public void deletePantryItem(UUID id) {
        pantryItemRepository.deleteById(id);
    }

    private PantryRowDto toRowDto(PantryItem item, LocalDate today) {
        LocalDate effectiveExpiresAt = item.getExpiresAtOverride() != null
            ? item.getExpiresAtOverride()
            : item.getExpiresAtPredicted();
        Long daysToExpire = effectiveExpiresAt != null
            ? ChronoUnit.DAYS.between(today, effectiveExpiresAt)
            : null;

        return new PantryRowDto(
            item.getId(),
            item.getName(),
            item.getQuantity(),
            item.getUnit(),
            item.getStorageType(),
            item.getPurchasedAt(),
            effectiveExpiresAt,
            daysToExpire,
            item.getExpiresAtOverride() != null,
            item.getExpireConfidence()
        );
    }
}
