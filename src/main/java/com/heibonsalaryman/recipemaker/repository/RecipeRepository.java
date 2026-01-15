package com.heibonsalaryman.recipemaker.repository;

import com.heibonsalaryman.recipemaker.domain.Recipe;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
}
