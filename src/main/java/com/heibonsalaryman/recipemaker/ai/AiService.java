package com.heibonsalaryman.recipemaker.ai;

import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import java.time.LocalDate;
import java.util.List;

public interface AiService {

    ShelfLifeEstimate estimateShelfLifeDays(String name, StorageType storageType, String assumptions);

    List<Recipe> generateWeeklyRecipeCandidates(LocalDate weekStart, String constraints, String pantryContext,
                                                String recentHistory);

    record ShelfLifeEstimate(int days, double confidence, String assumptions) {
    }
}
