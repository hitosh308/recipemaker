package com.heibonsalaryman.recipemaker.api;

import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.repository.PantryItemRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pantry")
public class PantryApiController {

    private final PantryItemRepository pantryItemRepository;

    public PantryApiController(PantryItemRepository pantryItemRepository) {
        this.pantryItemRepository = pantryItemRepository;
    }

    @GetMapping
    public List<PantryItem> list() {
        return pantryItemRepository.findAll();
    }

    @PostMapping
    public PantryItem create(@RequestBody PantryItem item) {
        item.setId(null);
        return pantryItemRepository.save(item);
    }

    @PutMapping("/{id}")
    public PantryItem update(@PathVariable("id") UUID id, @RequestBody PantryItem item) {
        PantryItem existing = pantryItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pantry item not found"));
        existing.setName(item.getName());
        existing.setQuantity(item.getQuantity());
        existing.setUnit(item.getUnit());
        existing.setStorageType(item.getStorageType());
        existing.setPurchasedAt(item.getPurchasedAt());
        existing.setShelfLifeDaysPredicted(item.getShelfLifeDaysPredicted());
        existing.setExpiresAtPredicted(item.getExpiresAtPredicted());
        existing.setExpiresAtOverride(item.getExpiresAtOverride());
        existing.setExpireConfidence(item.getExpireConfidence());
        existing.setExpireAssumptions(item.getExpireAssumptions());
        existing.setExpiresEstimatedAt(item.getExpiresEstimatedAt());
        return pantryItemRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        pantryItemRepository.deleteById(id);
    }
}
