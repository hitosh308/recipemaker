package com.heibonsalaryman.recipemaker.api;

import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.repository.RecipeRepository;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
public class RecipeApiController {

    private final RecipeRepository recipeRepository;

    public RecipeApiController(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @GetMapping("/{id}")
    public Recipe getRecipe(@PathVariable("id") UUID id) {
        return recipeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
    }
}
