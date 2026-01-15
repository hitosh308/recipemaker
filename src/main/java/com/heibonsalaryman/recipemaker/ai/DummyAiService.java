package com.heibonsalaryman.recipemaker.ai;

import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DummyAiService implements AiService {

    @Override
    public ShelfLifeEstimate estimateShelfLifeDays(String name, StorageType storageType, String assumptions) {
        int baseDays = switch (storageType) {
            case FREEZER -> 30;
            case FRIDGE -> 7;
            case ROOM -> 3;
        };
        return new ShelfLifeEstimate(baseDays, 0.6, assumptions == null ? "Default estimate" : assumptions);
    }

    @Override
    public List<Recipe> generateWeeklyRecipeCandidates(LocalDate weekStart, String constraints, String pantryContext,
                                                       String recentHistory) {
        List<Recipe> recipes = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Recipe recipe = new Recipe();
            recipe.setTitle("AI Candidate " + i + " (" + weekStart + ")");
            recipe.setServings(2 + i);
            recipe.setCookTimeMinutes(20 + (i * 5));
            recipe.setStepsJson("[\"Step 1\", \"Step 2\"]");
            recipe.setIngredientsJson("[\"Ingredient A\", \"Ingredient B\"]");
            recipe.setMissingIngredientsJson("[]");
            recipe.setNutritionPerServingJson("{\"calories\":" + (400 + i * 50) + "}");
            recipe.setTagsJson("[\"week" + weekStart.getDayOfMonth() + "\"]");
            recipe.setMainIngredientsJson("[\"Chicken\", \"Rice\"]");
            recipe.setSource("AI");
            recipes.add(recipe);
        }
        return recipes;
    }
}
