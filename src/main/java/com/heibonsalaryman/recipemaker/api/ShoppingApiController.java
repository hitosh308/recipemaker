package com.heibonsalaryman.recipemaker.api;

import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.domain.ShoppingItem;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import com.heibonsalaryman.recipemaker.repository.ShoppingItemRepository;
import com.heibonsalaryman.recipemaker.service.ShoppingService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shopping")
public class ShoppingApiController {

    private final ShoppingItemRepository shoppingItemRepository;
    private final ShoppingService shoppingService;

    public ShoppingApiController(ShoppingItemRepository shoppingItemRepository,
                                 ShoppingService shoppingService) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.shoppingService = shoppingService;
    }

    @GetMapping
    public List<ShoppingItem> list() {
        return shoppingItemRepository.findAll();
    }

    @PostMapping
    public ShoppingItem create(@RequestBody ShoppingItem item) {
        item.setId(null);
        return shoppingItemRepository.save(item);
    }

    @PutMapping("/{id}")
    public ShoppingItem update(@PathVariable("id") UUID id, @RequestBody ShoppingItem item) {
        ShoppingItem existing = shoppingItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Shopping item not found"));
        existing.setName(item.getName());
        existing.setQuantity(item.getQuantity());
        existing.setUnit(item.getUnit());
        existing.setChecked(item.getChecked());
        existing.setShelfLifeDaysHint(item.getShelfLifeDaysHint());
        return shoppingItemRepository.save(existing);
    }

    @PostMapping("/{id}/move-to-pantry")
    public PantryItem moveToPantry(@PathVariable("id") UUID id,
                                   @RequestParam("storageType") StorageType storageType) {
        return shoppingService.moveToPantry(id, storageType).item();
    }
}
