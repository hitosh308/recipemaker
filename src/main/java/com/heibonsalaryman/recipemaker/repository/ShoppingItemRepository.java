package com.heibonsalaryman.recipemaker.repository;

import com.heibonsalaryman.recipemaker.domain.ShoppingItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, UUID> {
}
