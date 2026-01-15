package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.domain.ShoppingItem;
import com.heibonsalaryman.recipemaker.repository.PantryItemRepository;
import com.heibonsalaryman.recipemaker.repository.ShoppingItemRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShoppingService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final PantryItemRepository pantryItemRepository;

    public ShoppingService(ShoppingItemRepository shoppingItemRepository,
                           PantryItemRepository pantryItemRepository) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.pantryItemRepository = pantryItemRepository;
    }

    public record MissingIngredientInput(String name, Double quantity, String unit, Integer shelfLifeDays) {
    }

    @Transactional
    public PantryItem moveToPantry(UUID shoppingItemId) {
        ShoppingItem item = shoppingItemRepository.findById(shoppingItemId)
            .orElseThrow(() -> new IllegalArgumentException("Shopping item not found"));

        PantryItem pantryItem = new PantryItem();
        pantryItem.setName(item.getName());
        pantryItem.setQuantity(item.getQuantity());
        pantryItem.setUnit(item.getUnit());
        pantryItem.setPurchasedAt(LocalDate.now());
        pantryItem.setShelfLifeDaysPredicted(item.getShelfLifeDaysHint());
        PantryItem saved = pantryItemRepository.save(pantryItem);

        shoppingItemRepository.delete(item);
        return saved;
    }

    @Transactional
    public int addMissingIngredientsToShopping(List<MissingIngredientInput> missingIngredients) {
        int updatedCount = 0;
        for (MissingIngredientInput missing : missingIngredients) {
            if (missing == null || missing.name() == null || missing.name().isBlank()) {
                continue;
            }
            ShoppingItem target = findMergeTarget(missing);
            if (target != null) {
                target.setQuantity(mergeQuantity(target.getQuantity(), missing.quantity()));
                if (missing.shelfLifeDays() != null) {
                    target.setShelfLifeDaysHint(missing.shelfLifeDays());
                }
                shoppingItemRepository.save(target);
            } else {
                ShoppingItem item = new ShoppingItem();
                item.setName(missing.name().trim());
                item.setQuantity(missing.quantity());
                item.setUnit(trimUnit(missing.unit()));
                item.setChecked(false);
                item.setShelfLifeDaysHint(missing.shelfLifeDays());
                shoppingItemRepository.save(item);
            }
            updatedCount++;
        }
        return updatedCount;
    }

    private ShoppingItem findMergeTarget(MissingIngredientInput missing) {
        List<ShoppingItem> sameName = shoppingItemRepository.findByNameIgnoreCase(missing.name().trim());
        if (sameName.isEmpty()) {
            return null;
        }
        String normalizedUnit = normalizeUnit(missing.unit());
        return sameName.stream()
            .filter(item -> Objects.equals(normalizeUnit(item.getUnit()), normalizedUnit))
            .findFirst()
            .orElse(null);
    }

    private String normalizeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return null;
        }
        return unit.trim().toLowerCase(Locale.ROOT);
    }

    private String trimUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return null;
        }
        return unit.trim();
    }

    private Double mergeQuantity(Double current, Double addition) {
        if (current == null) {
            return addition;
        }
        if (addition == null) {
            return current;
        }
        return current + addition;
    }
}
