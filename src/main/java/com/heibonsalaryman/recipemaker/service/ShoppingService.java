package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.domain.ShoppingItem;
import com.heibonsalaryman.recipemaker.repository.PantryItemRepository;
import com.heibonsalaryman.recipemaker.repository.ShoppingItemRepository;
import java.time.LocalDate;
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
}
