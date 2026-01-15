package com.heibonsalaryman.recipemaker.repository;

import com.heibonsalaryman.recipemaker.domain.ShoppingItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, UUID> {
    List<ShoppingItem> findByNameIgnoreCase(String name);

    List<ShoppingItem> findByNameIgnoreCaseAndCheckedFalse(String name);
}
