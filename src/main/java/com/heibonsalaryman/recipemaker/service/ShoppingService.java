package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.domain.ShoppingItem;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import com.heibonsalaryman.recipemaker.repository.ShoppingItemRepository;
import com.heibonsalaryman.recipemaker.web.dto.PantryForm;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShoppingService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final PantryService pantryService;

    public ShoppingService(ShoppingItemRepository shoppingItemRepository,
                           PantryService pantryService) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.pantryService = pantryService;
    }

    public record MissingIngredientInput(String name, Double quantity, String unit, Integer shelfLifeDays) {
    }

    public List<ShoppingItem> list() {
        return shoppingItemRepository.findAll().stream()
            .sorted(Comparator
                .comparing((ShoppingItem item) -> Boolean.TRUE.equals(item.getChecked()))
                .thenComparing(ShoppingItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .toList();
    }

    @Transactional
    public ShoppingItem addOrMerge(String name, Double quantity, String unit, Integer shelfLifeDaysHint) {
        String trimmedName = name == null ? null : name.trim();
        if (trimmedName == null || trimmedName.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        MissingIngredientInput input = new MissingIngredientInput(trimmedName, quantity, unit, shelfLifeDaysHint);
        ShoppingItem target = findMergeTarget(input);
        if (target != null) {
            target.setQuantity(mergeQuantity(target.getQuantity(), quantity));
            if (shelfLifeDaysHint != null) {
                target.setShelfLifeDaysHint(shelfLifeDaysHint);
            }
            return shoppingItemRepository.save(target);
        }
        ShoppingItem item = new ShoppingItem();
        item.setName(trimmedName);
        item.setQuantity(quantity);
        item.setUnit(trimUnit(unit));
        item.setChecked(false);
        item.setShelfLifeDaysHint(shelfLifeDaysHint);
        return shoppingItemRepository.save(item);
    }

    @Transactional
    public ShoppingItem toggleChecked(UUID shoppingItemId) {
        ShoppingItem item = shoppingItemRepository.findById(shoppingItemId)
            .orElseThrow(() -> new IllegalArgumentException("Shopping item not found"));
        item.setChecked(!Boolean.TRUE.equals(item.getChecked()));
        return shoppingItemRepository.save(item);
    }

    @Transactional
    public void delete(UUID shoppingItemId) {
        shoppingItemRepository.deleteById(shoppingItemId);
    }

    @Transactional
    public PantryService.PantrySaveResult moveToPantry(UUID shoppingItemId, StorageType storageType) {
        ShoppingItem item = shoppingItemRepository.findById(shoppingItemId)
            .orElseThrow(() -> new IllegalArgumentException("Shopping item not found"));

        PantryForm form = new PantryForm();
        form.setName(item.getName());
        form.setQuantity(item.getQuantity());
        form.setUnit(item.getUnit());
        form.setStorageType(storageType);
        form.setPurchasedAt(LocalDate.now());

        PantryService.PantrySaveResult result = pantryService.createPantryItem(form);
        shoppingItemRepository.delete(item);
        return result;
    }

    @Transactional
    public int addMissingIngredientsToShopping(List<MissingIngredientInput> missingIngredients) {
        int updatedCount = 0;
        for (MissingIngredientInput missing : missingIngredients) {
            if (missing == null || missing.name() == null || missing.name().isBlank()) {
                continue;
            }
            addOrMerge(missing.name(), missing.quantity(), missing.unit(), missing.shelfLifeDays());
            updatedCount++;
        }
        return updatedCount;
    }

    private ShoppingItem findMergeTarget(MissingIngredientInput missing) {
        List<ShoppingItem> sameName = shoppingItemRepository.findByNameIgnoreCaseAndCheckedFalse(
            missing.name().trim()
        );
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
