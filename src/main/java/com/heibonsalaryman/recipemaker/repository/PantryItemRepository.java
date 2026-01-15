package com.heibonsalaryman.recipemaker.repository;

import com.heibonsalaryman.recipemaker.domain.PantryItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {
    List<PantryItem> findByNameContainingIgnoreCase(String name);
}
